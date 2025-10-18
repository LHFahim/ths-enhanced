package com.fahim.ths.controller;

import com.fahim.ths.Response;
import com.fahim.ths.ServerMain;
import com.fahim.ths.ThsClient;

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
import java.util.List;
import java.util.Map;

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

    // ================= alerts (NEW tab) =================
    @FXML private TextField alertPatientEmailField;
    @FXML private TableView<AlertRow> alertsTable;
    @FXML private TableColumn<AlertRow, String> aTypeCol;
    @FXML private TableColumn<AlertRow, String> aSeverityCol;
    @FXML private TableColumn<AlertRow, String> aMessageCol;
    @FXML private TableColumn<AlertRow, String> aWhenCol;

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

        // prescriptions table
        rxIdCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        rxPatientCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPatientId()));
        rxMedCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMedicine()));
        rxDoseCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDosage()));
        rxApprovedCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isApproved() ? "Yes" : "No"));

        // alerts table (NEW)
        if (alertsTable != null) { // safe if tab not loaded yet
            aTypeCol.setCellValueFactory(c -> c.getValue().typeProp());
            aSeverityCol.setCellValueFactory(c -> c.getValue().severityProp());
            aMessageCol.setCellValueFactory(c -> c.getValue().messageProp());
            aWhenCol.setCellValueFactory(c -> c.getValue().whenProp());
            alertsTable.setItems(FXCollections.observableArrayList());
        }

        refreshAppointments();
        refreshPrescriptions();
    }

    private void refreshAppointments() {
        try {
            Integer doctorId = com.fahim.ths.Session.currentUserId();
            if (doctorId == null) { apptTable.setItems(FXCollections.observableArrayList()); return; }

            ThsClient c = new ThsClient("127.0.0.1", ServerMain.PORT);
            Response r = c.send("LIST_APPTS_FOR_DOCTOR", Map.of("doctor_id", doctorId));
            if (!r.ok) { apptTable.setItems(FXCollections.observableArrayList()); return; }

            @SuppressWarnings("unchecked")
            List<Map<String,Object>> rows = (List<Map<String,Object>>) r.data.get("appointments");

            // Map the DB rows to your Appointment model for display (best-effort mapping)
            var list = rows.stream().map(m -> {
                String id = String.valueOf(m.get("id"));
                String pid = String.valueOf(m.get("patient_id"));
                String specialist = "Consultation"; // or use patient/doctor names as you prefer
                String time = String.valueOf(m.get("start_time"));
                String location = String.valueOf(m.getOrDefault("notes", "Online"));
                // Your Appointment has (id, patientId, specialist, LocalDateTime, location):
                LocalDateTime ldt;
                try { ldt = LocalDateTime.parse(time.replace(' ', 'T')); } catch (Exception e) { ldt = null; }
                return new Appointment(id, pid, specialist, ldt, location);
            }).toList();

            apptTable.setItems(FXCollections.observableArrayList(list));
            apptTable.refresh();
        } catch (Exception ex) {
            apptTable.setItems(FXCollections.observableArrayList());
            ex.printStackTrace();
        }
    }

    private void refreshPrescriptions() {
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
                blankToEmpty(diagnosisArea.getText()),
                blankToEmpty(planArea.getText()),
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
        String pid = text(rxPatientIdField);
        String med = text(rxMedField);
        String dose = text(rxDoseField);

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
        if (text(patientIdField).isBlank() || text(facilityField).isBlank() || datePicker.getValue() == null) {
            new Alert(Alert.AlertType.ERROR, "Fill in patient ID, hospital/clinic, and date").show();
            return;
        }

        LocalTime t = LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue());
        LocalDateTime dt = LocalDateTime.of(datePicker.getValue(), t);

        db.addAppointment(
                text(patientIdField),
                "External Facility",
                dt,
                text(facilityField) + (text(reasonField).isBlank() ? "" : (" (" + text(reasonField) + ")"))
        );

        new Alert(Alert.AlertType.INFORMATION, "External booking created").show();

        patientIdField.clear();
        facilityField.clear();
        reasonField.clear();
        datePicker.setValue(null);

        refreshAppointments();
    }

    // ================= Alerts tab actions (NEW) =================
    @FXML
    private void onLoadAlerts() {
        try {
            String email = text(alertPatientEmailField);
            if (email.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Enter patient email").show();
                return;
            }

            ThsClient c = new ThsClient("127.0.0.1", ServerMain.PORT);

            // 1) resolve patient id from email
            Response find = c.send("FIND_USER_BY_EMAIL", Map.of("email", email));
            if (!find.ok) {
                new Alert(Alert.AlertType.ERROR, "No such user: " + email).show();
                return;
            }
            int pid = ((Number) find.data.get("id")).intValue();

            // 2) fetch alerts for that patient
            Response r = c.send("LIST_ALERTS_FOR_PATIENT", Map.of("patient_id", pid, "limit", 200));
            if (!r.ok) {
                new Alert(Alert.AlertType.ERROR, "Failed to load alerts: " + r.error).show();
                return;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) r.data.get("alerts");
            var list = rows.stream().map(AlertRow::fromMap).toList();
            alertsTable.getItems().setAll(list);

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).show();
            ex.printStackTrace();
        }
    }

    // tiny row model for alerts
    public static class AlertRow {
        private final javafx.beans.property.SimpleStringProperty type =
                new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.SimpleStringProperty severity =
                new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.SimpleStringProperty message =
                new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.SimpleStringProperty when =
                new javafx.beans.property.SimpleStringProperty();

        public AlertRow(String type, String severity, String message, String when) {
            this.type.set(type); this.severity.set(severity); this.message.set(message); this.when.set(when);
        }

        public static AlertRow fromMap(Map<String, Object> m) {
            String type = String.valueOf(m.get("type"));
            String sev  = String.valueOf(m.get("severity"));
            String msg  = String.valueOf(m.get("message"));
            String at   = String.valueOf(m.get("created_at"));
            return new AlertRow(type, sev, msg, at);
        }

        public javafx.beans.property.SimpleStringProperty typeProp(){ return type; }
        public javafx.beans.property.SimpleStringProperty severityProp(){ return severity; }
        public javafx.beans.property.SimpleStringProperty messageProp(){ return message; }
        public javafx.beans.property.SimpleStringProperty whenProp(){ return when; }
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

    // ===== helpers =====
    private String text(TextField tf) { return tf.getText() == null ? "" : tf.getText().trim(); }
    private String blankToEmpty(String s) { return (s == null || s.isBlank()) ? "" : s.trim(); }
}
