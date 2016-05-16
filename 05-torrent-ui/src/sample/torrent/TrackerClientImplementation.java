package sample.torrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TrackerClientImplementation implements TrackerClient {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    @Override
    public void connect(byte[] ip, int port) throws IOException {
        socket = new Socket(InetAddress.getByAddress(ip), port);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void disconnect() throws IOException {
        socket.close();
    }

    @Override
    public synchronized List<FileEntry> executeList() throws IOException {
        dataOutputStream.writeInt(Constants.LIST);
        dataOutputStream.flush();
        int size = dataInputStream.readInt();
        List<FileEntry> filesList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            filesList.add(FileEntry.readFrom(dataInputStream));
        }
        return filesList;
    }

    @Override
    public synchronized int executeUpload(String path, long size) throws IOException {
        dataOutputStream.writeInt(Constants.UPLOAD);
        dataOutputStream.writeUTF(path);
        dataOutputStream.writeLong(size);
        dataOutputStream.flush();
        return dataInputStream.readInt();
    }

    @Override
    public synchronized List<ClientInformation> executeSources(int id) throws IOException {
        dataOutputStream.writeInt(Constants.SOURCES);
        dataOutputStream.writeInt(id);
        dataOutputStream.flush();
        int size = dataInputStream.readInt();
        List<ClientInformation> clientsList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            clientsList.add(ClientInformation.readFrom(dataInputStream));
        }
        return clientsList;
    }

    @Override
    public synchronized boolean executeUpdate(short port, List<Integer> seededFiles) throws IOException {
        dataOutputStream.writeInt(Constants.UPDATE);
        dataOutputStream.writeShort(port);
        dataOutputStream.writeInt(seededFiles.size());
        for (int id : seededFiles) {
            dataOutputStream.writeInt(id);
        }
        dataOutputStream.flush();
        return dataInputStream.readBoolean();
    }
}
