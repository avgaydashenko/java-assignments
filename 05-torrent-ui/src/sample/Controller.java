package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import sample.torrent.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {

    public static Client[] client = new Client[Short.MAX_VALUE];
    public static Server server;
    public static short clientId;

    public static double progress = 0;

    public ProgressBar progressBar = new ProgressBar();
    public ListView<String> downloadsList;
    public Label chosenFile;
    public TextField portField;
    public TextField uploadFileField;

    public void onClickShowDownloads(ActionEvent actionEvent) throws IOException {
        refreshDownloadsList();
    }

    public void onClickDownloadFile(ActionEvent actionEvent) throws IOException {
        progress = 0;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (progress == 1) {
                    timer.cancel();
                    return;
                }
                synchronized (progressBar) {
                    Platform.runLater(() -> progressBar.setProgress(progress));
                }
            }
        }, 0, 500);
        client[clientId].download(0, Paths.get(chosenFile.getText()));
        //progressBar.setProgress(1);
    }

    public void onClickUploadFile(ActionEvent actionEvent) throws IOException {
        client[clientId].upload(uploadFileField.getText());
        refreshDownloadsList();
    }

    public void refreshChosenFile(Event event) {
        chosenFile.setText(downloadsList.getSelectionModel().getSelectedItem());
    }

    public void onPortEntered(ActionEvent actionEvent) throws IOException {
        clientId = (short) Integer.parseInt(portField.getText());

        if (client[clientId] == null) {
            client[clientId] = new ClientMain(clientId);
            client[clientId].start(new byte[]{127, 0, 0, 1});
        }
    }

    public void onClickStop(ActionEvent actionEvent) throws IOException {
        server.stop();
        for (short i = 0; i < Short.MAX_VALUE; i++)
            if (client[i] != null)
                client[i].stop();
    }

    private void refreshDownloadsList() throws IOException {
        List<FileEntry> filesList = client[clientId].getFilesList();
        ArrayList<String> fileNames = new ArrayList<>();

        for (FileEntry fileEntry : filesList) {
            fileNames.add(fileEntry.getName());
        }

        ObservableList<String> items = FXCollections.observableArrayList(fileNames);
        downloadsList.setItems(items);
        progressBar.setProgress(0);
    }

}
