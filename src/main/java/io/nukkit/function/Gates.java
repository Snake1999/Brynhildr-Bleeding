package io.nukkit.function;

import io.nukkit.block.BlockIdentifier;
import io.nukkit.block.BlockMixed;
import io.nukkit.util.math.BlockPos;
import io.nukkit.world.WorldSystem;
import io.nukkit.world.WorldUnique;

import java.util.Objects;

import static io.nukkit.util.Identifiers.*;

/**
 * Tool for operating doors and fence gates.
 * <p>Details on meta value:
 * <table border="1"><tr><td>Door Type</td><td>Meaning of Meta Value</td></tr>
 * <tr><td>Door</td><td>
 * <table>
 * <tr><td>Bit       </td><td>Meaning<br>(Upper part of door)</td><td>Meaning<br>(Lower part of door)</td></tr>
 * <tr><td>bit 4     </td><td>1 to identify 'upper'</td><td>0 to identify 'lower'</td></tr>
 * <tr><td>bit 3     </td><td>(Unused)</td><td>0 if the entire door is closed, 1 if open</td></tr>
 * <tr><td>bit 2     </td><td>0 if not powered, 1 if powered</td><td>bits 2 & 1 together to describe facing</td></tr>
 * <tr><td>bit 1     </td><td>0 if hinge is on the left(default)<br>1 if on the right</td><td>bits 2 & 1 together to describe facing</td></tr>
 * </table>
 * Notes on facing on meta for lower part of doors:
 * <table>
 * <tr><td>Bits 2&1<br>(in binary)</td><td>Door direction</td><td>As Axis</td></tr>
 * <tr><td>00       </td><td>Facing east  </td><td>Facing Positive X</td></tr>
 * <tr><td>01       </td><td>Facing south </td><td>Facing Positive Z</td></tr>
 * <tr><td>10       </td><td>Facing west  </td><td>Facing Negative X</td></tr>
 * <tr><td>11       </td><td>Facing north </td><td>Facing Negative Z</td></tr>
 * </table>
 * </td></tr>
 * <tr><td>Fence Gate</td><td>
 * <table>
 * <tr><td>Bit       </td><td>Meaning</td></tr>
 * <tr><td>bit 2     </td><td>1 for opened, 0 for closed </td></tr>
 * <tr><td>bit 1,0   </td><td>Bit of fence facing.</td></tr>
 * </table>
 * Notes on bits of fence facing:
 * <table>
 * <tr><td>Value<br>(in binary)</td><td>Yaw<br>(in degree)</td><td>Fence Gate Direction</td><td>As Axis</td></tr>
 * <tr><td>00    </td><td>0.0     </td><td>Facing south  </td><td>Facing Positive Z  </td></tr>
 * <tr><td>01    </td><td>90.0    </td><td>Facing west   </td><td>Facing Negative X  </td></tr>
 * <tr><td>10    </td><td>180.0   </td><td>Facing north  </td><td>Facing Negative Z  </td></tr>
 * <tr><td>11    </td><td>270.0   </td><td>Facing east   </td><td>Facing Positive X  </td></tr>
 * </table>
 * </td></tr>
 * <tr><td>Trapdoor</td><td>
 * <table>
 * <tr><td>Bit      </td><td>Meaning</td></tr>
 * <tr><td>bit 4    </td><td>1 for trapdoor at top half of block, 0 for bottom half</td></tr>
 * <tr><td>bit 3    </td><td>1 for trapdoor is open, 0 for closed</td></tr>
 * <tr><td>bits 2&1 </td><td>Describe which side of block this trapdoor is hanging on</td></tr>
 * </table>
 * Notes on bits of trapdoor side:
 * <table>
 * <tr><td>Bits 2&1<br>(in binary)</td><td>Meaning</td></tr>
 * <tr><td>00 </td><td>Trapdoor on the south side of a block</td></tr>
 * <tr><td>01 </td><td>Trapdoor on the north side of a block</td></tr>
 * <tr><td>10 </td><td>Trapdoor on the east side of a block</td></tr>
 * <tr><td>11 </td><td>Trapdoor on the west side of a block</td></tr>
 * </table>
 * </td>
 * </table>
 * Data from http://minecraft.gamepedia.com/
 *
 * @see io.nukkit.util.math.EntityRotation
 * @see io.nukkit.block.BlockFace
 */
public final class Gates {
    private Gates() {throw new AssertionError("No instances!");}

