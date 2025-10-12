package com.fahim.ths.controller;

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

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private static final Map<String, Cred> USERS = Map.of(
            "patient", new Cred(Role.PATIENT, "pass123"),
            "staff",   new Cred(Role.STAFF,   "pass123"),
            "doctor",  new Cred(Role.DOCTOR,  "pass123")
    );

    private enum Role { PATIENT, STAFF, DOCTOR }

    private record Cred(Role role, String password) {}

    @FXML
    private void onLogin(ActionEvent e) {
        String u = usernameField.getText() == null ? "" : usernameField.getText().trim().toLowerCase();
        String p = passwordField.getText() == null ? "" : passwordField.getText();

        Cred cred = USERS.get(u);
        if (cred == null || !cred.password.equals(p)) {
            new Alert(Alert.AlertType.ERROR, "Invalid username or password.\nTry patient/staff/doctor with pass123.")
                    .show();
            return;
        }

        String fxml = switch (cred.role) {
            case PATIENT -> "/fxml/PatientView.fxml";
            case STAFF   -> "/fxml/StaffView.fxml";
            case DOCTOR  -> "/fxml/DoctorView.fxml";
        };

        try {
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource(fxml)), 1000, 700);
            Stage st = (Stage) ((Node) e.getSource()).getScene().getWindow();
            st.setScene(scene);
            st.centerOnScreen();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Failed to load screen: " + ex.getMessage()).show();
            ex.printStackTrace();
        }
    }
}
