package io.nukkit.scheduler;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/21 11:30.
 * All rights reserved
 *
 * Masks the owner of a task.
 * This can be a plugin or something else.
 */
public interface TaskOwner {

    /**
     * Tasks will only run when owner is ready.
     * This is set to false when owner is not ready, for example, plugin disabled
     * @return If this owner is ready to hold tasks
     */
    boolean isReadyForTasks();
}
