package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.torrent.ServerMain;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Torrent");
        primaryStage.setScene(new Scene(root, 509, 323));
        primaryStage.show();

        Controller.server = new ServerMain();
        Controller.server.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
