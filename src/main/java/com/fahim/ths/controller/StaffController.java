package com.fahim.ths.controller;

import com.fahim.ths.Response;
import com.fahim.ths.ServerMain;
import com.fahim.ths.ThsClient;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;


public class StaffController {

    // table
    @FXML private TableView<Map<String, Object>> apptTable;
    @FXML private TableColumn<Map<String, Object>, String> idCol;
    @FXML private TableColumn<Map<String, Object>, String> patientCol;
    @FXML private TableColumn<Map<String, Object>, String> doctorCol;
    @FXML private TableColumn<Map<String, Object>, String> timeCol;
    @FXML private TableColumn<Map<String, Object>, String> locationCol;
    @FXML private TableColumn<Map<String, Object>, String> statusCol;

    // edit controls
    @FXML private TextField apptIdField;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private TextField locationField;

    @FXML
    public void initialize() {
        // bind columns
        idCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("id"))));
        patientCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("patient_name", "N/A"))));
        doctorCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("doctor_name", "N/A"))));
        timeCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("start_time", ""))));
        locationCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("notes", "Online"))));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrDefault("status", ""))));

        // spinners
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));

        refreshTable();
    }

    private void refreshTable() {
        try {
            ThsClient c = new ThsClient("127.0.0.1", ServerMain.PORT);

            Response r = c.send("LIST_APPTS_FOR_DOCTOR", Map.of("doctor_id", 1)); // or any doctor id
            if (!r.ok) {
                apptTable.setItems(FXCollections.observableArrayList());
                new Alert(Alert.AlertType.ERROR, "Failed to load appointments: " + r.error).show();
                return;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) r.data.get("appointments");
            apptTable.setItems(FXCollections.observableArrayList(rows));
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error loading appointments: " + ex.getMessage()).show();
        }
    }

    @FXML
    private void changeBooking(ActionEvent e) {
        var sel = apptTable.getSelectionModel().getSelectedItem();
        String idText = apptIdField.getText();
        int id;

        try {
            if (sel != null) {
                id = ((Number) sel.get("id")).intValue();
            } else if (idText != null && !idText.isBlank()) {
                id = Integer.parseInt(idText.trim());
            } else {
                new Alert(Alert.AlertType.ERROR, "Select or enter an appointment ID first.").show();
                return;
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Invalid appointment ID.").show();
            return;
        }

        LocalDate d = datePicker.getValue();
        if (d == null) {
            new Alert(Alert.AlertType.ERROR, "Pick a new date.").show();
            return;
        }

        LocalTime t = LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue());
        LocalDateTime start = LocalDateTime.of(d, t);
        LocalDateTime end = start.plusMinutes(30);
        String newLoc = locationField.getText() == null ? "" : locationField.getText().trim();

        try {
            ThsClient c = new ThsClient("127.0.0.1", ServerMain.PORT);
            Response r = c.send("UPDATE_APPOINTMENT", Map.of(
                    "id", id,
                    "start_time", start.toString(),
                    "end_time", end.toString(),
                    "location", newLoc.isEmpty() ? "Online" : newLoc,
                    "status", "BOOKED",
                    "notes", "Edited by staff"
            ));

            if (!r.ok) {
                new Alert(Alert.AlertType.ERROR, r.error).show();
                return;
            }

            new Alert(Alert.AlertType.INFORMATION, "Appointment updated successfully.").show();
            refreshTable();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).show();
            ex.printStackTrace();
        }
    }

    @FXML
    private void logout(ActionEvent e) {
        try {
            Scene scene = new Scene(
                    FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml")),
                    600, 400);
            Stage st = (Stage) ((Node) e.getSource()).getScene().getWindow();
            st.setScene(scene);
            st.centerOnScreen();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Logout failed: " + ex.getMessage()).show();
            ex.printStackTrace();
        }
    }
}
