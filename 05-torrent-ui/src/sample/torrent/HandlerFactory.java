package sample.torrent;

import java.net.Socket;

public interface HandlerFactory {
    Runnable getHandler(Socket socket);
}
