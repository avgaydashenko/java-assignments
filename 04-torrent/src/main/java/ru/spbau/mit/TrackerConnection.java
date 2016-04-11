package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TrackerConnection extends Connection {

    public static final int SERVER_PORT = 8081;
    public static final int UPDATE_TIME = 5 * 60 * 1000;

    public static final int LIST = 1;
    public static final int UPLOAD = 2;
    public static final int SOURCES = 3;
    public static final int UPDATE = 4;

    public TrackerConnection(Socket socket) throws IOException {
        super(socket);
    }

    public void writeListRequest() throws IOException {
        DataOutputStream dataOutputStream = getOutput();
        dataOutputStream.writeByte(LIST);
        dataOutputStream.flush();
    }

    public void writeListResponse(Collection<FileEntry> files) throws IOException {
        writeCollection(files, (dataOutputStream, entry) -> {
            if (!entry.hasId()) {
                throw new IllegalStateException("Uploaded files must have id.");
            }
            entry.writeTo(dataOutputStream);
        });
        getOutput().flush();
    }

    public List<FileEntry> readListResponse() throws IOException {
        return readCollection(new ArrayList<>(), (dataInputStream) -> FileEntry.readFrom(dataInputStream, true));
    }

    public void writeUploadRequest(FileEntry file) throws IOException {
        if (file.hasId()) {
            throw new IllegalStateException("Uploading file cannot have id.");
        }

        DataOutputStream dataOutputStream = getOutput();
        dataOutputStream.writeByte(UPLOAD);
        file.writeTo(dataOutputStream);
        dataOutputStream.flush();
    }

    public FileEntry readUploadRequest() throws IOException {
        return FileEntry.readFrom(getInput(), false);
    }

    public void writeUploadResponse(int fileId) throws IOException {
        DataOutputStream dataOutputStream = getOutput();
        dataOutputStream.writeInt(fileId);
        dataOutputStream.flush();
    }

    public int readUploadResponse() throws IOException {
        return getInput().readInt();
    }

    public void writeSourcesRequest(Collection<Integer> filesIds) throws IOException {
        if (filesIds.size() == 0) {
            throw new IllegalStateException("There are no ids");
        }

        DataOutputStream dataOutputStream = getOutput();
        dataOutputStream.writeByte(SOURCES);
        writeCollection(filesIds, DataOutputStream::writeInt);
        dataOutputStream.flush();
    }

    public List<Integer> readSourcesRequest() throws IOException {
        return readCollection(new ArrayList<>(), DataInputStream::readInt);
    }

    public void writeSourcesResponse(Collection<InetSocketAddress> addresses) throws IOException {
        writeCollection(addresses, IOHandler::writeAddress);
    }

    public List<InetSocketAddress> readSourcesResponse() throws IOException {
        return readCollection(new ArrayList<>(), IOHandler::readAddress);
    }

    public void writeUpdateRequest(ClientInformation clientInformation) throws IOException {
        if (clientInformation.getFilesIds().size() == 0) {
            throw new IllegalStateException("There are no ids");
        }

        DataOutputStream dataOutputStream = getOutput();
        dataOutputStream.writeByte(UPDATE);
        clientInformation.writeTo(dataOutputStream);
        dataOutputStream.flush();
    }

    public ClientInformation readUpdateRequest() throws IOException {
        return ClientInformation.readFrom(getInput());
    }

    public void writeUpdateResponse(boolean isSuccessful) throws IOException {
        DataOutputStream dataOutputStream = getOutput();
        dataOutputStream.writeBoolean(isSuccessful);
        dataOutputStream.flush();
    }

    public boolean readUpdateResponse() throws IOException {
        return getInput().readBoolean();
    }
}
