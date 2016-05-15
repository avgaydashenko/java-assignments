package ru.spbau.mit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestTorrent {
    private static final Path TEST_DIRECTORY = Paths.get("src", "test", "resources", "test");
    private static final Path DIRECTORY1 = TEST_DIRECTORY.resolve("DIRECTORY1");
    private static final String FILE1 = "FILE1";
    private static final Path PATH1 = DIRECTORY1.resolve(FILE1);
    private static final long LENGTH1 = 37;
    private static final Path DIRECTORY2 = TEST_DIRECTORY.resolve("DIRECTORY2");
    private static final String FILE2 = "FILE2";
    private static final Path PATH2 = DIRECTORY2.resolve(FILE2);
    private static final long LENGTH2 = 93;
    private static final byte[] LOCALHOST = new byte[] {127, 0, 0, 1};

    Server server;
    Client client1, client2;

    @Before
    public void createTestDirectory() throws IOException {
        Files.createDirectory(TEST_DIRECTORY);

        Id.getInstance().reset();
        server = new ServerMain();
        server.start();
        client1 = new ClientMain((short) 1);
        client1.start(LOCALHOST);
        client2 = new ClientMain((short) 2);
        client2.start(LOCALHOST);
    }

    @After
    public void deleteTestDirectory() throws IOException {
        FileUtils.deleteDirectory(TEST_DIRECTORY.toFile());

        server.stop();
        client1.stop();
        client2.stop();
    }

    @Test
    public void testList() throws IOException {
        Files.createDirectory(DIRECTORY1);
        createFile(PATH1, LENGTH1);
        client1.upload(PATH1.toString());

        List<FileEntry> filesList = client2.getFilesList();
        assertEquals(Collections.singletonList(new FileEntry(0, FILE1, LENGTH1)), filesList);

        Files.createDirectory(DIRECTORY2);
        createFile(PATH2, LENGTH2);
        client2.upload(PATH2.toString());

        List<FileEntry> filesList2 = client1.getFilesList();
        assertEquals(Arrays.asList(new FileEntry(0, FILE1, LENGTH1), new FileEntry(1, FILE2, LENGTH2)), filesList2);
    }

    @Test
    public void testDownload() throws IOException, InterruptedException {
        Files.createDirectory(DIRECTORY1);
        createFile(PATH1, LENGTH1);
        client1.upload(PATH1.toString());

        Files.createDirectory(DIRECTORY2);
        Thread.sleep(1000);
        client2.download(0, DIRECTORY2);
        assertEquals(LENGTH1, DIRECTORY1.resolve(FILE1).toFile().length());
    }

    private void createFile(Path filePath, long fileLength) throws IOException {
        RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "rw");
        file.setLength(fileLength);
        file.close();
    }
}
