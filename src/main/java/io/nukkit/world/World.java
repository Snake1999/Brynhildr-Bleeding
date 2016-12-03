package io.nukkit.world;

import io.nukkit.block.BlockMixed;
import io.nukkit.util.math.BlockPos;

/**
 * Created by Mulan Lin('Snake1999') on 2016/12/3 21:28.
 * All rights reserved
 */
public interface World {

    BlockMixed getBlockAt(BlockPos pos);

    void setBlockAt(BlockPos pos, BlockMixed blockMixed);
}
