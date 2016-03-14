import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Client {

    private static final int LIST = 1;
    private static final int GET = 2;
    private static final int BUFFER = (int) Math.pow(2, 21);

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public class FileDescription {

        private String name;
        private Boolean isDir;

        public Boolean getIsDir() {
            return isDir;
        }

        public void setIsDir(Boolean isDir) {
            this.isDir = isDir;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public Client(String host, int port) throws IOException {
        socket = new Socket(host, port);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    public ArrayList<FileDescription> list(String path) throws IOException {

        ArrayList<FileDescription> fileDescriptionArrayList = new ArrayList<>();

        dataOutputStream.writeInt(LIST);
        dataOutputStream.writeUTF(path);
        dataOutputStream.flush();

        int size = dataInputStream.readInt();
        for (int i = 0; i < size; i++) {
            FileDescription fileDescription = new FileDescription();
            fileDescription.setName(dataInputStream.readUTF());
            fileDescription.setIsDir(dataInputStream.readBoolean());
            fileDescriptionArrayList.add(fileDescription);
        }

        return fileDescriptionArrayList;
    }

    public long get(String path, OutputStream outputStream) throws IOException {

        dataOutputStream.writeInt(GET);
        dataOutputStream.writeUTF(path);
        dataOutputStream.flush();

        long size = dataInputStream.readLong();
        byte[] buffer = new byte[BUFFER];

        for (long i = 0; i < size; ) {
            int numberReadBytes = dataInputStream.read(buffer, 0, (int) Math.min(BUFFER, size - i));
            outputStream.write(buffer, 0, numberReadBytes);
            i += numberReadBytes;
        }

        return size;
    }

    public synchronized void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
            System.out.println("Failed to close socket (client)");
        }
    }
}
