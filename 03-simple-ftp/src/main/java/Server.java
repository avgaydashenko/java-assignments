import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {

    private static final int LIST = 1;
    private static final int BUFFER = (int) Math.pow(2, 21);

    private ServerSocket serverSocket;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() {
        Thread thread = new Thread(() -> {
            try {
                processSocket();
            } catch (IOException ignored) {
                System.out.println("Failed to process socket");
            }
        });
        thread.start();
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException ignored) {
            System.out.println("Failed to close socket (server)");
        }
    }

    private synchronized void processSocket() throws IOException {
        while (true) {
            if (!serverSocket.isClosed()) {

                Socket socket;
                try {
                    socket = serverSocket.accept();
                } catch (SocketException ignored) {
                    return;
                }

                Thread thread = new Thread(() -> {
                    decodeSocket(socket);
                });
                thread.start();

            } else {
                return;
            }
        }
    }

    private void decodeSocket(Socket socket) {
        try {

            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            while (!socket.isClosed()) {

                int command = dataInputStream.readInt();
                String path = dataInputStream.readUTF();

                if (command == LIST) {
                    list(path, dataOutputStream);
                } else {
                    get(path, dataOutputStream);
                }
            }
        } catch (IOException ignored) {
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void list(String path, DataOutputStream dataOutputStream) throws IOException {

        File file = new File(path);

        if (file.isDirectory()) {

            File[] fileList = file.listFiles();

            if (fileList == null) {
                dataOutputStream.writeInt(0);
                return;
            }

            dataOutputStream.writeInt(fileList.length);

            for (File f : fileList) {
                dataOutputStream.writeUTF(f.getName());
                dataOutputStream.writeBoolean(f.isDirectory());
            }

        } else {
            dataOutputStream.write(0);
        }
    }

    private void get(String path, DataOutputStream dataOutputStream) throws IOException {

        File file = new File(path);

        if (file.exists() && !file.isDirectory() && file.canRead()) {

            dataOutputStream.writeLong(file.length());

            FileInputStream fileInputStream = new FileInputStream(file);

            byte[] buffer = new byte[(int) Math.min(BUFFER, file.length())];

            int numberReadBytes = fileInputStream.read(buffer);
            while (numberReadBytes != -1) {
                dataOutputStream.write(buffer, 0, numberReadBytes);
                numberReadBytes = fileInputStream.read(buffer);
            }

        } else {
            dataOutputStream.writeLong(0L);
        }

        dataOutputStream.flush();
    }
}
