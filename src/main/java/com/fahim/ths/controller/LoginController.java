package com.fahim.ths.controller;

import com.fahim.ths.Response;
import com.fahim.ths.ServerMain;
import com.fahim.ths.Session;
import com.fahim.ths.ThsClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Map;

public class LoginController {

    @FXML private TextField usernameField;   // used for email
    @FXML private PasswordField passwordField;

    @FXML
    private void onLogin(ActionEvent e) {
        String email = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "please enter both email and password").show();
            return;
        }

        try {
            // talk to the running server
            ThsClient client = new ThsClient("127.0.0.1", ServerMain.PORT);

            // send both email + password as payload
            Response res = client.send("LOGIN", Map.of(
                    "email", email,
                    "password", password
            ));

            if (!res.ok) {
                new Alert(Alert.AlertType.ERROR, "login failed: " + res.error).show();
                return;
            }

            // save user in session for later
            Session.setCurrentUser(res.data);
            String role = String.valueOf(res.data.get("role")).toUpperCase();

            // choose FXML by role
            String fxml = switch (role) {
                case "PATIENT" -> "/fxml/PatientView.fxml";
                case "STAFF"   -> "/fxml/StaffView.fxml";
                case "DOCTOR"  -> "/fxml/DoctorView.fxml";
                default -> null;
            };

            if (fxml == null) {
                new Alert(Alert.AlertType.ERROR, "unsupported role: " + role).show();
                return;
            }

            // load next scene
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource(fxml)), 1000, 700);
            Stage st = (Stage) ((Node) e.getSource()).getScene().getWindow();
            st.setScene(scene);
            st.centerOnScreen();

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "login error: " + ex.getMessage()).show();
            ex.printStackTrace();
        }
    }
}
