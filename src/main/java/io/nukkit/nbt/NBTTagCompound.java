package io.nukkit.nbt;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public class NBTTagCompound extends NBTTag {
    private final Map<String, NBTTag> tagMap = Maps.newHashMap();

    private static void writeEntry(String name, NBTTag data, DataOutput output) throws IOException {
        output.writeByte(data.getId());

        if (data.getId() != 0) {
            output.writeUTF(name);
            data.write(output);
        }
    }

    private static byte readType(DataInput input, NBTSizeTracker sizeTracker) throws IOException {
        return input.readByte();
    }

    private static String readKey(DataInput input, NBTSizeTracker sizeTracker) throws IOException {
        return input.readUTF();
    }

    static NBTTag readNBT(byte id, String key, DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        NBTTag tag = NBTTag.createNewByType(id);

        tag.read(input, depth, sizeTracker);
        return tag;
    }

    /**
     * Write the actual data contents of the tag, implemented in NBT extension classes
     */
    void write(DataOutput output) throws IOException {
        for (String s : this.tagMap.keySet()) {
            NBTTag tag = this.tagMap.get(s);
            writeEntry(s, tag, output);
        }

        output.writeByte(0);
    }

    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        sizeTracker.read(384L);

        if (depth > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        } else {
            this.tagMap.clear();
            byte b0;

            while ((b0 = readType(input, sizeTracker)) != 0) {
                String s = readKey(input, sizeTracker);
                sizeTracker.read((long) (224 + 16 * s.length()));
                NBTTag tag = readNBT(b0, s, input, depth + 1, sizeTracker);

                if (this.tagMap.put(s, tag) != null) {
                    sizeTracker.read(288L);
                }
            }
        }
    }

    public Set<String> getKeySet() {
        return this.tagMap.keySet();
    }

    /**
     * Gets the type byte for the tag.
     */
    public byte getId() {
        return (byte) 10;
    }

    public int getSize() {
        return this.tagMap.size();
    }

    /**
     * Stores the given tag into the map with the given string key. This is mostly used to store tag lists.
     */
    public void setTag(String key, NBTTag value) {
        this.tagMap.put(key, value);
    }

    /**
     * Stores a new NBTTagByte with the given byte value into the map with the given string key.
     */
    public void setByte(String key, byte value) {
        this.tagMap.put(key, new NBTTagByte(value));
    }

    /**
     * Stores a new NBTTagShort with the given short value into the map with the given string key.
     */
    public void setShort(String key, short value) {
        this.tagMap.put(key, new NBTTagShort(value));
    }

    /**
     * Stores a new NBTTagInt with the given integer value into the map with the given string key.
     */
    public void setInteger(String key, int value) {
        this.tagMap.put(key, new NBTTagInt(value));
    }

    /**
     * Stores a new NBTTagLong with the given long value into the map with the given string key.
     */
    public void setLong(String key, long value) {
        this.tagMap.put(key, new NBTTagLong(value));
    }

    public void setUniqueId(String key, UUID value) {
        this.setLong(key + "Most", value.getMostSignificantBits());
        this.setLong(key + "Least", value.getLeastSignificantBits());
    }

    @Nullable
    public UUID getUniqueId(String key) {
        return new UUID(this.getLong(key + "Most"), this.getLong(key + "Least"));
    }

    public boolean hasUniqueId(String key) {
        return this.hasKey(key + "Most", 99) && this.hasKey(key + "Least", 99);
    }

    /**
     * Stores a new NBTTagFloat with the given float value into the map with the given string key.
     */
    public void setFloat(String key, float value) {
        this.tagMap.put(key, new NBTTagFloat(value));
    }

    /**
     * Stores a new NBTTagDouble with the given double value into the map with the given string key.
     */
    public void setDouble(String key, double value) {
        this.tagMap.put(key, new NBTTagDouble(value));
    }

    /**
     * Stores a new NBTTagString with the given string value into the map with the given string key.
     */
    public void setString(String key, String value) {
        this.tagMap.put(key, new NBTTagString(value));
    }

    /**
     * Stores a new NBTTagByteArray with the given array as data into the map with the given string key.
     */
    public void setByteArray(String key, byte[] value) {
        this.tagMap.put(key, new NBTTagByteArray(value));
    }

    /**
     * Stores a new NBTTagIntArray with the given array as data into the map with the given string key.
     */
    public void setIntArray(String key, int[] value) {
        this.tagMap.put(key, new NBTTagIntArray(value));
    }

    /**
     * Stores the given boolean value as a NBTTagByte, storing 1 for true and 0 for false, using the given string key.
     */
    public void setBoolean(String key, boolean value) {
        this.setByte(key, (byte) (value ? 1 : 0));
    }

    /**
     * gets a generic tag with the specified name
     */
    public NBTTag getTag(String key) {
        return this.tagMap.get(key);
    }

    /**
     * Gets the ID byte for the given tag key
     */
    public byte getTagId(String key) {
        NBTTag tag = this.tagMap.get(key);
        return tag == null ? 0 : tag.getId();
    }

    /**
     * Returns whether the given string has been previously stored as a key in the map.
     */
    public boolean hasKey(String key) {
        return this.tagMap.containsKey(key);
    }

    /**
     * Returns whether the given string has been previously stored as a key in this tag compound as a particular type,
     * denoted by a parameter in the form of an ordinal. If the provided ordinal is 99, this method will match tag types
     * representing numbers.
     */
    public boolean hasKey(String key, int type) {
        int i = this.getTagId(key);
        return i == type || (type == 99 && (i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6));
    }

    /**
     * Retrieves a byte value using the specified key, or 0 if no such key was stored.
     */
    public byte getByte(String key) {
        try {
            if (this.hasKey(key, 99)) {
                return ((NBTTagNumber) this.tagMap.get(key)).getByte();
            }
        } catch (ClassCastException ignored) {
        }

        return (byte) 0;
    }

    /**
     * Retrieves a short value using the specified key, or 0 if no such key was stored.
     */
    public short getShort(String key) {
        try {
            if (this.hasKey(key, 99)) {
                return ((NBTTagNumber) this.tagMap.get(key)).getShort();
            }
        } catch (ClassCastException ignored) {
        }

        return (short) 0;
    }

    /**
     * Retrieves an integer value using the specified key, or 0 if no such key was stored.
     */
    public int getInteger(String key) {
        try {
            if (this.hasKey(key, 99)) {
                return ((NBTTagNumber) this.tagMap.get(key)).getInt();
            }
        } catch (ClassCastException ignored) {
        }

        return 0;
    }

    /**
     * Retrieves a long value using the specified key, or 0 if no such key was stored.
     */
    public long getLong(String key) {
        try {
            if (this.hasKey(key, 99)) {
                return ((NBTTagNumber) this.tagMap.get(key)).getLong();
            }
        } catch (ClassCastException ignored) {
        }

        return 0L;
    }

    /**
     * Retrieves a float value using the specified key, or 0 if no such key was stored.
     */
    public float getFloat(String key) {
        try {
            if (this.hasKey(key, 99)) {
                return ((NBTTagNumber) this.tagMap.get(key)).getFloat();
            }
        } catch (ClassCastException ignored) {
        }

        return 0.0F;
    }

    /**
     * Retrieves a double value using the specified key, or 0 if no such key was stored.
     */
    public double getDouble(String key) {
        try {
            if (this.hasKey(key, 99)) {
                return ((NBTTagNumber) this.tagMap.get(key)).getDouble();
            }
        } catch (ClassCastException ignored) {
        }

        return 0.0D;
    }

    /**
     * Retrieves a string value using the specified key, or an empty string if no such key was stored.
     */
    public String getString(String key) {
        try {
            if (this.hasKey(key, 8)) {
                return this.tagMap.get(key).getString();
            }
        } catch (ClassCastException ignored) {
        }

        return "";
    }

    /**
     * Retrieves a byte array using the specified key, or a zero-length array if no such key was stored.
     */
    public byte[] getByteArray(String key) {
        if (this.hasKey(key, 7)) {
            return ((NBTTagByteArray) this.tagMap.get(key)).getByteArray();
        }

        return new byte[0];
    }

    /**
     * Retrieves an int array using the specified key, or a zero-length array if no such key was stored.
     */
    public int[] getIntArray(String key) {
        if (this.hasKey(key, 11)) {
            return ((NBTTagIntArray) this.tagMap.get(key)).getIntArray();
        }

        return new int[0];
    }

    /**
     * Retrieves a NBTTagCompound subtag matching the specified key, or a new empty NBTTagCompound if no such key was
     * stored.
     */
    public NBTTagCompound getCompoundTag(String key) {
        if (this.hasKey(key, 10)) {
            return (NBTTagCompound) this.tagMap.get(key);
        }

        return new NBTTagCompound();
    }

    /**
     * Gets the NBTTagList object with the given name.
     */
    public NBTTagList getTagList(String key, int type) {
        if (this.getTagId(key) == 9) {
            NBTTagList nbttaglist = (NBTTagList) this.tagMap.get(key);

            if (!nbttaglist.hasNoTags() && nbttaglist.getTagType() != type) {
                return new NBTTagList();
            }

            return nbttaglist;
        }

        return new NBTTagList();
    }

    /**
     * Retrieves a boolean value using the specified key, or false if no such key was stored. This uses the getByte
     * method.
     */
    public boolean getBoolean(String key) {
        return this.getByte(key) != 0;
    }

    /**
     * Remove the specified tag.
     */
    public void removeTag(String key) {
        this.tagMap.remove(key);
    }

    public String toString() {
        StringBuilder stringbuilder = new StringBuilder("{");

        for (Entry<String, NBTTag> entry : this.tagMap.entrySet()) {
            if (stringbuilder.length() != 1) {
                stringbuilder.append(',');
            }

            stringbuilder.append(entry.getKey()).append(':').append(entry.getValue());
        }

        return stringbuilder.append('}').toString();
    }

    /**
     * Return whether this compound has no tags.
     */
    public boolean hasNoTags() {
        return this.tagMap.isEmpty();
    }

    /**
     * Creates a clone of the tag.
     */
    public NBTTagCompound copy() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        for (String s : this.tagMap.keySet()) {
            nbttagcompound.setTag(s, this.tagMap.get(s).copy());
        }

        return nbttagcompound;
    }

    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            NBTTagCompound nbttagcompound = (NBTTagCompound) obj;
            return this.tagMap.entrySet().equals(nbttagcompound.tagMap.entrySet());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return super.hashCode() ^ this.tagMap.hashCode();
    }

    /**
     * Merges this NBTTagCompound with the given compound. Any sub-compounds are merged using the same methods, other
     * types of tags are overwritten from the given compound.
     */
    public void merge(NBTTagCompound other) {
        for (String s : other.tagMap.keySet()) {
            NBTTag tag = other.tagMap.get(s);

            if (tag.getId() == 10) {
                if (this.hasKey(s, 10)) {
                    NBTTagCompound nbttagcompound = this.getCompoundTag(s);
                    nbttagcompound.merge((NBTTagCompound) tag);
                } else {
                    this.setTag(s, tag.copy());
                }
            } else {
                this.setTag(s, tag.copy());
            }
        }
    }
}
