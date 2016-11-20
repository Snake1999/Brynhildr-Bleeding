package io.nukkit.util.math;

public class Vec4b {
    private byte type;
    private byte x;
    private byte y;
    private byte rotation;

    public Vec4b(byte typeIn, byte xIn, byte yIn, byte rotationIn) {
        this.type = typeIn;
        this.x = xIn;
        this.y = yIn;
        this.rotation = rotationIn;
    }

    public Vec4b(Vec4b vec) {
        this.type = vec.type;
        this.x = vec.x;
        this.y = vec.y;
        this.rotation = vec.rotation;
    }

    public byte getType() {
        return this.type;
    }

    public byte getX() {
        return this.x;
    }

    public byte getY() {
        return this.y;
    }

    public byte getRotation() {
        return this.rotation;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Vec4b)) {
            return false;
        } else {
            Vec4b vec4b = (Vec4b) obj;
            return this.type == vec4b.type && (this.rotation == vec4b.rotation && (this.x == vec4b.x && this.y == vec4b.y));
        }
    }

    public int hashCode() {
        int i = this.type;
        i = 31 * i + this.x;
        i = 31 * i + this.y;
        i = 31 * i + this.rotation;
        return i;
    }
}
