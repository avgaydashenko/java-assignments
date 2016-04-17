package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FileEntry {
    private int id;
    private String name;
    private long size;

    public FileEntry(int id, String name, long size) {
        this.id = id;
        this.name = name;
        this.size = size;
    }

    public FileEntry(String name, long size) {
        this(0, name, size);
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
        this.id = id;
    }

    public void writeTo(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(id);
        dataOutputStream.writeUTF(name);
        dataOutputStream.writeLong(size);
    }

    public static FileEntry readFrom(DataInputStream dataInputStream) throws IOException {
        int id = dataInputStream.readInt();
        String name = dataInputStream.readUTF();
        long size = dataInputStream.readLong();
        return new FileEntry(id, name, size);
    }

    public static FileEntry readWithoutId(DataInputStream dataInputStream) throws IOException {
        String name = dataInputStream.readUTF();
        long size = dataInputStream.readLong();
        return new FileEntry(name, size);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FileEntry)) {
            return false;
        }
        FileEntry fileEntry = (FileEntry) obj;
        return id == fileEntry.id && name.equals(fileEntry.name) && size == fileEntry.size;
    }
}
