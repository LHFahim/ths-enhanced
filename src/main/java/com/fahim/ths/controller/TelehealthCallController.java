package com.fahim.ths.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * A tiny simulated telehealth-call window:
 * - shows connected status
 * - call timer
 * - mute/unmute
 * - simulate network drop
 * - end call
 */
public class TelehealthCallController {

    @FXML private Label statusLabel;
    @FXML private Label timerLabel;
    @FXML private TextArea notesArea;
    @FXML private Button muteBtn;

    private boolean muted = false;
    private int seconds = 0;
    private Timeline timer;

    @FXML
    public void initialize() {
        // this starts a simple mm:ss timer
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            seconds++;
            int m = seconds / 60;
            int s = seconds % 60;
            timerLabel.setText(String.format("%02d:%02d", m, s));
        }));

        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    @FXML
    private void toggleMute(ActionEvent e) {
        muted = !muted;
        muteBtn.setText(muted ? "Unmute" : "Mute");
        statusLabel.setText(muted ? "Connected (Muted)" : "Connected");
    }

    @FXML
    private void simulateNetworkDrop(ActionEvent e) {
        statusLabel.setText("Reconnecting…");
        // briefly pause timer to simulate a hiccup
        timer.pause();
        // resume after 2 seconds
        Timeline resume = new Timeline(new KeyFrame(Duration.seconds(2), ev -> {
            statusLabel.setText(muted ? "Connected (Muted)" : "Connected");
            timer.play();
        }));
        resume.play();
    }

    @FXML
    private void endCall(ActionEvent e) {
        if (timer != null) timer.stop();
        Stage st = (Stage) statusLabel.getScene().getWindow();
        st.close();
    }

    /** static helper to open this window from any controller */
    public static void open(javafx.stage.Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(TelehealthCallController.class.getResource("/fxml/TelehealthCall.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Telehealth Call (Simulated)");
            stage.setScene(scene);
            stage.initOwner(owner);
            stage.initModality(Modality.NONE); // a separate window; keep app usable
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();

        }
    }
}
