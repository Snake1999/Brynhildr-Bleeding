package io.nukkit.block;

/**
 * Created by Mulan Lin('Snake1999') on 2016/12/3 17:42.
 * All rights reserved
 */
public interface BlockMixed {

    BlockMixed MIXED_AIR = ofBlockIdentifier(BlockIdentifier.IDENTIFIER_AIR);

    BlockIdentifier getBlockIdentifier();

    BlockEntity getBlockEntity();

    static BlockMixed ofBlockIdentifier(BlockIdentifier id) {
        return new BlockMixed() {
            @Override public BlockIdentifier getBlockIdentifier() {return id;}
            @Override public BlockEntity getBlockEntity() {return null;}
        };
    }
}
