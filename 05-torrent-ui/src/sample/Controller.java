package sample;

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

public class Controller {

    public static Client client;
    public static Server server;

    public ListView<String> downloadsList;
    public Label chosenFile;
    public ProgressBar progressBar;
    public TextField portField;
    public TextField uploadFileField;

    private String uploadFile;

    public void onClickShowDownloads(ActionEvent actionEvent) throws IOException {
        refreshDownloadsList();
    }

    public void onClickDownloadFile(ActionEvent actionEvent) throws IOException {
        client.download(0, Paths.get(chosenFile.getText()));

    }

    public void onClickUploadFile(ActionEvent actionEvent) throws IOException {
        client.upload(uploadFile);
        refreshDownloadsList();
    }

    public void refreshChosenFile(Event event) {
        chosenFile.setText(downloadsList.getSelectionModel().getSelectedItem());
    }

    public void onPortEntered(ActionEvent actionEvent) throws IOException {
        server = new ServerMain();
        server.start();

        client = new ClientMain((short) Integer.parseInt(portField.getText()));
        client.start(new byte[]{127, 0, 0, 1});
    }

    public void onUploadFileEntered(ActionEvent actionEvent) {
        uploadFile = uploadFileField.getText();
    }

    public void onClickStop(ActionEvent actionEvent) {
        server.stop();
    }

    private void refreshDownloadsList() throws IOException {
        List<FileEntry> filesList = client.getFilesList();
        ArrayList<String> fileNames = new ArrayList<>();

        for (FileEntry fileEntry : filesList) {
            fileNames.add(fileEntry.getName());
        }

        ObservableList<String> items = FXCollections.observableArrayList(fileNames);
        downloadsList.setItems(items);
    }

}
