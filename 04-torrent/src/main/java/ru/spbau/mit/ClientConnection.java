package ru.spbau.mit;

import java.io.*;
import java.net.Socket;

public class ClientConnection extends Connection {

    public static final int STAT = 1;
    public static final int GET = 2;

    private static final int BUFFER_SIZE = 4 * (1 << 10);

    public ClientConnection(Socket socket) throws IOException {
        super(socket);
    }

    public void writeStatRequest(int fileId) throws IOException {
        DataOutputStream dataOutputStream = getOutput();
        dataOutputStream.writeByte(STAT);
        dataOutputStream.writeInt(fileId);
        dataOutputStream.flush();
    }

    public int readStatRequest() throws IOException {
        return getInput().readInt();
    }

    public void writeStatResponse(PartsSet parts) throws IOException {
        DataOutputStream dataOutputStream = getOutput();
        parts.writeTo(dataOutputStream);
        dataOutputStream.flush();
    }

    public PartsSet readStatResponse(int size) throws IOException {
        return PartsSet.readFrom(getInput(), size);
    }

    public void writeGetRequest(Request request) throws IOException {
        DataOutputStream dataOutputStream = getOutput();
        dataOutputStream.writeByte(GET);
        request.writeTo(dataOutputStream);
        dataOutputStream.flush();
    }

    public Request readGetRequest() throws IOException {
        return Request.readFrom(getInput());
    }

    public void writeGetResponse(RandomAccessFile from, int partId, FileEntry entry) throws IOException {
        from.seek(FileEntry.BLOCK_SIZE * partId);
        int amount = entry.getPartSize(partId);

        DataOutputStream dataOutputStream = getOutput();
        byte[] buffer = new byte[BUFFER_SIZE];
        while (amount > 0) {
            int read = from.read(buffer, 0, Math.min(amount, BUFFER_SIZE));
            if (read == -1) {
                throw new EOFException("File is shorter than recorded size.");
            }
            amount -= read;
            dataOutputStream.write(buffer, 0, read);
        }
        dataOutputStream.flush();
    }

    public void readGetResponse(RandomAccessFile to, int partId, FileEntry entry) throws IOException {
        to.seek(FileEntry.BLOCK_SIZE * partId);
        int amount = entry.getPartSize(partId);

        DataInputStream dataInputStream = getInput();
        byte[] buffer = new byte[BUFFER_SIZE];
        while (amount > 0) {
            int read = dataInputStream.read(buffer, 0, Math.min(amount, BUFFER_SIZE));
            if (read == -1) {
                throw new EOFException("Cannot read the end of the file from socket.");
            }
            amount -= read;
            to.write(buffer, 0, read);
        }
    }
}
