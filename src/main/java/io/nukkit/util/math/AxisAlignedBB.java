package io.nukkit.util.math;

import com.google.common.annotations.VisibleForTesting;
import io.nukkit.util.EnumFacing;

import javax.annotation.Nullable;

public class AxisAlignedBB {
    public final double minX;
    public final double minY;
    public final double minZ;
    public final double maxX;
    public final double maxY;
    public final double maxZ;

    public AxisAlignedBB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public AxisAlignedBB(BlockPos pos) {
        this((double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), (double) (pos.getX() + 1), (double) (pos.getY() + 1), (double) (pos.getZ() + 1));
    }

    public AxisAlignedBB(BlockPos minPos, BlockPos maxPos) {
        this((double) minPos.getX(), (double) minPos.getY(), (double) minPos.getZ(), (double) maxPos.getX(), (double) maxPos.getY(), (double) maxPos.getZ());
    }

    public AxisAlignedBB setMaxY(double maxY) {
        return new AxisAlignedBB(this.minX, this.minY, this.minZ, this.maxX, maxY, this.maxZ);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof AxisAlignedBB)) {
            return false;
        } else {
            AxisAlignedBB bb = (AxisAlignedBB) obj;
            return Double.compare(bb.minX, this.minX) == 0 && (Double.compare(bb.minY, this.minY) == 0 && (Double.compare(bb.minZ, this.minZ) == 0 && (Double.compare(bb.maxX, this.maxX) == 0 && (Double.compare(bb.maxY, this.maxY) == 0 && Double.compare(bb.maxZ, this.maxZ) == 0))));
        }
    }

    public int hashCode() {
        long i = Double.doubleToLongBits(this.minX);
        int j = (int) (i ^ i >>> 32);
        i = Double.doubleToLongBits(this.minY);
        j = 31 * j + (int) (i ^ i >>> 32);
        i = Double.doubleToLongBits(this.minZ);
        j = 31 * j + (int) (i ^ i >>> 32);
        i = Double.doubleToLongBits(this.maxX);
        j = 31 * j + (int) (i ^ i >>> 32);
        i = Double.doubleToLongBits(this.maxY);
        j = 31 * j + (int) (i ^ i >>> 32);
        i = Double.doubleToLongBits(this.maxZ);
        j = 31 * j + (int) (i ^ i >>> 32);
        return j;
    }

    /**
     * Adds a coordinate to the bounding box, extending it if the point lies outside the current ranges.
     */
    public AxisAlignedBB addCoord(double x, double y, double z) {
        double minX = this.minX;
        double minY = this.minY;
        double minZ = this.minZ;
        double maxX = this.maxX;
        double maxY = this.maxY;
        double maxZ = this.maxZ;

        if (x < 0.0D) {
            minX += x;
        } else if (x > 0.0D) {
            maxX += x;
        }

        if (y < 0.0D) {
            minY += y;
        } else if (y > 0.0D) {
            maxY += y;
        }

        if (z < 0.0D) {
            minZ += z;
        } else if (z > 0.0D) {
            maxZ += z;
        }

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Creates a new bounding box that has been expanded. If negative values are used, it will shrink.
     */
    public AxisAlignedBB expand(double x, double y, double z) {
        double minX = this.minX - x;
        double minY = this.minY - y;
        double minZ = this.minZ - z;
        double maxX = this.maxX + x;
        double maxY = this.maxY + y;
        double maxZ = this.maxZ + z;
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public AxisAlignedBB expandXyz(double value) {
        return this.expand(value, value, value);
    }

    public AxisAlignedBB union(AxisAlignedBB other) {
        double minX = Math.min(this.minX, other.minX);
        double minY = Math.min(this.minY, other.minY);
        double minZ = Math.min(this.minZ, other.minZ);
        double maxX = Math.max(this.maxX, other.maxX);
        double maxY = Math.max(this.maxY, other.maxY);
        double maxZ = Math.max(this.maxZ, other.maxZ);
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Offsets the current bounding box by the specified amount.
     */
    public AxisAlignedBB offset(double x, double y, double z) {
        return new AxisAlignedBB(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    public AxisAlignedBB offset(BlockPos pos) {
        return new AxisAlignedBB(this.minX + (double) pos.getX(), this.minY + (double) pos.getY(), this.minZ + (double) pos.getZ(), this.maxX + (double) pos.getX(), this.maxY + (double) pos.getY(), this.maxZ + (double) pos.getZ());
    }

    /**
     * if instance and the argument bounding boxes overlap in the Y and Z dimensions, calculate the offset between them
     * in the X dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    public double calculateXOffset(AxisAlignedBB other, double offsetX) {
        if (other.maxY > this.minY && other.minY < this.maxY && other.maxZ > this.minZ && other.minZ < this.maxZ) {
            if (offsetX > 0.0D && other.maxX <= this.minX) {
                double offset = this.minX - other.maxX;

                if (offset < offsetX) {
                    offsetX = offset;
                }
            } else if (offsetX < 0.0D && other.minX >= this.maxX) {
                double offset = this.maxX - other.minX;

                if (offset > offsetX) {
                    offsetX = offset;
                }
            }

            return offsetX;
        } else {
            return offsetX;
        }
    }

    /**
     * if instance and the argument bounding boxes overlap in the X and Z dimensions, calculate the offset between them
     * in the Y dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    public double calculateYOffset(AxisAlignedBB other, double offsetY) {
        if (other.maxX > this.minX && other.minX < this.maxX && other.maxZ > this.minZ && other.minZ < this.maxZ) {
            if (offsetY > 0.0D && other.maxY <= this.minY) {
                double offset = this.minY - other.maxY;

                if (offset < offsetY) {
                    offsetY = offset;
                }
            } else if (offsetY < 0.0D && other.minY >= this.maxY) {
                double offset = this.maxY - other.minY;

                if (offset > offsetY) {
                    offsetY = offset;
                }
            }

            return offsetY;
        } else {
            return offsetY;
        }
    }

    /**
     * if instance and the argument bounding boxes overlap in the Y and X dimensions, calculate the offset between them
     * in the Z dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    public double calculateZOffset(AxisAlignedBB other, double offsetZ) {
        if (other.maxX > this.minX && other.minX < this.maxX && other.maxY > this.minY && other.minY < this.maxY) {
            if (offsetZ > 0.0D && other.maxZ <= this.minZ) {
                double offset = this.minZ - other.maxZ;

                if (offset < offsetZ) {
                    offsetZ = offset;
                }
            } else if (offsetZ < 0.0D && other.minZ >= this.maxZ) {
                double offset = this.maxZ - other.minZ;

                if (offset > offsetZ) {
                    offsetZ = offset;
                }
            }

            return offsetZ;
        } else {
            return offsetZ;
        }
    }

    /**
     * Checks if the bounding box intersects with another.
     */
    public boolean intersectsWith(AxisAlignedBB other) {
        return this.intersects(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY && this.minZ < maxZ && this.maxZ > minZ;
    }

    /**
     * Returns if the supplied Vec3D is completely inside the bounding box
     */
    public boolean isVecInside(Vec3d vec) {
        return (vec.xCoord > this.minX && vec.xCoord < this.maxX) && ((vec.yCoord > this.minY && vec.yCoord < this.maxY) && (vec.zCoord > this.minZ && vec.zCoord < this.maxZ));
    }

    /**
     * Returns the average length of the edges of the bounding box.
     */
    public double getAverageEdgeLength() {
        double xDiff = this.maxX - this.minX;
        double yDiff = this.maxY - this.minY;
        double zDiff = this.maxZ - this.minZ;
        return (xDiff + yDiff + zDiff) / 3.0D;
    }

    public AxisAlignedBB contract(double value) {
        return this.expandXyz(-value);
    }

    @Nullable
    public RayTraceResult calculateIntercept(Vec3d vecA, Vec3d vecB) {
        Vec3d vec = this.collideWithXPlane(this.minX, vecA, vecB);
        EnumFacing enumfacing = EnumFacing.WEST;
        Vec3d temp = this.collideWithXPlane(this.maxX, vecA, vecB);

        if (temp != null && this.isClosest(vecA, vec, temp)) {
            vec = temp;
            enumfacing = EnumFacing.EAST;
        }

        temp = this.collideWithYPlane(this.minY, vecA, vecB);

        if (temp != null && this.isClosest(vecA, vec, temp)) {
            vec = temp;
            enumfacing = EnumFacing.DOWN;
        }

        temp = this.collideWithYPlane(this.maxY, vecA, vecB);

        if (temp != null && this.isClosest(vecA, vec, temp)) {
            vec = temp;
            enumfacing = EnumFacing.UP;
        }

        temp = this.collideWithZPlane(this.minZ, vecA, vecB);

        if (temp != null && this.isClosest(vecA, vec, temp)) {
            vec = temp;
            enumfacing = EnumFacing.NORTH;
        }

        temp = this.collideWithZPlane(this.maxZ, vecA, vecB);

        if (temp != null && this.isClosest(vecA, vec, temp)) {
            vec = temp;
            enumfacing = EnumFacing.SOUTH;
        }

        return vec == null ? null : new RayTraceResult(vec, enumfacing);
    }

    @VisibleForTesting
    boolean isClosest(Vec3d from, @Nullable Vec3d vecA, Vec3d vecB) {
        return vecA == null || from.squareDistanceTo(vecB) < from.squareDistanceTo(vecA);
    }

    @Nullable
    @VisibleForTesting
    Vec3d collideWithXPlane(double x, Vec3d vecA, Vec3d vecB) {
        Vec3d vec3d = vecA.getIntermediateWithXValue(vecB, x);
        return vec3d != null && this.intersectsWithYZ(vec3d) ? vec3d : null;
    }

    @Nullable
    @VisibleForTesting
    Vec3d collideWithYPlane(double y, Vec3d vecA, Vec3d vecB) {
        Vec3d vec3d = vecA.getIntermediateWithYValue(vecB, y);
        return vec3d != null && this.intersectsWithXZ(vec3d) ? vec3d : null;
    }

    @Nullable
    @VisibleForTesting
    Vec3d collideWithZPlane(double z, Vec3d vecA, Vec3d vecB) {
        Vec3d vec3d = vecA.getIntermediateWithZValue(vecB, z);
        return vec3d != null && this.intersectsWithXY(vec3d) ? vec3d : null;
    }

    @VisibleForTesting
    public boolean intersectsWithYZ(Vec3d vec) {
        return vec.yCoord >= this.minY && vec.yCoord <= this.maxY && vec.zCoord >= this.minZ && vec.zCoord <= this.maxZ;
    }

    @VisibleForTesting
    public boolean intersectsWithXZ(Vec3d vec) {
        return vec.xCoord >= this.minX && vec.xCoord <= this.maxX && vec.zCoord >= this.minZ && vec.zCoord <= this.maxZ;
    }

    @VisibleForTesting
    public boolean intersectsWithXY(Vec3d vec) {
        return vec.xCoord >= this.minX && vec.xCoord <= this.maxX && vec.yCoord >= this.minY && vec.yCoord <= this.maxY;
    }

    public String toString() {
        return "box[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }
}
