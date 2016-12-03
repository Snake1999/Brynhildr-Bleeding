package io.nukkit.util;

import io.nukkit.block.BlockIdentifier;

import java.util.Objects;

/**
 * Created by Mulan Lin('Snake1999') on 2016/12/3 21:35.
 * All rights reserved
 */
public final class Identifiers {
    private Identifiers() {throw new AssertionError("No instances!");}

    public static final BlockIdentifier BLOCK_AIR = ofIdOnly(0);
    public static final BlockIdentifier BLOCK_FENCE_GATE_OAK = ofIdOnly(107);
    public static final BlockIdentifier BLOCK_FENCE_GATE_SPRUCE = ofIdOnly(183);
    public static final BlockIdentifier BLOCK_FENCE_GATE_BIRCH = ofIdOnly(184);
    public static final BlockIdentifier BLOCK_FENCE_GATE_JUNGLE = ofIdOnly(185);
    public static final BlockIdentifier BLOCK_FENCE_GATE_DARK_OAK = ofIdOnly(186);
    public static final BlockIdentifier BLOCK_FENCE_GATE_ACACIA = ofIdOnly(187);

    public static boolean isFenceGateBlock(BlockIdentifier id) {
        Objects.requireNonNull(id);
        return BLOCK_FENCE_GATE_OAK.equals(id) || BLOCK_FENCE_GATE_SPRUCE.equals(id) ||
                BLOCK_FENCE_GATE_BIRCH.equals(id) || BLOCK_FENCE_GATE_JUNGLE.equals(id) ||
                BLOCK_FENCE_GATE_DARK_OAK.equals(id) || BLOCK_FENCE_GATE_ACACIA.equals(id);
    }

    public static BlockIdentifier withIntegerMeta(BlockIdentifier id, int integerMeta) {
        return ofIdMeta(id.asIntegerId(), integerMeta);
    }

    private static BlockIdentifier ofIdMeta(int integerId, int integerMeta) {
        return new BlockIdentifier() {
            @Override public int asIntegerId() {return integerId;}
            @Override public int asIntegerMeta() {return integerMeta;}
            @Override public boolean equals(Object obj) {
                if (obj instanceof BlockIdentifier) {
                    BlockIdentifier i = (BlockIdentifier) obj;
                    return i.asIntegerId() == integerId && i.asIntegerMeta() == integerMeta;
                } else return false;
            }
        };
    }

    private static BlockIdentifier ofIdOnly(int integerId) {
        return new BlockIdentifier() {
            @Override public int asIntegerId() {return integerId;}
            @Override public int asIntegerMeta() {return 0;}
            @Override public boolean equals(Object obj) {
                if (obj instanceof BlockIdentifier) {
                    BlockIdentifier i = (BlockIdentifier) obj;
                    return i.asIntegerId() == integerId; // only compare id
                } else return false;
            }
        };
    }

}
