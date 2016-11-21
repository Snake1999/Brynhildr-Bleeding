package io.nukkit.scheduler;

import io.nukkit.Nukkit;

class Task implements Runnable {
    private volatile Task next;
    private volatile long period;
    private long nextRun;
    private final Runnable task;
    private final TaskOwner owner;
    private final int id;

    Task() {
        this(null, null, -1, -1L);
    }

    Task(Runnable task) {
        this(null, task, -1, -1L);
    }

    Task(TaskOwner owner, Runnable task, int id, long period) {
        this.next = null;
        this.owner = owner;
        this.task = task;
        this.id = id;
        this.period = period;
    }

    public final int getTaskId() {
        return this.id;
    }

    public final TaskOwner getOwner() {
        return this.owner;
    }

    public boolean isSync() {
        return true;
    }

    public void run() {
        this.task.run();
    }

    long getPeriod() {
        return this.period;
    }

    void setPeriod(long period) {
        this.period = period;
    }

    long getNextRun() {
        return this.nextRun;
    }

    void setNextRun(long nextRun) {
        this.nextRun = nextRun;
    }

    Task getNext() {
        return this.next;
    }

    void setNext(Task next) {
        this.next = next;
    }

    Class getTaskClass() {
        return this.task.getClass();
    }

    public void cancel() {
        Nukkit.getScheduler().cancelTask(this.id);
    }

    boolean cancel0() {
        this.setPeriod(-2L);
        return true;
    }
}
