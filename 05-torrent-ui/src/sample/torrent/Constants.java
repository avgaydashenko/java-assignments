package sample.torrent;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Constants {
    public static final short SERVER_PORT = 8081;
    public static final int LIST = 1;
    public static final int UPLOAD = 2;
    public static final int SOURCES = 3;
    public static final int UPDATE = 4;
    public static final int STAT = 1;
    public static final int GET = 2;
    public static final long REST_DELAY = 1000;
    public static final int BLOCK_SIZE = 2 * (1 << 10);
    public static final Path SAVE_PATH = Paths.get("src", "main", "resources", "save.txt");
}
