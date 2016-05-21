package sample.torrent;

import java.net.Socket;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class P2PClientHandlerFactory implements HandlerFactory {
    private Map<Integer, Set<Integer>> availableFileParts;
    private Map<Integer, Path> filesPath;

    public P2PClientHandlerFactory(Map<Integer, Set<Integer>> availableFileParts,
                                   Map<Integer, Path> filesPath) {
        this.availableFileParts = availableFileParts;
        this.filesPath = filesPath;
    }

    @Override
    public Runnable getHandler(Socket socket) {
        return new P2PClientHandler(socket, availableFileParts, filesPath);
    }
}
