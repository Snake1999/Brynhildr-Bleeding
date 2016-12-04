package io.nukkit.util;

import io.nukkit.block.BlockIdentifier;
import io.nukkit.function.Gates;

/**
 * Created by Mulan Lin('Snake1999') on 2016/12/3 21:35.
 * All rights reserved
 */
public final class Identifiers {
    private Identifiers() {throw new AssertionError("No instances!");}

    public static final BlockIdentifier BLOCK_AIR = ofIdOnly(0);
    public static final BlockIdentifier BLOCK_DOOR_OAK = ofIdOnly(64);
    public static final BlockIdentifier BLOCK_TRAPDOOR_OAK = ofIdOnly(96);
    public static final BlockIdentifier BLOCK_FENCE_GATE_OAK = ofIdOnly(107);
    public static final BlockIdentifier BLOCK_TRAPDOOR_IRON = ofIdOnly(167);
    public static final BlockIdentifier BLOCK_FENCE_GATE_SPRUCE = ofIdOnly(183);
    public static final BlockIdentifier BLOCK_FENCE_GATE_BIRCH = ofIdOnly(184);
    public static final BlockIdentifier BLOCK_FENCE_GATE_JUNGLE = ofIdOnly(185);
    public static final BlockIdentifier BLOCK_FENCE_GATE_DARK_OAK = ofIdOnly(186);
    public static final BlockIdentifier BLOCK_FENCE_GATE_ACACIA = ofIdOnly(187);
    public static final BlockIdentifier BLOCK_DOOR_SPRUCE = ofIdOnly(193);
    public static final BlockIdentifier BLOCK_DOOR_BIRCH = ofIdOnly(194);
    public static final BlockIdentifier BLOCK_DOOR_JUNGLE = ofIdOnly(195);
    public static final BlockIdentifier BLOCK_DOOR_ACACIA = ofIdOnly(196);
    public static final BlockIdentifier BLOCK_DOOR_DARK_OAK = ofIdOnly(197);

    @SuppressWarnings("unused")
    public static boolean isFenceGateBlock(BlockIdentifier id) {
        return Gates.isFenceGateBlock(id);
    }

    @SuppressWarnings("unused")
    public static boolean isDoorBlock(BlockIdentifier id) {
        return Gates.isDoorBlock(id);
    }

    @SuppressWarnings("unused")
    public static boolean isTrapdoorBlock(BlockIdentifier id) {
        return Gates.isTrapdoorBlock(id);
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
