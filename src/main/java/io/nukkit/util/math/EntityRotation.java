package io.nukkit.util.math;

/**
 * Describes the rotation of entities.
 * <p>
 * For pitch:<br>
 * <table>
 * <tr><td>Pitch<br>(in degree)</td><td>Action  </tr>
 * <tr><td>P = -90.0        </td><td>Facing sky     </tr>
 * <tr><td>-90.0 < P < 0.0  </td><td>Heading up     </tr>
 * <tr><td>P = 0.0          </td><td>Facing horizon </tr>
 * <tr><td>0.0 < P < 90.0   </td><td>Heading down   </tr>
 * <tr><td>P = 90.0         </td><td>Facing earth   </tr>
 * </table>
 * When looking straight at the horizon, pitch = 0.0<br>
 * While looking up, pitch decreases. Looking up to sky, pitch reaches its min value -90.0<br>
 * While looking down, pitch increases. Looking down to ground, pitch reaches its max value 90.0<br>
 * <p>
 * For yaw:
 * <table>
 * <tr><td>Yaw<br>(in degree)</td><td>As Direction  </td><td>As Axis       </td></tr>
 * <tr><td>0.0      </td><td>Facing south  </td><td>Facing Positive Z  </td></tr>
 * <tr><td>90.0     </td><td>Facing west   </td><td>Facing Negative X  </td></tr>
 * <tr><td>180.0    </td><td>Facing north  </td><td>Facing Negative Z  </td></tr>
 * <tr><td>270.0    </td><td>Facing east   </td><td>Facing Positive X  </td></tr>
 * </table>
 *
 * @see io.nukkit.block.BlockFace
 */
public interface EntityRotation {

    // float because mc-pe client send float in packets.

    float getYawInDegree();

    float getPitchInDegree();

}
