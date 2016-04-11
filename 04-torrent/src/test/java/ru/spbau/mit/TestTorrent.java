package ru.spbau.mit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TestTorrent {

    private static final Path EXAMPLE_PATH = Paths.get("src", "test", "resources", "ConnectLog.txt");
    private static final Path TRACKER_DIR = Paths.get("test", "tracker");
    private static final Path CLIENT1_DIR = Paths.get("test", "client-01");
    private static final Path CLIENT2_DIR = Paths.get("test", "client-02");
    private static final Path CLIENT3_DIR = Paths.get("test", "client-03");
    private static final long TIME_LIMIT = 70 * 1000L;

    @Test
    public void testListAndUpload() throws Throwable {
        try (
                Tracker tracker = new Tracker(TRACKER_DIR);
                Client client1 = new Client("localhost", CLIENT1_DIR);
                Client client2 = new Client("localhost", CLIENT2_DIR)
        ) {
            assertAllCollectionEquals(Collections.emptyList(), client1.list(), client2.list());

            FileEntry entry1 = client1.newFile(EXAMPLE_PATH);
            FileEntry entry2 = client2.newFile(EXAMPLE_PATH);
            assertNotEquals("Should be different ids", entry1.getId(), entry2.getId());

            assertAllCollectionEquals(Arrays.asList(entry1, entry2), client1.list(), client2.list());
        }
    }

    @Test
    public void testListConsistency() throws Throwable {
        try (Client client = new Client("localhost", CLIENT1_DIR)) {
            FileEntry entry;
            try (Tracker tracker = new Tracker(TRACKER_DIR)) {
                entry = client.newFile(EXAMPLE_PATH);
            }

            List<FileEntry> list;
            try (Tracker tracker = new Tracker(TRACKER_DIR)) {
                list = client.list();
            }
            assertEquals(Collections.singletonList(entry), list);
        }
    }

    @Test(timeout = TIME_LIMIT)
    public void testDownload() throws Throwable {
        final DownloadWaiter waiter2 = new DownloadWaiter();
        final DownloadWaiter waiter3 = new DownloadWaiter();
        FileEntry entry;
        try (
                Tracker tracker = new Tracker(TRACKER_DIR);
                Client client2 = new Client("localhost", CLIENT2_DIR)
        ) {
            client2.setCallbacks(waiter2);
            try (Client client1 = new Client("localhost", CLIENT1_DIR)) {
                entry = client1.newFile(EXAMPLE_PATH);
                assertTrue(client2.get(entry.getId()));

                client1.run();
                client2.run();

                synchronized (waiter2) {
                    while (!waiter2.ready) {
                        waiter2.wait();
                    }
                }
            }

            try (Client client3 = new Client("localhost", CLIENT3_DIR)) {
                client3.setCallbacks(waiter3);
                assertTrue(client3.get(entry.getId()));

                client3.run();
                synchronized (waiter3) {
                    while (!waiter3.ready) {
                        waiter3.wait();
                    }
                }
            }
        }

        Path downloadedPath = Paths.get("downloads", Integer.toString(entry.getId()),
                EXAMPLE_PATH.getFileName().toString());
        assertTrue("Downloaded file is different!", FileUtils.contentEquals(EXAMPLE_PATH.toFile(),
                CLIENT2_DIR.resolve(downloadedPath).toFile()));
        assertTrue("Downloaded file is different!", FileUtils.contentEquals(EXAMPLE_PATH.toFile(),
                CLIENT3_DIR.resolve(downloadedPath).toFile()));
    }

    @Before
    @After
    public void clear() throws IOException {
        clearDirectory("test");
    }

    private void clearDirectory(String name) throws IOException {
        Path path = Paths.get(name);
        if (!Files.exists(path)) {
            return;
        }

        Files.walkFileTree(path, new Deleter());
    }

    private void assertAllCollectionEquals(Collection<?> expected, Collection<?>... others) {
        for (Collection<?> other : others) {
            assertEquals(expected, other);
        }
    }

    private static class Deleter extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return super.visitFile(file, attrs);
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return super.postVisitDirectory(dir, exc);
        }
    }

    private static final class DownloadWaiter implements Client.StatusCallbacks {
        private boolean ready = false;

        private DownloadWaiter() {
        }

        @Override
        public void onTrackerUpdated(boolean result, Throwable e) {
        }

        @Override
        public void onDownloadIssue(FileEntry entry, String message, Throwable e) {
        }

        @Override
        public void onDownloadStart(FileEntry entry) {
        }

        @Override
        public void onDownloadPart(FileEntry entry, int partId) {
        }

        @Override
        public void onDownloadComplete(FileEntry entry) {
            synchronized (this) {
                ready = true;
                notify();
            }
        }

        @Override
        public void onConnectionServerIssue(Throwable e) {
        }
    }
}
