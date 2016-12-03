package io.nukkit.function;

import io.nukkit.block.BlockIdentifier;
import io.nukkit.block.BlockMixed;
import io.nukkit.util.Identifiers;
import io.nukkit.util.math.BlockPos;
import io.nukkit.world.WorldSystem;
import io.nukkit.world.WorldUnique;

/**
 * Created by Mulan Lin('Snake1999') on 2016/12/3 17:14.
 * All rights reserved
 */
public final class Gates {

    static LazyGate lazyGateAt(WorldSystem sys, WorldUnique world, BlockPos pos) {
        return new LazyGate() {
            private BlockIdentifier blockId() {return sys.getOrBlackHole(world).getBlockAt(getBlockPos()).getBlockIdentifier();}
            private void set(BlockIdentifier id) {sys.getOrBlackHole(world).setBlockAt(getBlockPos(), BlockMixed.ofBlockIdentifier(id));}
            @Override public void setOpened(boolean value) {
                BlockIdentifier id = blockId();
                if(Identifiers.isFenceGateBlock(id))
                    if(value != isFenceGateOpened(id.asIntegerMeta())) id = id.withIntegerMeta(toggleFenceGateMeta(id.asIntegerMeta()));
                // TODO: 2016/12/3 Door
                set(id);
            }
            @Override public boolean isOpened() {
                BlockIdentifier id = blockId();
                if(Identifiers.isFenceGateBlock(id)) return isFenceGateOpened(id.asIntegerMeta());
                // TODO: 2016/12/3 Door
                return false;
            }
            @Override public boolean isValid() {return Identifiers.isFenceGateBlock(blockId());} // TODO: 2016/12/3 Door
            @Override public WorldUnique getWorldUnique() {return world;}
            @Override public BlockPos getBlockPos() {return pos;}
        };
    }

    private static void rebuildDoor(WorldUnique wu, BlockPos pos) {

    }
    private static boolean isDoorOpened(int integerMeta) {

        return false; //todo
    }
    private static int openDoorMeta(int integerMeta) {
        return 0; //todo
    }


    /**
     * Notes on fence meta:
     * <table>
     * <tr><td>Bit       </td><td>Meaning</td></tr>
     * <tr><td>bit 2     </td><td>1 for opened, 0 for closed </td></tr>
     * <tr><td>bit 1,0   </td><td>Bit of fence facing.</td></tr>
     * </table>
     * <p>
     * Notes on Bit of fence facing:
     * <table>
     * <tr><td>Value<br>(in binary)</td><td>Yaw<br>(in degree)</td><td>As Direction</td><td>As Axis</td></tr>
     * <tr><td>00    </td><td>0.0     </td><td>Facing south  </td><td>Facing Positive Z  </td></tr>
     * <tr><td>01    </td><td>90.0    </td><td>Facing west   </td><td>Facing Negative X  </td></tr>
     * <tr><td>10    </td><td>180.0   </td><td>Facing north  </td><td>Facing Negative Z  </td></tr>
     * <tr><td>11    </td><td>270.0   </td><td>Facing east   </td><td>Facing Positive X  </td></tr>
     * </table>
     *
     * @see io.nukkit.util.math.EntityRotation
     * @see io.nukkit.block.BlockFace
     */
    private static int toggleFenceGateMeta(int integerMeta, float rotation) {
        int openBits = integerMeta & 0b100;
        openBits = ~openBits; // toggle the open bit
        int directionBits = rotationToDirectionBits(integerMeta & 0b11, rotation);
        return openBits | directionBits;
    }
    /**
     * @see #toggleFenceGateMeta
     */
    private static boolean isFenceGateOpened(int integerMeta) {
        return (integerMeta & 0b100) > 0;
    }
    /**
     * @see #toggleFenceGateMeta
     */
    private static int toggleFenceGateMeta(int integerMeta) {
        int openBits = integerMeta & 0b100;
        openBits = ~openBits; // toggle the open bit
        int directionBits = integerMeta & 0b11;
        return openBits | directionBits;
    }
    /**
     * @see #toggleFenceGateMeta
     */
    private static int rotationToDirectionBits(int originMeta, float rotationInDegree) {
        if (rotationInDegree < 0) rotationInDegree += 360.0;
        if (originMeta == 0b00 || originMeta == 0b10) { // 0.0 || 180.0
            if(rotationInDegree >= 270.0 || rotationInDegree < 90.0) {
                return 0b00; //[-90.0, 90.0) -> 0.0
            } else {
                return 0b10; //[90.0, 270.0) -> 180.0
            }
        } else {
            if(rotationInDegree >= 0.0 || rotationInDegree < 180.0) {
                return 0b01; //[0.0, 180.0) -> 90.0
            } else {
                return 0b11; //[180.0, 360.0) -> 270.0
            }
        }
    }

}
