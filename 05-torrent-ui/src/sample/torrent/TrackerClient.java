package sample.torrent;

import java.io.IOException;
import java.util.List;

public interface TrackerClient {
    void connect(byte[] ip, int port) throws IOException;
    void disconnect() throws IOException;
    List<FileEntry> executeList() throws IOException;
    int executeUpload(String path, long size) throws IOException;
    List<ClientInformation> executeSources(int id) throws IOException;
    boolean executeUpdate(short port, List<Integer> seededFiles) throws IOException;
}
