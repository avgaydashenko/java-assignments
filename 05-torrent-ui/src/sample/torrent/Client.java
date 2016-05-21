package sample.torrent;

import javafx.scene.control.ProgressBar;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface Client {
    void start(byte[] ip) throws IOException;
    void stop() throws IOException;
    List<FileEntry> getFilesList() throws IOException;
    void download(int fileId, Path path, ProgressBar progressBar) throws IOException;
    void upload(String path) throws IOException;
    void save() throws IOException;
    void restore() throws IOException;
}
