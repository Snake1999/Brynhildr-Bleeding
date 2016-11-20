package io.nukkit.nbt;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

public class NBTTagList extends NBTTag {
    private static final Logger LOGGER = LogManager.getLogger();
    private List<NBTTag> tagList = Lists.newArrayList();

    /**
     * The type byte for the tags in the list - they must all be of the same type.
     */
    private byte tagType = 0;

    /**
     * Write the actual data contents of the tag, implemented in NBT extension classes
     */
    void write(DataOutput output) throws IOException {
        if (this.tagList.isEmpty()) {
            this.tagType = 0;
        } else {
            this.tagType = this.tagList.get(0).getId();
        }

        output.writeByte(this.tagType);
        output.writeInt(this.tagList.size());

        for (int i = 0; i < this.tagList.size(); ++i) {
            this.tagList.get(i).write(output);
        }
    }

    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        sizeTracker.read(296L);

        if (depth > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        } else {
            this.tagType = input.readByte();
            int i = input.readInt();

            if (this.tagType == 0 && i > 0) {
                throw new RuntimeException("Missing type on ListTag");
            } else {
                sizeTracker.read(32L * (long) i);
                this.tagList = Lists.newArrayListWithCapacity(i);

                for (int j = 0; j < i; ++j) {
                    NBTTag tag = NBTTag.createNewByType(this.tagType);
                    tag.read(input, depth + 1, sizeTracker);
                    this.tagList.add(tag);
                }
            }
        }
    }

    /**
     * Gets the type byte for the tag.
     */
    public byte getId() {
        return (byte) 9;
    }

    public String toString() {
        StringBuilder stringbuilder = new StringBuilder("[");

        for (int i = 0; i < this.tagList.size(); ++i) {
            if (i != 0) {
                stringbuilder.append(',');
            }

            stringbuilder.append(i).append(':').append(this.tagList.get(i));
        }

        return stringbuilder.append(']').toString();
    }

    /**
     * Adds the provided tag to the end of the list. There is no check to verify this tag is of the same type as any
     * previous tag.
     */
    public void appendTag(NBTTag nbt) {
        if (nbt.getId() == 0) {
            LOGGER.warn("Invalid TagEnd added to ListTag");
        } else {
            if (this.tagType == 0) {
                this.tagType = nbt.getId();
            } else if (this.tagType != nbt.getId()) {
                LOGGER.warn("Adding mismatching tag types to tag list");
                return;
            }

            this.tagList.add(nbt);
        }
    }

    /**
     * Set the given index to the given tag
     */
    public void set(int index, NBTTag nbt) {
        if (nbt.getId() == 0) {
            LOGGER.warn("Invalid TagEnd added to ListTag");
        } else if (index >= 0 && index < this.tagList.size()) {
            if (this.tagType == 0) {
                this.tagType = nbt.getId();
            } else if (this.tagType != nbt.getId()) {
                LOGGER.warn("Adding mismatching tag types to tag list");
                return;
            }

            this.tagList.set(index, nbt);
        } else {
            LOGGER.warn("index out of bounds to offset tag in tag list");
        }
    }

    /**
     * Removes a tag at the given index.
     */
    public NBTTag removeTag(int i) {
        return this.tagList.remove(i);
    }

    /**
     * Return whether this compound has no tags.
     */
    public boolean hasNoTags() {
        return this.tagList.isEmpty();
    }

    /**
     * Retrieves the NBTTagCompound at the specified index in the list
     */
    public NBTTagCompound getCompoundTagAt(int i) {
        if (i >= 0 && i < this.tagList.size()) {
            NBTTag tag = this.tagList.get(i);

            if (tag.getId() == NBTTag.TAG_COMPOUND) {
                return (NBTTagCompound) tag;
            }
        }

        return new NBTTagCompound();
    }

    public int getIntAt(int i) {
        if (i >= 0 && i < this.tagList.size()) {
            NBTTag tag = this.tagList.get(i);

            if (tag.getId() == NBTTag.TAG_INT) {
                return ((NBTTagInt) tag).getInt();
            }
        }

        return 0;
    }

    public int[] getIntArrayAt(int i) {
        if (i >= 0 && i < this.tagList.size()) {
            NBTTag tag = this.tagList.get(i);

            if (tag.getId() == NBTTag.TAG_INT_ARRAY) {
                return ((NBTTagIntArray) tag).getIntArray();
            }
        }

        return new int[0];
    }

    public double getDoubleAt(int i) {
        if (i >= 0 && i < this.tagList.size()) {
            NBTTag tag = this.tagList.get(i);

            if (tag.getId() == NBTTag.TAG_DOUBLE) {
                return ((NBTTagDouble) tag).getDouble();
            }
        }

        return 0.0D;
    }

    public float getFloatAt(int i) {
        if (i >= 0 && i < this.tagList.size()) {
            NBTTag tag = this.tagList.get(i);

            if (tag.getId() == NBTTag.TAG_FLOAT) {
                return ((NBTTagFloat) tag).getFloat();
            }
        }

        return 0.0F;
    }

    /**
     * Retrieves the tag String value at the specified index in the list
     */
    public String getStringTagAt(int i) {
        if (i >= 0 && i < this.tagList.size()) {
            NBTTag tag = this.tagList.get(i);
            return tag.getId() == NBTTag.TAG_STRING ? tag.getString() : tag.toString();
        } else {
            return "";
        }
    }

    /**
     * Get the tag at the given position
     */
    public NBTTag get(int idx) {
        return idx >= 0 && idx < this.tagList.size() ? this.tagList.get(idx) : new NBTTagEnd();
    }

    /**
     * Returns the number of tags in the list.
     */
    public int tagCount() {
        return this.tagList.size();
    }

    /**
     * Creates a clone of the tag.
     */
    public NBTTagList copy() {
        NBTTagList nbttaglist = new NBTTagList();
        nbttaglist.tagType = this.tagType;

        for (NBTTag tag : this.tagList) {
            NBTTag newTag = tag.copy();
            nbttaglist.tagList.add(newTag);
        }

        return nbttaglist;
    }

    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            NBTTagList nbttaglist = (NBTTagList) obj;

            if (this.tagType == nbttaglist.tagType) {
                return this.tagList.equals(nbttaglist.tagList);
            }
        }

        return false;
    }

    public int hashCode() {
        return super.hashCode() ^ this.tagList.hashCode();
    }

    public int getTagType() {
        return this.tagType;
    }
}
