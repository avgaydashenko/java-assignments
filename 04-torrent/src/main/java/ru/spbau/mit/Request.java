package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Request {
    
    private int fileId;
    private int partId;

    public Request(int fileId, int partId) {
        this.fileId = fileId;
        this.partId = partId;
    }

    public int getFileId() {
        return fileId;
    }

    public int getPartId() {
        return partId;
    }

    public void writeTo(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(fileId);
        dataOutputStream.writeInt(partId);
    }

    public static Request readFrom(DataInputStream dataInputStream) throws IOException {
        return new Request(dataInputStream.readInt(), dataInputStream.readInt());
    }
}
