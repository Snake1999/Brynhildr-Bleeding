package io.nukkit.util.math;


import io.nukkit.nbt.NBTTagFloat;
import io.nukkit.nbt.NBTTagList;

public class Rotations {
    /**
     * Rotation on the X axis
     */
    protected final float x;

    /**
     * Rotation on the Y axis
     */
    protected final float y;

    /**
     * Rotation on the Z axis
     */
    protected final float z;

    public Rotations(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Rotations(NBTTagList nbt) {
        this.x = nbt.getFloatAt(0);
        this.y = nbt.getFloatAt(1);
        this.z = nbt.getFloatAt(2);
    }

    public NBTTagList writeToNBT() {
        NBTTagList nbtTagList = new NBTTagList();
        nbtTagList.appendTag(new NBTTagFloat(this.x));
        nbtTagList.appendTag(new NBTTagFloat(this.y));
        nbtTagList.appendTag(new NBTTagFloat(this.z));
        return nbtTagList;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Rotations)) {
            return false;
        } else {
            Rotations rotations = (Rotations) obj;
            return this.x == rotations.x && this.y == rotations.y && this.z == rotations.z;
        }
    }

    /**
     * Gets the X axis rotation
     */
    public float getX() {
        return this.x;
    }

    /**
     * Gets the Y axis rotation
     */
    public float getY() {
        return this.y;
    }

    /**
     * Gets the Z axis rotation
     */
    public float getZ() {
        return this.z;
    }
}
