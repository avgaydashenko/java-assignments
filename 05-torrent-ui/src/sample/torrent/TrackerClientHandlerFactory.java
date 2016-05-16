package sample.torrent;

import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

public class TrackerClientHandlerFactory implements HandlerFactory {
    private List<FileEntry> filesList;
    private Map<ClientInformation, Set<Integer>> clientSeededFiles;
    private Map<ClientInformation, TimerTask> toRemoveClientTasks;

    TrackerClientHandlerFactory(List<FileEntry> filesList, Map<ClientInformation, Set<Integer>> clientSeededFiles,
                                Map<ClientInformation, TimerTask> toRemoveClientTasks) {
        this.filesList = filesList;
        this.clientSeededFiles = clientSeededFiles;
        this.toRemoveClientTasks = toRemoveClientTasks;
    }

    @Override
    public Runnable getHandler(Socket socket) {
        return new TrackerClientHandler(socket, filesList, clientSeededFiles, toRemoveClientTasks);
    }
}
