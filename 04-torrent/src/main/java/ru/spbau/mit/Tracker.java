package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class Tracker implements AutoCloseable {
    private static final String STATE_FILE = "tracker-state.dat";

    private Path workingDir;
    private ExecutorService threadPool;
    private ScheduledExecutorService scheduler;
    private ServerSocket serverSocket;
    private List<FileEntry> files;
    private Map<Integer, Set<ClientInformation>> seeders;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public Tracker(Path workingDir) throws IOException {
        try (LockHandler handler = LockHandler.lock(lock.writeLock())) {
            this.workingDir = workingDir;
            serverSocket = new ServerSocket(TrackerConnection.SERVER_PORT);
            threadPool = Executors.newCachedThreadPool();
            scheduler = Executors.newScheduledThreadPool(1);
            load();
        }
        threadPool.submit(this::work);
    }

    @Override
    public void close() throws IOException {
        try (LockHandler handler = LockHandler.lock(lock.writeLock())) {
            serverSocket.close();
        }
        threadPool.shutdown();
        scheduler.shutdown();
        store();
    }

    private void work() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                if (socket == null) {
                    return;
                }
                threadPool.submit(() -> handleConnection(socket));
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void handleConnection(Socket socket) {
        try (TrackerConnection connection = new TrackerConnection(socket)) {
            int request = connection.readRequest();
            switch (request) {
                case TrackerConnection.LIST:
                    doList(connection);
                    break;
                case TrackerConnection.SOURCES:
                    doSources(connection);
                    break;
                case TrackerConnection.UPLOAD:
                    doUpload(connection);
                    break;
                case TrackerConnection.UPDATE:
                    doUpdate(connection);
                    break;
                default:
                    System.err.printf("Wrong request from client: %d.\n", request);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doList(TrackerConnection connection) throws IOException {
        try (LockHandler handler = LockHandler.lock(lock.readLock())) {
            connection.writeListResponse(files);
        }
    }

    private void doSources(TrackerConnection connection) throws IOException {
        List<Integer> request = connection.readSourcesRequest();
        List<InetSocketAddress> result;
        try (LockHandler handler = LockHandler.lock(lock.readLock())) {
            result = request.stream()
                    .flatMap(i -> seeders
                                    .getOrDefault(i, Collections.emptySet())
                                    .stream()
                    )
                    .distinct()
                    .map(ClientInformation::getSocketAddress)
                    .collect(Collectors.toList());
        }
        connection.writeSourcesResponse(result);
    }

    private void doUpload(TrackerConnection connection) throws IOException {
        FileEntry newEntry = connection.readUploadRequest();
        try (LockHandler handler = LockHandler.lock(lock.writeLock())) {
            int newId = files.size();
            newEntry.setId(newId);
            files.add(newEntry);
        }
        connection.writeUploadResponse(newEntry.getId());
    }

    private void doUpdate(TrackerConnection connection) throws IOException {
        ClientInformation receivedClientInfo = connection.readUpdateRequest();
        ClientInformation clientInfo = new ClientInformation(
                new InetSocketAddress(connection.getHost(), receivedClientInfo.getSocketAddress().getPort()),
                receivedClientInfo.getFilesIds()
        );
        try (LockHandler handler = LockHandler.lock(lock.writeLock())) {
            for (int id : clientInfo.getFilesIds()) {
                if (seeders.get(id) == null) {
                    seeders.put(id, new HashSet<>());
                }
                seeders.get(id).add(clientInfo);
            }
        }

        scheduler.schedule(() -> {
            try (LockHandler handler = LockHandler.lock(lock.writeLock())) {
                for (int id : clientInfo.getFilesIds()) {
                    seeders.get(id).remove(clientInfo);
                }
            }
        }, TrackerConnection.UPDATE_TIME, TimeUnit.MILLISECONDS);

        connection.writeUpdateResponse(true);
    }

    private void store() throws IOException {
        Path path = workingDir.resolve(STATE_FILE);
        if (!Files.exists(path)) {
            Files.createDirectories(workingDir);
            Files.createFile(path);
        }
        try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(path))) {
            IOHandler.writeCollection(files, (dos1, o) -> o.writeTo(dos1), dos);
        }
    }

    private void load() throws IOException {
        Path path = workingDir.resolve(STATE_FILE);
        if (Files.exists(path)) {
            try (DataInputStream dis = new DataInputStream(Files.newInputStream(path))) {
                files = IOHandler.readCollection(new ArrayList<>(), dis1 -> FileEntry.readFrom(dis1, true), dis);
            }
        } else {
            files = new ArrayList<>();
        }
        seeders = new HashMap<>();
    }
}
