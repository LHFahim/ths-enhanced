package com.fahim.ths.controller;

import com.fahim.ths.model.Appointment;
import com.fahim.ths.repo.DataStore;
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

public class StaffController {

    // table
    @FXML private TableView<Appointment> apptTable;
    @FXML private TableColumn<Appointment, String> idCol;
    @FXML private TableColumn<Appointment, String> patientCol;
    @FXML private TableColumn<Appointment, String> specialistCol;
    @FXML private TableColumn<Appointment, String> timeCol;
    @FXML private TableColumn<Appointment, String> locationCol;

    // edit controls
    @FXML private TextField apptIdField;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private TextField locationField;

    private final DataStore db = DataStore.get();

    @FXML
    public void initialize() {
        // bind columns
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        patientCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPatientId()));
        specialistCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSpecialist()));
        timeCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTime() == null ? "" : c.getValue().getTime().toString()));
        locationCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocation()));

        // time spinners
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));

        refreshTable();
    }

    private void refreshTable() {
        apptTable.setItems(FXCollections.observableArrayList(db.getAppointments()));
        apptTable.refresh(); // force reload so changes are visible immediately
    }

    @FXML
    private void changeBooking(ActionEvent e) {
        String id = apptIdField.getText() == null ? "" : apptIdField.getText().trim();
        if (id.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Enter an appointment ID from the table above.").show();
            return;
        }

        Appointment appt = db.findAppointmentById(id);
        if (appt == null) {
            new Alert(Alert.AlertType.ERROR, "Appointment not found: " + id).show();
            return;
        }

        LocalDate d = datePicker.getValue();
        if (d == null) {
            new Alert(Alert.AlertType.ERROR, "Pick a new date.").show();
            return;
        }
        LocalTime t = LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue());
        LocalDateTime dt = LocalDateTime.of(d, t);

        // update time/location
        appt.setTime(dt);
        String newLoc = locationField.getText() == null ? "" : locationField.getText().trim();
        if (!newLoc.isEmpty()) appt.setLocation(newLoc);


        refreshTable();
        apptTable.refresh();

        new Alert(Alert.AlertType.INFORMATION, "Appointment updated.").show();
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
