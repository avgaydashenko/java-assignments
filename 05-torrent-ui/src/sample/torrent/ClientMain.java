package sample.torrent;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import sample.Controller;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ClientMain implements Client {
    private static final String START = "start";
    private static final String STOP = "stop";
    private static final String LIST = "list";
    private static final String DOWNLOAD = "download";
    private static final String UPLOAD = "upload";
    private static final String QUIT = "quit";
    private static final String WRONG_INPUT = "choose one: start | stop | list | download | upload | quit";
    private static final byte[] LOCALHOST = new byte[] {127, 0, 0, 1};

    private TrackerClient trackerClient = new TrackerClientImplementation();
    private P2PConnection p2pConnection;
    private Timer updateTimer = new Timer();
    private TimerTask updateTask;
    private short port;

    public ClientMain(short port) {
        this.port = port;
        p2pConnection = new P2PConnection(port);
    }

    @Override
    public void start(byte[] ip) throws IOException {
        trackerClient.connect(ip, Constants.SERVER_PORT);
        p2pConnection.start();
        updateTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    trackerClient.executeUpdate(port, p2pConnection.getAvailableFileIds());
                } catch (IOException e) {
                    System.out.println("ClientMain#start: failed to update trackerClient.");
                }
            }
        };
        updateTimer.schedule(updateTask, 0, Constants.REST_DELAY);
    }

    @Override
    public void stop() throws IOException {
        trackerClient.disconnect();
        p2pConnection.stop();
        p2pConnection.disconnect();
        updateTask.cancel();
    }

    @Override
    public List<FileEntry> getFilesList() throws IOException {
        return trackerClient.executeList();
    }

    @Override
    public void download(int fileId, Path path, ProgressBar progressBar) throws IOException {
        List<ClientInformation> clientsList = trackerClient.executeSources(fileId);
        List<FileEntry> filesList = trackerClient.executeList();
        FileEntry newFileEntry = null;
        for (FileEntry fileEntry : filesList) {
            if (fileEntry.getId() == fileId) {
                newFileEntry = fileEntry;
                break;
            }
        }
        assert newFileEntry != null;
        Path filePath = Paths.get(path.toString() + "(1)");
        File file = filePath.toFile();
        RandomAccessFile newFile = new RandomAccessFile(file, "rw");
        long fileSize = newFileEntry.getSize();
        newFile.setLength(fileSize);

        int partNumber = (int) ((fileSize + Constants.BLOCK_SIZE - 1) / Constants.BLOCK_SIZE);
        Set<Integer> availableParts = new HashSet<>();
        while (availableParts.size() != partNumber) {
            for (ClientInformation clientInformation : clientsList) {
                p2pConnection.connect(clientInformation.getIp(), clientInformation.getPort());
                List<Integer> fileParts = p2pConnection.executeStat(fileId);
                for (int part : fileParts) {
                    if (!availableParts.contains(part)) {
                        newFile.seek(part * Constants.BLOCK_SIZE);
                        int partSize = Constants.BLOCK_SIZE;
                        if (part == partNumber - 1) {
                            partSize = (int) (fileSize % Constants.BLOCK_SIZE);
                        }
                        byte[] buffer;
                        try {
                            buffer = p2pConnection.executeGet(fileId, part);
                        } catch (IOException e) {
                            continue;
                        }
                        newFile.write(buffer, 0, partSize);
                        availableParts.add(part);
                        Controller.progress = (double) availableParts.size() / partNumber;
                        if (progressBar != null) {
                            synchronized (progressBar) {
                                Platform.runLater(() -> progressBar.setProgress(Controller.progress));
                            }
                        }
                        p2pConnection.addFilePart(fileId, part, filePath);
                        trackerClient.executeUpdate(port, p2pConnection.getAvailableFileIds());
                    }
                }
            }
        }
        newFile.close();
    }

    @Override
    public void upload(String path) throws IOException {
        Path p = Paths.get(path);
        File file = p.toFile();
        if (!file.exists() || file.isDirectory()) {
            throw new NoSuchFileException(path);
        }
        int id = trackerClient.executeUpload(p.getFileName().toString(), file.length());
        p2pConnection.addFile(id, p);
        trackerClient.executeUpdate(port, p2pConnection.getAvailableFileIds());
    }

    @Override
    public void save() throws IOException {
        p2pConnection.save();
    }

    @Override
    public void restore() throws IOException {
        p2pConnection.restore();
    }

    public static void main(String[] args) {
        Client client = new ClientMain(Short.valueOf(args[0]));
        if (Constants.SAVE_PATH.toFile().exists()) {
            try {
                client.restore();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            List<String> arguments = Arrays.asList(line.split(" "));
            if (arguments.size() == 0) {
                continue;
            }
            switch (arguments.get(0)) {
                case START:
                    handleStart(client);
                    break;
                case STOP:
                    handleStop(client);
                    break;
                case LIST:
                    handleList(client);
                    break;
                case DOWNLOAD:
                    handleDownload(client, arguments.subList(1, arguments.size()));
                    break;
                case UPLOAD:
                    handleUpload(client, arguments.subList(1, arguments.size()));
                    break;
                case QUIT:
                    handleQuit(client);
                    return;
                default:
                    System.out.println(WRONG_INPUT);
            }
        }
    }

    private static void handleStart(Client client) {
        try {
            client.start(LOCALHOST);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void handleStop(Client client) {
        try {
            client.stop();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void handleList(Client client) {
        try {
            List<FileEntry> filesList = client.getFilesList();
            for (FileEntry fileEntry : filesList) {
                System.out.println(fileEntry.getId() + " " + fileEntry.getName() + " " + fileEntry.getSize());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void handleDownload(Client client, List<String> arguments) {
        if (arguments.size() != 2) {
            System.out.println(WRONG_INPUT);
            return;
        }
        try {
            client.download(Integer.valueOf(arguments.get(0)), Paths.get(arguments.get(1)), null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void handleUpload(Client client, List<String> arguments) {
        if (arguments.size() != 1) {
            System.out.println(WRONG_INPUT);
            return;
        }
        try {
            client.upload(arguments.get(0));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void handleQuit(Client client) {
        try {
            client.save();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
