package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class FileEntry {

    public static final int BLOCK_SIZE = 10 * (1 << 20);

    private boolean hasId;
    private int id;
    private String name;
    private long size;

    public FileEntry(int id, String name, long size) {
        this.hasId = true;
        this.id = id;
        this.name = name;
        this.size = size;
    }

    public FileEntry(String name, long size) {
        this.hasId = false;
        this.name = name;
        this.size = size;
    }

    public boolean hasId() {
        return hasId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public void setId(int id) {
        this.hasId = true;
        this.id = id;
    }

    public void writeTo(DataOutputStream dataOutputStream) throws IOException {
        if (hasId) {
            dataOutputStream.writeInt(id);
        }
        dataOutputStream.writeUTF(name);
        dataOutputStream.writeLong(size);
    }

    public static FileEntry readFrom(DataInputStream dataInputStream, boolean hasId) throws IOException {
        if (hasId) {
            return new FileEntry(dataInputStream.readInt(), dataInputStream.readUTF(), dataInputStream.readLong());
        } else {
            return new FileEntry(dataInputStream.readUTF(), dataInputStream.readLong());
        }
    }

    public int getPartsCount() {
        return (int) ((size + BLOCK_SIZE - 1) / BLOCK_SIZE);
    }

    public int getPartSize(int partId) {
        if (partId < getPartsCount() - 1) {
            return BLOCK_SIZE;
        } else
        if (size % BLOCK_SIZE == 0) {
            return BLOCK_SIZE;
        } else {
            return (int) (size % BLOCK_SIZE);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FileEntry)) {
            return false;
        }
        FileEntry that = (FileEntry) obj;
        return this.hasId == that.hasId
                && this.id == that.id
                && Objects.equals(this.name, that.name)
                && this.size == that.size;
    }
}
