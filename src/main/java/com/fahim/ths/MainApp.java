package com.fahim.ths;

import com.fahim.ths.repo.DatabaseInit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Map;

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
                            "\n\ncheck db.properties and that mysql is running.");
            a.showAndWait();
            Platform.exit();
            return;
        }

        // verify the socket server is reachable
        try {
            ThsClient client = new ThsClient("127.0.0.1", ServerMain.PORT);
            Response ping = client.send("PING", Map.of());
            if (!(ping.ok && "PONG".equals(String.valueOf(ping.data.get("reply"))))) {
                throw new RuntimeException("unexpected ping reply");
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.WARNING,
                    "server not reachable on localhost:" + ServerMain.PORT +
                            "\nstart ServerMain in another run configuration to enable live features.\n\n" +
                            ex.getMessage()).showAndWait();

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
