package ru.spbau.mit;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class P2PConnection extends AbstractServer implements P2PClient {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Map<Integer, Set<Integer>> availableFileParts;
    private Map<Integer, Path> filesPaths;

    public P2PConnection(short port) {
        super(port);
        availableFileParts = new HashMap<>();
        filesPaths = new HashMap<>();
        setHandlerFactory(new P2PClientHandlerFactory(availableFileParts, filesPaths));
    }

    @Override
    public void connect(byte[] ip, short port) throws IOException {
        socket = new Socket(InetAddress.getByAddress(ip), port);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void disconnect() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }

    @Override
    public List<Integer> executeStat(int id) throws IOException {
        dataOutputStream.writeInt(Constants.STAT);
        dataOutputStream.writeInt(id);
        dataOutputStream.flush();
        int size = dataInputStream.readInt();
        List<Integer> partsList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            partsList.add(dataInputStream.readInt());
        }
        return partsList;
    }

    @Override
    public byte[] executeGet(int id, int part) throws IOException {
        dataOutputStream.writeInt(Constants.GET);
        dataOutputStream.writeInt(id);
        dataOutputStream.writeInt(part);
        dataOutputStream.flush();
        byte[] buffer = new byte[Constants.BLOCK_SIZE];
        dataInputStream.readFully(buffer);
        return buffer;
    }

    public List<Integer> getAvailableFileIds() {
        return new ArrayList<>(availableFileParts.keySet());
    }

    public void addFile(int id, Path path) {
        long size = path.toFile().length();
        filesPaths.put(id, path);
        Set<Integer> fileParts = new HashSet<>();
        for (int i = 0; i < (size + Constants.BLOCK_SIZE - 1) / Constants.BLOCK_SIZE; i++) {
            fileParts.add(i);
        }
        availableFileParts.put(id, fileParts);
    }

    public void addFilePart(int id, int part, Path path) {
        if (!filesPaths.containsKey(id)) {
            filesPaths.put(id, path);
            availableFileParts.put(id, new HashSet<>());
        }
        availableFileParts.get(id).add(part);
    }

    public void save() throws IOException {
        File file = Constants.SAVE_PATH.toFile();
        if (!file.exists()) {
            Files.createFile(Constants.SAVE_PATH);
            file = Constants.SAVE_PATH.toFile();
        }
        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file));

        dataOutputStream.writeInt(availableFileParts.size());
        for (Map.Entry<Integer, Set<Integer>> entry : availableFileParts.entrySet()) {
            dataOutputStream.writeInt(entry.getKey());
            Set<Integer> availableParts = entry.getValue();
            dataOutputStream.writeInt(availableParts.size());
            for (int part : availableParts) {
                dataOutputStream.writeInt(part);
            }
        }

        dataOutputStream.writeInt(filesPaths.size());
        for (Map.Entry<Integer, Path> filePath : filesPaths.entrySet()) {
            dataOutputStream.writeInt(filePath.getKey());
            dataOutputStream.writeUTF(filePath.getValue().toString());
        }
        dataOutputStream.close();
    }

    public void restore() throws IOException {
        File file = Constants.SAVE_PATH.toFile();
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));

        availableFileParts = new HashMap<>();
        filesPaths = new HashMap<>();

        int availableFilePartsSize = dataInputStream.readInt();
        for (int i = 0; i < availableFilePartsSize; i++) {
            int id = dataInputStream.readInt();
            int setSize = dataInputStream.readInt();
            Set<Integer> availableParts = new HashSet<>();
            for (int j = 0; j < setSize; j++) {
                availableParts.add(dataInputStream.readInt());
            }
            availableFileParts.put(id, availableParts);
        }

        int filesPathsSize = dataInputStream.readInt();
        for (int i = 0; i < filesPathsSize; i++) {
            int id = dataInputStream.readInt();
            String path = dataInputStream.readUTF();
            filesPaths.put(id, Paths.get(path));
        }
    }
}
