package com.fahim.ths;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml")), 960, 620);
        stage.setTitle("THS - TeleHealth System (Initial Prototype)");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args){ launch(); }
}