    public static LazyGate lazyGateAt(WorldSystem sys, WorldUnique world, BlockPos pos) {
        return new LazyGate() {
            private BlockIdentifier blockId() {return sys.getOrBlackHole(world).getBlockAt(getBlockPos()).getBlockIdentifier();}
            private void set(BlockIdentifier id) {sys.getOrBlackHole(world).setBlockAt(getBlockPos(), BlockMixed.ofBlockIdentifier(id));}
            @Override public void setGateOpened(boolean value) {
                BlockIdentifier id = blockId();
                if(isFenceGateBlock(id))
                    if(value != isFenceGateOpened(id.asIntegerMeta())) id = id.withIntegerMeta(toggleFenceGateMeta(id.asIntegerMeta()));
                // TODO: 2016/12/3 Door, trapdoor
                set(id);
            }
            @Override public boolean isGateOpened() {
                BlockIdentifier id = blockId();
                if(isFenceGateBlock(id)) return isFenceGateOpened(id.asIntegerMeta());
                // TODO: 2016/12/3 Door, trapdoor
                return false;
            }
            @Override public boolean isValid() {
                BlockIdentifier id = blockId();
                return isFenceGateBlock(id) || isDoorBlock(id);
            } // TODO: 2016/12/3 trapdoor
            @Override public WorldUnique getWorldUnique() {return world;}
            @Override public BlockPos getBlockPos() {return pos;}
        };
    }

    public static void setGateOpenedAt(WorldSystem sys, WorldUnique world, BlockPos pos, boolean value) {
        LazyGate g = lazyGateAt(sys, world, pos);
        if (!g.isValid()) return;
        if (value != g.isGateOpened()) g.setGateOpened(value);
    }

    public static boolean isGateOpenedAt(WorldSystem sys, WorldUnique world, BlockPos pos) {
        LazyGate g = lazyGateAt(sys, world, pos);
        return g.isValid() && g.isGateOpened();
    }

    public static void toggleOpenedAt(WorldSystem sys, WorldUnique world, BlockPos pos) {
        LazyGate g = lazyGateAt(sys, world, pos);
        g.toggleOpened();
    }

    public static boolean isFenceGateBlock(BlockIdentifier id) {
        Objects.requireNonNull(id);
        return BLOCK_FENCE_GATE_OAK.equals(id) || BLOCK_FENCE_GATE_SPRUCE.equals(id) ||
                BLOCK_FENCE_GATE_BIRCH.equals(id) || BLOCK_FENCE_GATE_JUNGLE.equals(id) ||
                BLOCK_FENCE_GATE_DARK_OAK.equals(id) || BLOCK_FENCE_GATE_ACACIA.equals(id);
    }

    public static boolean isDoorBlock(BlockIdentifier id) {
        Objects.requireNonNull(id);
        return BLOCK_DOOR_OAK.equals(id) || BLOCK_DOOR_SPRUCE.equals(id) ||
                BLOCK_DOOR_BIRCH.equals(id) || BLOCK_DOOR_JUNGLE.equals(id) ||
                BLOCK_DOOR_DARK_OAK.equals(id) || BLOCK_DOOR_ACACIA.equals(id);
    }

    public static boolean isTrapdoorBlock(BlockIdentifier id) {
        Objects.requireNonNull(id);
        return BLOCK_TRAPDOOR_WOOD.equals(id) || BLOCK_TRAPDOOR_IRON.equals(id);
    }

    /*----------* Internal Part *----------* Do NOT attempt to call or use in plugins *----------*/

    private static int DOOR_UPPER_LOWER_BIT = 0b1000;
    private static int DOOR_OPEN_CLOSE_BIT  = 0b0100;
    private static int DOOR_FACING_BITS     = 0b0011;
    private static int DOOR_POWERED_BIT     = 0b0010;
    private static int DOOR_LEFT_RIGHT_BIT  = 0b0001;

    private static int FENCE_OPEN_CLOSE_BIT = 0b100;
    private static int FENCE_DIRECTION_BITS = 0b011;

    // TODO: 2016/12/4 Trapdoor

    private static int toggleDoorMeta(int integerMeta) {
        int upperLowerBit = integerMeta & DOOR_UPPER_LOWER_BIT;
        if(upperLowerBit > 0) return integerMeta; // Upper part, no need for toggle
        int openCloseBit = integerMeta & DOOR_OPEN_CLOSE_BIT;
        int facingBits = integerMeta & DOOR_FACING_BITS;
        openCloseBit = ~openCloseBit;
        return upperLowerBit | openCloseBit | facingBits;
    }
    private static boolean isLowerDoorOpened(int integerMeta) {
        //At first, the door must be lower part
        return (integerMeta & DOOR_OPEN_CLOSE_BIT) > 0;
    }
    private static void rebuildDoor(WorldUnique wu, BlockPos pos) {
        // TODO: 2016/12/4 finish door part
    }

    private static int toggleFenceGateMeta(int integerMeta, float rotation) {
        int openBits = integerMeta & FENCE_OPEN_CLOSE_BIT;
        openBits = ~openBits; // toggle the open bit
        int directionBits = rotationToDirectionBits(integerMeta & FENCE_DIRECTION_BITS, rotation);
        return openBits | directionBits;
    }

    private static boolean isFenceGateOpened(int integerMeta) {
        return (integerMeta & FENCE_OPEN_CLOSE_BIT) > 0;
    }

    private static int toggleFenceGateMeta(int integerMeta) {
        int openBits = integerMeta & FENCE_OPEN_CLOSE_BIT;
        openBits = ~openBits; // toggle the open bit
        int directionBits = integerMeta & FENCE_DIRECTION_BITS;
        return openBits | directionBits;
    }

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
