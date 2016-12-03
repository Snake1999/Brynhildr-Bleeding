package io.nukkit.world;

import io.nukkit.block.BlockMixed;
import io.nukkit.util.math.BlockPos;

/**
 * Created by Mulan Lin('Snake1999') on 2016/12/3 17:37.
 * All rights reserved
 */
public interface WorldSystem {

    World BLACK_HOLE = new World() {
        @Override public BlockMixed getBlockAt(BlockPos pos) {return BlockMixed.MIXED_AIR;}
        @Override public void setBlockAt(BlockPos pos, BlockMixed blockMixed) {}
    };

    // Nullable. Might cause problems
    World getWorld(WorldUnique unique);

    World getWorldOr(WorldUnique unique, World defaultValue);

    default World getOrBlackHole(WorldUnique unique){
        return getWorldOr(unique, BLACK_HOLE);
    }

}
