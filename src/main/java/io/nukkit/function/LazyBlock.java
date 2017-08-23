package io.nukkit.function;

import io.nukkit.util.math.BlockPos;
import io.nukkit.world.WorldUnique;

/**
 * Created by Mulan Lin('Snake1999') on 2016/12/3 17:39.
 * All rights reserved
 */
public interface LazyBlock {

    WorldUnique getWorldUnique();

    BlockPos getBlockPos();

    boolean isValid();
}
