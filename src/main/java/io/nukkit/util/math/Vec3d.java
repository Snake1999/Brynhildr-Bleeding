package io.nukkit.util.math;

import javax.annotation.Nullable;

public class Vec3d {
    public static final Vec3d ZERO = new Vec3d(0.0D, 0.0D, 0.0D);

    /**
     * X coordinate of Vec3D
     */
    public final double xCoord;

    /**
     * Y coordinate of Vec3D
     */
    public final double yCoord;

    /**
     * Z coordinate of Vec3D
     */
    public final double zCoord;

    public Vec3d(double x, double y, double z) {
        if (x == -0.0D) {
            x = 0.0D;
        }

        if (y == -0.0D) {
            y = 0.0D;
        }

        if (z == -0.0D) {
            z = 0.0D;
        }

        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;
    }

    public Vec3d(Vec3i vector) {
        this((double) vector.getX(), (double) vector.getY(), (double) vector.getZ());
    }

    /**
     * Returns a new vector with the result of the specified vector minus this.
     */
    public Vec3d subtractReverse(Vec3d vec) {
        return new Vec3d(vec.xCoord - this.xCoord, vec.yCoord - this.yCoord, vec.zCoord - this.zCoord);
    }

    /**
     * Normalizes the vector to a length of 1 (except if it is the zero vector)
     */
    public Vec3d normalize() {
        double d = (double) MathHelper.sqrt_double(this.xCoord * this.xCoord + this.yCoord * this.yCoord + this.zCoord * this.zCoord);
        return d < 1.0E-4D ? ZERO : new Vec3d(this.xCoord / d, this.yCoord / d, this.zCoord / d);
    }

    public double dotProduct(Vec3d vec) {
        return this.xCoord * vec.xCoord + this.yCoord * vec.yCoord + this.zCoord * vec.zCoord;
    }

    public Vec3d subtract(Vec3d vec) {
        return this.subtract(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public Vec3d subtract(double x, double y, double z) {
        return this.addVector(-x, -y, -z);
    }

    public Vec3d add(Vec3d vec) {
        return this.addVector(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    /**
     * Adds the specified x,y,z vector components to this vector and returns the resulting vector. Does not change this
     * vector.
     */
    public Vec3d addVector(double x, double y, double z) {
        return new Vec3d(this.xCoord + x, this.yCoord + y, this.zCoord + z);
    }

    /**
     * Euclidean distance between this and the specified vector, returned as double.
     */
    public double distanceTo(Vec3d vec) {
        double xDistance = vec.xCoord - this.xCoord;
        double yDistance = vec.yCoord - this.yCoord;
        double zDistance = vec.zCoord - this.zCoord;
        return (double) MathHelper.sqrt_double(xDistance * xDistance + yDistance * yDistance + zDistance * zDistance);
    }

    /**
     * The square of the Euclidean distance between this and the specified vector.
     */
    public double squareDistanceTo(Vec3d vec) {
        double xDistance = vec.xCoord - this.xCoord;
        double yDistance = vec.yCoord - this.yCoord;
        double zDistance = vec.zCoord - this.zCoord;
        return xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;
    }

    public double squareDistanceTo(double x, double y, double z) {
        double xDistance = x - this.xCoord;
        double yDistance = y - this.yCoord;
        double zDistance = z - this.zCoord;
        return xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;
    }

    public Vec3d scale(double n) {
        return new Vec3d(this.xCoord * n, this.yCoord * n, this.zCoord * n);
    }

    /**
     * Returns the length of the vector.
     */
    public double lengthVector() {
        return (double) MathHelper.sqrt_double(this.xCoord * this.xCoord + this.yCoord * this.yCoord + this.zCoord * this.zCoord);
    }

    @Nullable

    /**
     * Returns a new vector with x value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     */
    public Vec3d getIntermediateWithXValue(Vec3d vec, double x) {
        double xDiff = vec.xCoord - this.xCoord;
        double yDiff = vec.yCoord - this.yCoord;
        double zDiff = vec.zCoord - this.zCoord;

        if (xDiff * xDiff < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d = (x - this.xCoord) / xDiff;
            return d >= 0.0D && d <= 1.0D ? new Vec3d(this.xCoord + xDiff * d, this.yCoord + yDiff * d, this.zCoord + zDiff * d) : null;
        }
    }

    @Nullable

    /**
     * Returns a new vector with y value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     */
    public Vec3d getIntermediateWithYValue(Vec3d vec, double y) {
        double xDiff = vec.xCoord - this.xCoord;
        double yDiff = vec.yCoord - this.yCoord;
        double zDiff = vec.zCoord - this.zCoord;

        if (yDiff * yDiff < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d = (y - this.yCoord) / yDiff;
            return d >= 0.0D && d <= 1.0D ? new Vec3d(this.xCoord + xDiff * d, this.yCoord + yDiff * d, this.zCoord + zDiff * d) : null;
        }
    }

    @Nullable

    /**
     * Returns a new vector with z value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     */
    public Vec3d getIntermediateWithZValue(Vec3d vec, double z) {
        double xDiff = vec.xCoord - this.xCoord;
        double yDiff = vec.yCoord - this.yCoord;
        double zDiff = vec.zCoord - this.zCoord;

        if (zDiff * zDiff < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d = (z - this.zCoord) / zDiff;
            return d >= 0.0D && d <= 1.0D ? new Vec3d(this.xCoord + xDiff * d, this.yCoord + yDiff * d, this.zCoord + zDiff * d) : null;
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Vec3d)) {
            return false;
        } else {
            Vec3d vec3d = (Vec3d) obj;
            return Double.compare(vec3d.xCoord, this.xCoord) == 0 && (Double.compare(vec3d.yCoord, this.yCoord) == 0 && Double.compare(vec3d.zCoord, this.zCoord) == 0);
        }
    }

    public int hashCode() {
        long j = Double.doubleToLongBits(this.xCoord);
        int i = (int) (j ^ j >>> 32);
        j = Double.doubleToLongBits(this.yCoord);
        i = 31 * i + (int) (j ^ j >>> 32);
        j = Double.doubleToLongBits(this.zCoord);
        i = 31 * i + (int) (j ^ j >>> 32);
        return i;
    }

    public String toString() {
        return "(" + this.xCoord + ", " + this.yCoord + ", " + this.zCoord + ")";
    }

    public Vec3d rotatePitch(float pitch) {
        float cos = MathHelper.cos(pitch);
        float sin = MathHelper.sin(pitch);
        double modX = this.xCoord;
        double modY = this.yCoord * (double) cos + this.zCoord * (double) sin;
        double modZ = this.zCoord * (double) cos - this.yCoord * (double) sin;
        return new Vec3d(modX, modY, modZ);
    }

    public Vec3d rotateYaw(float yaw) {
        float cos = MathHelper.cos(yaw);
        float sin = MathHelper.sin(yaw);
        double modX = this.xCoord * (double) cos + this.zCoord * (double) sin;
        double modY = this.yCoord;
        double modZ = this.zCoord * (double) cos - this.xCoord * (double) sin;
        return new Vec3d(modX, modY, modZ);
    }
}
