package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class P2PClientHandler implements Runnable {
    private Socket socket;
    private Map<Integer, Set<Integer>> availableFileParts;
    private Map<Integer, Path> filesPaths;

    public P2PClientHandler(Socket socket, Map<Integer, Set<Integer>> availableFileParts,
                            Map<Integer, Path> filesPaths) {
        this.socket = socket;
        this.availableFileParts = availableFileParts;
        this.filesPaths = filesPaths;
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            DataOutputStream dataOutputStream;
            DataInputStream dataInputStream;
            try {
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            int requestType;
            try {
                try {
                    requestType = dataInputStream.readInt();
                } catch (EOFException e) {
                    e.printStackTrace();
                    return;
                }
                switch (requestType) {
                    case Constants.STAT:
                        handleStat(dataInputStream, dataOutputStream);
                        break;
                    case Constants.GET:
                        handleGet(dataInputStream, dataOutputStream);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleStat(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        int id = dataInputStream.readInt();
        if (!availableFileParts.containsKey(id)) {
            dataOutputStream.writeInt(0);
        } else {
            Set<Integer> availableParts = availableFileParts.get(id);
            dataOutputStream.writeInt(availableParts.size());
            for (Integer part : availableParts) {
                dataOutputStream.writeInt(part);
            }
        }
        dataOutputStream.flush();
    }

    private void handleGet(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        int id = dataInputStream.readInt();
        int partNumber = dataInputStream.readInt();
        if (availableFileParts.containsKey(id) && availableFileParts.get(id).contains(partNumber)) {
            byte[] buffer = new byte[Constants.BLOCK_SIZE];
            DataInputStream fileInputStream = new DataInputStream(Files.newInputStream(filesPaths.get(id)));
            fileInputStream.skipBytes(partNumber * Constants.BLOCK_SIZE);
            try {
                fileInputStream.readFully(buffer);
            } catch (EOFException ignored) { }
            fileInputStream.close();
            dataOutputStream.write(buffer);
        }
    }
}
