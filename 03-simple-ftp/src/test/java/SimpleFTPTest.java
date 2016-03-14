import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.*;

public class SimpleFTPTest {

    private static final int MIN_PORT = 10000;
    private static final int MAX_PORT = 20000;

    @Test
    public void testGet() throws IOException {

        Path path = Files.createTempDirectory("FTP");
        File file = new File(path.toString() + File.separator + "test1");

        PrintWriter printWriter = new PrintWriter(file);

        String fileString = "test2";
        printWriter.print(fileString);
        printWriter.close();

        OutputStream outputStream = new OutputStream() {
            private StringBuilder stringBuilder = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                this.stringBuilder.append((char) b);
            }

            public String toString() {
                return this.stringBuilder.toString();
            }
        };

        Random random = new Random();
        int port = random.nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;

        Server server;
        while (true) {
            try {
                server = new Server(port);
                break;
            } catch (BindException ignored) {
                port = random.nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;
            }
        }

        server.start();

        Client client = new Client("localhost", port);

        try {
            assertEquals(client.get(path.toString() + File.separator + "test1", outputStream), fileString.length());
            assertEquals(outputStream.toString(), fileString);
        } finally {
            server.stop();
            client.close();
        }

        System.out.println("First test completed");
    }

    @Test
    public void testList() throws IOException {
        String[] fileName = new String[]{"dir1", "dir2", "dir3", "dir4", "file1", "file2", "file3", "file4"};

        Path path = Files.createTempDirectory("FTP");

        for (int i = 0; i < 4; ++i) {
            (new File(path.toString() + File.separator + fileName[i])).mkdir();
        }

        for (int i = 4; i < 8; ++i) {
            (new File(path.toString() + File.separator + fileName[i])).createNewFile();
        }

        Random rnd = new Random();
        int port = rnd.nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;

        Server server;
        while (true) {
            try {
                server = new Server(port);
                break;
            } catch (BindException e) {
                port = rnd.nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;
            }
        }

        server.start();

        Client client = new Client("localhost", port);

        try {
            ArrayList<Client.FileDescription> fileDescriptionArrayList = client.list(path.toString());
            assertEquals(fileDescriptionArrayList.size(), fileName.length);
            fileDescriptionArrayList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));

            for (int i = 0; i < 4; ++i) {
                assertEquals(fileDescriptionArrayList.get(i).getIsDir(), true);
            }

            for (int i = 4; i < 8; ++i) {
                assertEquals(fileDescriptionArrayList.get(i).getIsDir(), false);
            }

            for (int i = 0; i < 8; ++i) {
                assertEquals(fileDescriptionArrayList.get(i).getName(), fileName[i]);
            }

            new File(path.toString()).delete();
        } finally {
            server.stop();
            client.close();
        }
    }
}