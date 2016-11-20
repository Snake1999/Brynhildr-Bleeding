package io.nukkit.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class NBTTag {
    public static final String[] NBT_TYPES = new String[]{"END", "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BYTE[]", "STRING", "LIST", "COMPOUND", "INT[]"};

    public static final byte TAG_END = 0;
    public static final byte TAG_BYTE = 1;
    public static final byte TAG_SHORT = 2;
    public static final byte TAG_INT = 3;
    public static final byte TAG_LONG = 4;
    public static final byte TAG_FLOAT = 5;
    public static final byte TAG_DOUBLE = 6;
    public static final byte TAG_BYTE_ARRAY = 7;
    public static final byte TAG_STRING = 8;
    public static final byte TAG_LIST = 9;
    public static final byte TAG_COMPOUND = 10;
    public static final byte TAG_INT_ARRAY = 11;

    /**
     * Write the actual data contents of the tag, implemented in NBT extension classes
     */
    abstract void write(DataOutput output) throws IOException;

    abstract void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException;

    public abstract String toString();

    /**
     * Gets the type byte for the tag.
     */
    public abstract byte getId();

    /**
     * Creates a new NBTTag object that corresponds with the passed in id.
     */
    protected static NBTTag createNewByType(byte id) {
        switch (id) {
            case TAG_END:
                return new NBTTagEnd();

            case TAG_BYTE:
                return new NBTTagByte();

            case TAG_SHORT:
                return new NBTTagShort();

            case TAG_INT:
                return new NBTTagInt();

            case TAG_LONG:
                return new NBTTagLong();

            case TAG_FLOAT:
                return new NBTTagFloat();

            case TAG_DOUBLE:
                return new NBTTagDouble();

            case TAG_BYTE_ARRAY:
                return new NBTTagByteArray();

            case TAG_STRING:
                return new NBTTagString();

            case TAG_LIST:
                return new NBTTagList();

            case TAG_COMPOUND:
                return new NBTTagCompound();

            case TAG_INT_ARRAY:
                return new NBTTagIntArray();

            default:
                return null;
        }
    }

    /**
     * Creates a clone of the tag.
     */
    public abstract NBTTag copy();

    /**
     * Return whether this compound has no tags.
     */
    public boolean hasNoTags() {
        return false;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof NBTTag)) {
            return false;
        } else {
            NBTTag tag = (NBTTag) obj;
            return this.getId() == tag.getId();
        }
    }

    public int hashCode() {
        return this.getId();
    }

    protected String getString() {
        return this.toString();
    }
}
