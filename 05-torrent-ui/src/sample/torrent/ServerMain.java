package sample.torrent;

import java.util.*;

public class ServerMain extends AbstractServer {
    public ServerMain() {
        super(Constants.SERVER_PORT);
        List<FileEntry> filesList = Collections.synchronizedList(new ArrayList<>());

        Map<ClientInformation, Set<Integer>> clientSeededFiles = Collections.synchronizedMap(new HashMap<>());

        Map<ClientInformation, TimerTask> toRemoveClientTasks = Collections.synchronizedMap(new HashMap<>());

        setHandlerFactory(new TrackerClientHandlerFactory(filesList, clientSeededFiles, toRemoveClientTasks));
    }

    public static void main(String[] args) {
        Server server = new ServerMain();
        server.start();
    }
}
