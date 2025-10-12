package com.fahim.ths;

import com.fahim.ths.repo.DatabaseInit;   // <-- add this import

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            // init database and create tables automatically
            DatabaseInit.init();
            // seed demo users so the ui has data
            DatabaseInit.seed();
        } catch (Exception ex) {
            // show a clear error and exit if db cannot be created
            Alert a = new Alert(Alert.AlertType.ERROR,
                    "database initialization failed:\n" + ex.getMessage() +
                            "\n\ncheck db.properties and mysql is running.");
            a.showAndWait();
            Platform.exit();
            return;
        }

        Scene scene = new Scene(
                FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml")),
                960, 620
        );
        stage.setTitle("THS - TeleHealth System (Initial Prototype)");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args){
        launch();
    }
}
