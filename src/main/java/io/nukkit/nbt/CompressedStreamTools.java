package io.nukkit.nbt;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressedStreamTools {
    /**
     * Load the gzipped compound from the inputstream.
     */
    public static NBTTagCompound readCompressed(InputStream is) throws IOException {
        NBTTagCompound nbttagcompound;

        try (DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(is)))) {
            nbttagcompound = read(datainputstream, NBTSizeTracker.INFINITE);
        }

        return nbttagcompound;
    }

    /**
     * Write the compound, gzipped, to the outputstream.
     */
    public static void writeCompressed(NBTTagCompound compound, OutputStream outputStream) throws IOException {

        try (DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(outputStream)))) {
            write(compound, dataoutputstream);
        }
    }

    /**
     * Reads from a CompressedStream.
     */
    public static NBTTagCompound read(DataInputStream inputStream) throws IOException {
        return read(inputStream, NBTSizeTracker.INFINITE);
    }

    /**
     * Reads the given DataInput, constructs, and returns an NBTTagCompound with the data from the DataInput
     */
    public static NBTTagCompound read(DataInput input, NBTSizeTracker accounter) throws IOException {
        NBTTag tag = read(input, 0, accounter);

        if (tag instanceof NBTTagCompound) {
            return (NBTTagCompound) tag;
        } else {
            throw new IOException("Root tag must be a named compound tag");
        }
    }

    public static void write(NBTTagCompound compound, DataOutput output) throws IOException {
        writeTag(compound, output);
    }

    private static void writeTag(NBTTag tag, DataOutput output) throws IOException {
        output.writeByte(tag.getId());

        if (tag.getId() != 0) {
            output.writeUTF("");
            tag.write(output);
        }
    }

    private static NBTTag read(DataInput input, int depth, NBTSizeTracker accounter) throws IOException {
        byte tagId = input.readByte();

        if (tagId == NBTTag.TAG_END) {
            return new NBTTagEnd();
        } else {
            input.readUTF();
            NBTTag tag = NBTTag.createNewByType(tagId);

            tag.read(input, depth, accounter);
            return tag;
        }
    }
}
