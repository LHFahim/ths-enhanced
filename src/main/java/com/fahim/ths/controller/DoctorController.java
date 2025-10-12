package com.fahim.ths.controller;

import com.fahim.ths.model.Appointment;
import com.fahim.ths.model.Prescription;
import com.fahim.ths.model.VisitSummary;
import com.fahim.ths.repo.DataStore;
import com.fahim.ths.util.PdfExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DoctorController {

    private final DataStore db = DataStore.get();

    // ================= appointment Table =================
    @FXML private TableView<Appointment> apptTable;
    @FXML private TableColumn<Appointment, String> idCol;
    @FXML private TableColumn<Appointment, String> patientCol;
    @FXML private TableColumn<Appointment, String> specialistCol;
    @FXML private TableColumn<Appointment, String> timeCol;
    @FXML private TableColumn<Appointment, String> locationCol;

    // ================= diagnosis & treatment =================
    @FXML private TextArea diagnosisArea;
    @FXML private TextArea planArea;

    // ================= prescriptions =================
    @FXML private TextField rxPatientIdField;
    @FXML private TextField rxMedField;
    @FXML private TextField rxDoseField;

    @FXML private TableView<Prescription> rxTable;
    @FXML private TableColumn<Prescription, String> rxIdCol;
    @FXML private TableColumn<Prescription, String> rxPatientCol;
    @FXML private TableColumn<Prescription, String> rxMedCol;
    @FXML private TableColumn<Prescription, String> rxDoseCol;
    @FXML private TableColumn<Prescription, String> rxApprovedCol;

    // ================= external booking =================
    @FXML private TextField patientIdField;
    @FXML private TextField facilityField;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private TextField reasonField;

    @FXML
    public void initialize() {
        // appointments
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));

        patientCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPatientId()));

        specialistCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSpecialist()));

        timeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTime() == null ? "" : c.getValue().getTime().toString()));

        locationCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocation()));

        // time spinners
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 10));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));

        // prescriptions table --- show pending first; approved visible too
        rxIdCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        rxPatientCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPatientId()));
        rxMedCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMedicine()));
        rxDoseCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDosage()));
        rxApprovedCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isApproved() ? "Yes" : "No"));

        refreshAppointments();
        refreshPrescriptions();
    }

    private void refreshAppointments() {
        apptTable.setItems(FXCollections.observableArrayList(db.getAppointments()));
    }

    private void refreshPrescriptions() {
        // show unapproved first, then approved
        var all = db.allPrescriptions().stream().toList();
        var pendingFirst = all.stream().filter(p -> !p.isApproved()).toList();
        var approved = all.stream().filter(Prescription::isApproved).toList();

        rxTable.setItems(FXCollections.observableArrayList());
        rxTable.getItems().addAll(pendingFirst);
        rxTable.getItems().addAll(approved);
        rxTable.refresh();
    }

    // ================= diagnosis =================
    @FXML
    private void saveDiagnosis(ActionEvent e) {
        Appointment selected = apptTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.ERROR, "Select an appointment first").show();
            return;
        }
        if ((diagnosisArea.getText() == null || diagnosisArea.getText().isBlank()) &&
                (planArea.getText() == null || planArea.getText().isBlank())) {
            new Alert(Alert.AlertType.ERROR, "Enter diagnosis or treatment plan").show();
            return;
        }

        db.addVisitSummary(new VisitSummary(
                selected.getId(),
                selected.getPatientId(),
                diagnosisArea.getText() == null ? "" : diagnosisArea.getText().trim(),
                planArea.getText() == null ? "" : planArea.getText().trim(),
                LocalDateTime.now()
        ));

        new Alert(Alert.AlertType.INFORMATION, "Diagnosis saved").show();
        diagnosisArea.clear();
        planArea.clear();
    }

    // ================= export visit summary =================
    @FXML
    private void exportSummary(ActionEvent e) {
        Appointment selected = apptTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.ERROR, "Select an appointment first").show();
            return;
        }

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        File file = fc.showSaveDialog(apptTable.getScene().getWindow());
        if (file != null) {
            try {
                PdfExporter.exportVisitSummary(file, db.getVisitSummary(selected.getId()));
                new Alert(Alert.AlertType.INFORMATION, "Summary exported: " + file.getName()).show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).show();
                ex.printStackTrace();
            }
        }
    }

    // ================= prescriptions =================
    @FXML
    private void addPrescription(ActionEvent e) {
        String pid = rxPatientIdField.getText() == null ? "" : rxPatientIdField.getText().trim();
        String med = rxMedField.getText() == null ? "" : rxMedField.getText().trim();
        String dose = rxDoseField.getText() == null ? "" : rxDoseField.getText().trim();

        if (pid.isEmpty() || med.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Enter patient ID and medicine").show();
            return;
        }
        if (dose.isEmpty()) dose = "1 tab daily";

        db.addPrescription(pid, med, dose); // starts as approved=false
        rxPatientIdField.clear();
        rxMedField.clear();
        rxDoseField.clear();

        refreshPrescriptions();
        new Alert(Alert.AlertType.INFORMATION, "Prescription added").show();
    }

    @FXML
    private void approveSelected(ActionEvent e) {
        Prescription sel = rxTable.getSelectionModel().getSelectedItem();

        if (sel == null) {
            new Alert(Alert.AlertType.ERROR, "Select a prescription first").show();
            return;
        }

        if (db.approvePrescription(sel.getId())) {
            refreshPrescriptions();
            new Alert(Alert.AlertType.INFORMATION, "Prescription approved").show();
        } else {
            new Alert(Alert.AlertType.ERROR, "Could not approve (not found)").show();
        }
    }

    @FXML
    private void declineSelected(ActionEvent e) {
        Prescription sel = rxTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.ERROR, "Select a prescription first").show();
            return;
        }
        if (db.removePrescription(sel.getId())) {
            refreshPrescriptions();
            new Alert(Alert.AlertType.INFORMATION, "Prescription declined/removed").show();
        } else {
            new Alert(Alert.AlertType.ERROR, "Could not remove (not found)").show();
        }
    }

    // ================= external booking =================
    @FXML
    private void makeExternalBooking(ActionEvent e) {
        if (patientIdField.getText() == null || patientIdField.getText().isBlank()
                || facilityField.getText() == null || facilityField.getText().isBlank()
                || datePicker.getValue() == null) {
            new Alert(Alert.AlertType.ERROR, "Fill in patient ID, hospital/clinic, and date").show();
            return;
        }

        LocalTime t = LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue());
        LocalDateTime dt = LocalDateTime.of(datePicker.getValue(), t);

        db.addAppointment(
                patientIdField.getText().trim(),
                "External Facility",
                dt,
                facilityField.getText().trim() + (reasonField.getText() == null || reasonField.getText().isBlank()
                        ? "" : (" (" + reasonField.getText().trim() + ")"))
        );

        new Alert(Alert.AlertType.INFORMATION, "External booking created").show();

        patientIdField.clear();
        facilityField.clear();
        reasonField.clear();
        datePicker.setValue(null);

        refreshAppointments();
    }

    // ================= telehealth call =================
    @FXML
    private void startTelehealth(ActionEvent e) {
        javafx.stage.Window owner = ((Node) e.getSource()).getScene().getWindow();
        TelehealthCallController.open(owner);
    }



    // ================= logout =================
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
