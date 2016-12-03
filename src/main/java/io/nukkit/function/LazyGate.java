package io.nukkit.function;

/**
 * Created by Mulan Lin('Snake1999') on 2016/12/3 17:36.
 * All rights reserved
 */
public interface LazyGate extends LazyBlock {

    void setOpened(boolean value);

    boolean isOpened();

    default void toggleOpened() {
        setOpened(!isOpened());
    }

}
