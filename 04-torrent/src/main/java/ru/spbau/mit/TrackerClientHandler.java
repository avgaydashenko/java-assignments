package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class TrackerClientHandler implements Runnable {
    private Socket socket;
    private final List<FileEntry> filesList;
    private final Map<ClientInformation, Set<Integer>> clientSeededFiles;
    private Map<ClientInformation, TimerTask> toRemoveClientTasks;
    private Timer toRemoveClientTimer;

    public TrackerClientHandler(Socket socket, List<FileEntry> filesList, Map<ClientInformation,
            Set<Integer>> clientSeededFiles, Map<ClientInformation, TimerTask> toRemoveClientTasks) {
        this.socket = socket;
        this.filesList = filesList;
        this.clientSeededFiles = clientSeededFiles;
        this.toRemoveClientTasks = toRemoveClientTasks;
        toRemoveClientTimer = new Timer();
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
                    return;
                }
                switch (requestType) {
                    case Constants.LIST:
                        handleList(dataOutputStream);
                        break;
                    case Constants.UPLOAD:
                        handleUpload(dataInputStream, dataOutputStream);
                        break;
                    case Constants.SOURCES:
                        handleSources(dataInputStream, dataOutputStream);
                        break;
                    case Constants.UPDATE:
                        handleUpdate(dataInputStream, dataOutputStream);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleList(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(filesList.size());
        synchronized (filesList) {
            for (FileEntry fileEntry : filesList) {
                fileEntry.writeTo(dataOutputStream);
            }
        }
        dataOutputStream.flush();
    }

    private void handleUpload(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        FileEntry fileEntry = FileEntry.readWithoutId(dataInputStream);
        int id = IdProvider.getInstance().getNextId();
        fileEntry.setId(id);
        filesList.add(fileEntry);
        dataOutputStream.writeInt(id);
        dataOutputStream.flush();
    }

    private void handleSources(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        int id = dataInputStream.readInt();
        List<ClientInformation> seedingClientsList = new ArrayList<>();
        synchronized (clientSeededFiles) {
            for (Map.Entry<ClientInformation, Set<Integer>> entry : clientSeededFiles.entrySet()) {
                Set<Integer> seededFiles = entry.getValue();
                if (seededFiles.contains(id)) {
                    seedingClientsList.add(entry.getKey());
                }
            }
        }
        dataOutputStream.writeInt(seedingClientsList.size());
        for (ClientInformation clientInformation : seedingClientsList) {
            clientInformation.writeTo(dataOutputStream);
        }
        dataOutputStream.flush();
    }

    private void handleUpdate(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        try {
            byte[] ip = socket.getInetAddress().getAddress();
            short port = dataInputStream.readShort();
            int count = dataInputStream.readInt();
            Set<Integer> seededFiles = new HashSet<>();
            for (int i = 0; i < count; i++) {
                seededFiles.add(dataInputStream.readInt());
            }
            ClientInformation clientInformation = new ClientInformation(ip, port);

            if (toRemoveClientTasks.containsKey(clientInformation)) {
                toRemoveClientTasks.get(clientInformation).cancel();
            }
            clientSeededFiles.put(clientInformation, seededFiles);

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    clientSeededFiles.remove(clientInformation);
                }
            };
            toRemoveClientTimer.schedule(task, Constants.REST_DELAY);

        } catch (IOException e) {
            e.printStackTrace();
            dataOutputStream.writeBoolean(false);
            dataOutputStream.flush();
            return;
        }
        dataOutputStream.writeBoolean(true);
        dataOutputStream.flush();
    }
}
