package io.nukkit.scheduler;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * The fundamental concepts for this implementation:
 * <li>Main thread owns {@link #head} and {@link #currentTick}, but it may be read from any thread</li>
 * <li>Main thread exclusively controls {@link #temp} and {@link #pending}.
 * They are never to be accessed outside of the main thread; alternatives exist to prevent locking.</li>
 * <li>{@link #head} to {@link #tail} act as a linked list/queue, with 1 consumer and infinite producers.
 * Adding to the tail is atomic and very efficient; utility method is {@link #handle(NukkitTask, long)} or {@link #addTask(NukkitTask)}. </li>
 * <li>Changing the period on a task is delicate.
 * Any future task needs to notify waiting threads.
 * Async tasks must be synchronized to make sure that any thread that's finishing will remove itself from {@link #runners}.
 * Another utility method is provided for this, {@link #cancelTask(int)}</li>
 * <li>{@link #runners} provides a moderately up-to-date view of active tasks.
 * If the linked head to tail set is read, all remaining tasks that were active at the time execution started will be located in runners.</li>
 * <li>Async tasks are responsible for removing themselves from runners</li>
 * <li>Sync tasks are only to be removed from runners on the main thread when coupled with a removal from pending and temp.</li>
 * <li>Most of the design in this scheduler relies on queuing special tasks to perform any data changes on the main thread.
 * When executed from inside a synchronous method, the scheduler will be updated before next execution by virtue of the frequent {@link #parsePending()} calls.</li>
 */
public class NukkitScheduler implements BukkitScheduler {

    private static final int RECENT_TICKS;

    static {
        RECENT_TICKS = 30;
    }

    /**
     * Counter for IDs. Order doesn't matter, only uniqueness.
     */
    private final AtomicInteger ids = new AtomicInteger(1);
    /**
     * Main thread logic only
     */
    private final PriorityQueue<NukkitTask> pending = new PriorityQueue<NukkitTask>(10,
            new Comparator<NukkitTask>() {
                public int compare(final NukkitTask o1, final NukkitTask o2) {
                    int value = Long.compare(o1.getNextRun(), o2.getNextRun());

                    // If the tasks should run on the same tick they should be run FIFO
                    return value != 0 ? value : Integer.compare(o1.getTaskId(), o2.getTaskId());
                }
            });
    /**
     * Main thread logic only
     */
    private final List<NukkitTask> temp = new ArrayList<NukkitTask>();
    /**
     * These are tasks that are currently active. It's provided for 'viewing' the current state.
     */
    private final ConcurrentHashMap<Integer, NukkitTask> runners = new ConcurrentHashMap<>();
    private final Executor executor = Executors.newCachedThreadPool(new com.google.common.util.concurrent.ThreadFactoryBuilder().setNameFormat("NukkitScheduler Thread - %1$d").build());
    /**
     * Current head of linked-list. This reference is always stale, {@link NukkitTask#next} is the live reference.
     */
    private volatile NukkitTask head = new NukkitTask();
    /**
     * Tail of a linked-list. AtomicReference only matters when adding to queue
     */
    private final AtomicReference<NukkitTask> tail = new AtomicReference<NukkitTask>(head);
    private volatile int currentTick = -1;
    private NukkitAsyncDebugger debugHead = new NukkitAsyncDebugger(-1, null, null) {
        @Override
        StringBuilder debugTo(StringBuilder string) {
            return string;
        }
    };
    private NukkitAsyncDebugger debugTail = debugHead;

    private static void validate(final Plugin plugin, final Object task) {
        Validate.notNull(plugin, "Plugin cannot be null");
        Validate.notNull(task, "Task cannot be null");
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register task while disabled");
        }
    }

    public int scheduleSyncDelayedTask(final Plugin plugin, final Runnable task) {
        return this.scheduleSyncDelayedTask(plugin, task, 0l);
    }

    public BukkitTask runTask(Plugin plugin, Runnable runnable) {
        return runTaskLater(plugin, runnable, 0l);
    }

    @Deprecated
    public int scheduleAsyncDelayedTask(final Plugin plugin, final Runnable task) {
        return this.scheduleAsyncDelayedTask(plugin, task, 0l);
    }

    public BukkitTask runTaskAsynchronously(Plugin plugin, Runnable runnable) {
        return runTaskLaterAsynchronously(plugin, runnable, 0l);
    }

    public int scheduleSyncDelayedTask(final Plugin plugin, final Runnable task, final long delay) {
        return this.scheduleSyncRepeatingTask(plugin, task, delay, -1l);
    }

    public BukkitTask runTaskLater(Plugin plugin, Runnable runnable, long delay) {
        return runTaskTimer(plugin, runnable, delay, -1l);
    }

    @Deprecated
    public int scheduleAsyncDelayedTask(final Plugin plugin, final Runnable task, final long delay) {
        return this.scheduleAsyncRepeatingTask(plugin, task, delay, -1l);
    }

    public BukkitTask runTaskLaterAsynchronously(Plugin plugin, Runnable runnable, long delay) {
        return runTaskTimerAsynchronously(plugin, runnable, delay, -1l);
    }

    public int scheduleSyncRepeatingTask(final Plugin plugin, final Runnable runnable, long delay, long period) {
        return runTaskTimer(plugin, runnable, delay, period).getTaskId();
    }

    public BukkitTask runTaskTimer(Plugin plugin, Runnable runnable, long delay, long period) {
        validate(plugin, runnable);
        if (delay < 0l) {
            delay = 0;
        }
        if (period == 0l) {
            period = 1l;
        } else if (period < -1l) {
            period = -1l;
        }
        return handle(new NukkitTask(plugin, runnable, nextId(), period), delay);
    }

    @Deprecated
    public int scheduleAsyncRepeatingTask(final Plugin plugin, final Runnable runnable, long delay, long period) {
        return runTaskTimerAsynchronously(plugin, runnable, delay, period).getTaskId();
    }

    public BukkitTask runTaskTimerAsynchronously(Plugin plugin, Runnable runnable, long delay, long period) {
        validate(plugin, runnable);
        if (delay < 0l) {
            delay = 0;
        }
        if (period == 0l) {
            period = 1l;
        } else if (period < -1l) {
            period = -1l;
        }
        return handle(new NukkitAsyncTask(runners, plugin, runnable, nextId(), period), delay);
    }

    public <T> Future<T> callSyncMethod(final Plugin plugin, final Callable<T> task) {
        validate(plugin, task);
        final NukkitFuture<T> future = new NukkitFuture<T>(task, plugin, nextId());
        handle(future, 0l);
        return future;
    }

    public void cancelTask(final int taskId) {
        if (taskId <= 0) {
            return;
        }
        NukkitTask task = runners.get(taskId);
        if (task != null) {
            task.cancel0();
        }
        task = new NukkitTask(
                new Runnable() {
                    public void run() {
                        if (!check(NukkitScheduler.this.temp)) {
                            check(NukkitScheduler.this.pending);
                        }
                    }

                    private boolean check(final Iterable<NukkitTask> collection) {
                        final Iterator<NukkitTask> tasks = collection.iterator();
                        while (tasks.hasNext()) {
                            final NukkitTask task = tasks.next();
                            if (task.getTaskId() == taskId) {
                                task.cancel0();
                                tasks.remove();
                                if (task.isSync()) {
                                    runners.remove(taskId);
                                }
                                return true;
                            }
                        }
                        return false;
                    }
                });
        handle(task, 0l);
        for (NukkitTask taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
            if (taskPending == task) {
                return;
            }
            if (taskPending.getTaskId() == taskId) {
                taskPending.cancel0();
            }
        }
    }

    public void cancelTasks(final Plugin plugin) {
        Validate.notNull(plugin, "Cannot cancel tasks of null plugin");
        final NukkitTask task = new NukkitTask(
                new Runnable() {
                    public void run() {
                        check(NukkitScheduler.this.pending);
                        check(NukkitScheduler.this.temp);
                    }

                    void check(final Iterable<NukkitTask> collection) {
                        final Iterator<NukkitTask> tasks = collection.iterator();
                        while (tasks.hasNext()) {
                            final NukkitTask task = tasks.next();
                            if (task.getOwner().equals(plugin)) {
                                task.cancel0();
                                tasks.remove();
                                if (task.isSync()) {
                                    runners.remove(task.getTaskId());
                                }
                            }
                        }
                    }
                });
        handle(task, 0l);
        for (NukkitTask taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
            if (taskPending == task) {
                return;
            }
            if (taskPending.getTaskId() != -1 && taskPending.getOwner().equals(plugin)) {
                taskPending.cancel0();
            }
        }
        for (NukkitTask runner : runners.values()) {
            if (runner.getOwner().equals(plugin)) {
                runner.cancel0();
            }
        }
    }

    public void cancelAllTasks() {
        final NukkitTask task = new NukkitTask(
                new Runnable() {
                    public void run() {
                        Iterator<NukkitTask> it = NukkitScheduler.this.runners.values().iterator();
                        while (it.hasNext()) {
                            NukkitTask task = it.next();
                            task.cancel0();
                            if (task.isSync()) {
                                it.remove();
                            }
                        }
                        NukkitScheduler.this.pending.clear();
                        NukkitScheduler.this.temp.clear();
                    }
                });
        handle(task, 0l);
        for (NukkitTask taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
            if (taskPending == task) {
                break;
            }
            taskPending.cancel0();
        }
        for (NukkitTask runner : runners.values()) {
            runner.cancel0();
        }
    }

    public boolean isCurrentlyRunning(final int taskId) {
        final NukkitTask task = runners.get(taskId);
        if (task == null || task.isSync()) {
            return false;
        }
        final NukkitAsyncTask asyncTask = (NukkitAsyncTask) task;
        synchronized (asyncTask.getWorkers()) {
            return asyncTask.getWorkers().isEmpty();
        }
    }

    public boolean isQueued(final int taskId) {
        if (taskId <= 0) {
            return false;
        }
        for (NukkitTask task = head.getNext(); task != null; task = task.getNext()) {
            if (task.getTaskId() == taskId) {
                return task.getPeriod() >= -1l; // The task will run
            }
        }
        NukkitTask task = runners.get(taskId);
        return task != null && task.getPeriod() >= -1l;
    }

    public List<BukkitWorker> getActiveWorkers() {
        final ArrayList<BukkitWorker> workers = new ArrayList<BukkitWorker>();
        for (final NukkitTask taskObj : runners.values()) {
            // Iterator will be a best-effort (may fail to grab very new values) if called from an async thread
            if (taskObj.isSync()) {
                continue;
            }
            final NukkitAsyncTask task = (NukkitAsyncTask) taskObj;
            synchronized (task.getWorkers()) {
                // This will never have an issue with stale threads; it's state-safe
                workers.addAll(task.getWorkers());
            }
        }
        return workers;
    }

    public List<BukkitTask> getPendingTasks() {
        final ArrayList<NukkitTask> truePending = new ArrayList<NukkitTask>();
        for (NukkitTask task = head.getNext(); task != null; task = task.getNext()) {
            if (task.getTaskId() != -1) {
                // -1 is special code
                truePending.add(task);
            }
        }

        final ArrayList<BukkitTask> pending = new ArrayList<BukkitTask>();
        for (NukkitTask task : runners.values()) {
            if (task.getPeriod() >= -1l) {
                pending.add(task);
            }
        }

        for (final NukkitTask task : truePending) {
            if (task.getPeriod() >= -1l && !pending.contains(task)) {
                pending.add(task);
            }
        }
        return pending;
    }

    /**
     * This method is designed to never block or wait for locks; an immediate execution of all current tasks.
     */
    public void mainThreadHeartbeat(final int currentTick) {
        this.currentTick = currentTick;
        final List<NukkitTask> temp = this.temp;
        parsePending();
        while (isReady(currentTick)) {
            final NukkitTask task = pending.remove();
            if (task.getPeriod() < -1l) {
                if (task.isSync()) {
                    runners.remove(task.getTaskId(), task);
                }
                parsePending();
                continue;
            }
            if (task.isSync()) {
                try {
                    task.timings.startTiming(); // Spigot
                    task.run();
                    task.timings.stopTiming(); // Spigot
                } catch (final Throwable throwable) {
                    task.getOwner().getLogger().log(
                            Level.WARNING,
                            String.format(
                                    "Task #%s for %s generated an exception",
                                    task.getTaskId(),
                                    task.getOwner().getDescription().getFullName()),
                            throwable);
                }
                parsePending();
            } else {
                debugTail = debugTail.setNext(new NukkitAsyncDebugger(currentTick + RECENT_TICKS, task.getOwner(), task.getTaskClass()));
                executor.execute(task);
                // We don't need to parse pending
                // (async tasks must live with race-conditions if they attempt to cancel between these few lines of code)
            }
            final long period = task.getPeriod(); // State consistency
            if (period > 0) {
                task.setNextRun(currentTick + period);
                temp.add(task);
            } else if (task.isSync()) {
                runners.remove(task.getTaskId());
            }
        }
        pending.addAll(temp);
        temp.clear();
        debugHead = debugHead.getNextHead(currentTick);
    }

    private void addTask(final NukkitTask task) {
        final AtomicReference<NukkitTask> tail = this.tail;
        NukkitTask tailTask = tail.get();
        while (!tail.compareAndSet(tailTask, task)) {
            tailTask = tail.get();
        }
        tailTask.setNext(task);
    }

    private NukkitTask handle(final NukkitTask task, final long delay) {
        task.setNextRun(currentTick + delay);
        addTask(task);
        return task;
    }

    private int nextId() {
        return ids.incrementAndGet();
    }

    private void parsePending() {
        NukkitTask head = this.head;
        NukkitTask task = head.getNext();
        NukkitTask lastTask = head;
        for (; task != null; task = (lastTask = task).getNext()) {
            if (task.getTaskId() == -1) {
                task.run();
            } else if (task.getPeriod() >= -1l) {
                pending.add(task);
                runners.put(task.getTaskId(), task);
            }
        }
        // We split this because of the way things are ordered for all of the async calls in NukkitScheduler
        // (it prevents race-conditions)
        for (task = head; task != lastTask; task = head) {
            head = task.getNext();
            task.setNext(null);
        }
        this.head = lastTask;
    }

    private boolean isReady(final int currentTick) {
        return !pending.isEmpty() && pending.peek().getNextRun() <= currentTick;
    }

    @Override
    public String toString() {
        int debugTick = currentTick;
        StringBuilder string = new StringBuilder("Recent tasks from ").append(debugTick - RECENT_TICKS).append('-').append(debugTick).append('{');
        debugHead.debugTo(string);
        return string.append('}').toString();
    }

    @Deprecated
    @Override
    public int scheduleSyncDelayedTask(Plugin plugin, BukkitRunnable task, long delay) {
        return scheduleSyncDelayedTask(plugin, (Runnable) task, delay);
    }

    @Deprecated
    @Override
    public int scheduleSyncDelayedTask(Plugin plugin, BukkitRunnable task) {
        return scheduleSyncDelayedTask(plugin, (Runnable) task);
    }

    @Deprecated
    @Override
    public int scheduleSyncRepeatingTask(Plugin plugin, BukkitRunnable task, long delay, long period) {
        return scheduleSyncRepeatingTask(plugin, (Runnable) task, delay, period);
    }

    @Deprecated
    @Override
    public BukkitTask runTask(Plugin plugin, BukkitRunnable task) throws IllegalArgumentException {
        return runTask(plugin, (Runnable) task);
    }

    @Deprecated
    @Override
    public BukkitTask runTaskAsynchronously(Plugin plugin, BukkitRunnable task) throws IllegalArgumentException {
        return runTaskAsynchronously(plugin, (Runnable) task);
    }

    @Deprecated
    @Override
    public BukkitTask runTaskLater(Plugin plugin, BukkitRunnable task, long delay) throws IllegalArgumentException {
        return runTaskLater(plugin, (Runnable) task, delay);
    }

    @Deprecated
    @Override
    public BukkitTask runTaskLaterAsynchronously(Plugin plugin, BukkitRunnable task, long delay) throws IllegalArgumentException {
        return runTaskLaterAsynchronously(plugin, (Runnable) task, delay);
    }

    @Deprecated
    @Override
    public BukkitTask runTaskTimer(Plugin plugin, BukkitRunnable task, long delay, long period) throws IllegalArgumentException {
        return runTaskTimer(plugin, (Runnable) task, delay, period);
    }

    @Deprecated
    @Override
    public BukkitTask runTaskTimerAsynchronously(Plugin plugin, BukkitRunnable task, long delay, long period) throws IllegalArgumentException {
        return runTaskTimerAsynchronously(plugin, (Runnable) task, delay, period);
    }
}
