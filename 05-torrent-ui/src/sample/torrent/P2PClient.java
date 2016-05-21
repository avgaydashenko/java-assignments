package sample.torrent;

import java.io.IOException;
import java.util.List;

public interface P2PClient {
    void connect(byte[] ip, short port) throws IOException;
    void disconnect() throws IOException;
    List<Integer> executeStat(int id) throws IOException;
    byte[] executeGet(int id, int part) throws IOException;
}
