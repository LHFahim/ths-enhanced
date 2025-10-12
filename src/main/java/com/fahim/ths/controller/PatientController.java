package com.fahim.ths.controller;

import com.fahim.ths.model.Appointment;
import com.fahim.ths.model.Prescription;
import com.fahim.ths.model.VitalSign;
import com.fahim.ths.repo.DataStore;
import com.fahim.ths.util.CsvVitals;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PatientController {

    private final DataStore db = DataStore.get();
    private final String patientId = "P001"; // demo patient

    // ================= book consultation =================
    @FXML private TextField specialistField;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private TextField locationField;

    @FXML private TableView<Appointment> apptTable;
    @FXML private TableColumn<Appointment, String> idCol;
    @FXML private TableColumn<Appointment, String> specialistCol;
    @FXML private TableColumn<Appointment, String> timeCol;
    @FXML private TableColumn<Appointment, String> locationCol;

    // ================= prescription refills =================
    @FXML private TextField medField;
    @FXML private TextField doseField;
    @FXML private TableView<Prescription> rxTable;
    @FXML private TableColumn<Prescription, String> rxIdCol;
    @FXML private TableColumn<Prescription, String> rxMedCol;
    @FXML private TableColumn<Prescription, String> rxDoseCol;
    @FXML private TableColumn<Prescription, String> rxApprovedCol;

    // ================= vital signs =================
    @FXML private Spinner<Double> pulseSpin, tempSpin, respSpin, sysSpin, diaSpin;
    @FXML private TableView<VitalSign> vitalTable;
    @FXML private TableColumn<VitalSign, String> vPulseCol;
    @FXML private TableColumn<VitalSign, String> vTempCol;
    @FXML private TableColumn<VitalSign, String> vRespCol;
    @FXML private TableColumn<VitalSign, String> vSysCol;
    @FXML private TableColumn<VitalSign, String> vDiaCol;
    @FXML private TableColumn<VitalSign, String> vTimeCol;

    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ================= initialization =================
    @FXML
    public void initialize() {
        // spinners
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 10));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));

        pulseSpin.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(40, 180, 72, 1));
        tempSpin.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(34, 42, 37, 0.1));
        respSpin.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(10, 40, 16, 1));
        sysSpin.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(80, 200, 120, 1));
        diaSpin.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(50, 120, 80, 1));

        // appointment table
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        specialistCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSpecialist()));
        timeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTime() == null ? "" : dtFmt.format(c.getValue().getTime())));
        locationCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocation()));

        // prescription table
        rxIdCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        rxMedCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMedicine()));
        rxDoseCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDosage()));
        rxApprovedCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isApproved() ? "Yes" : "No"));

        // vital table
        vPulseCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf((int)c.getValue().getPulse())));
        vTempCol.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.1f", c.getValue().getTemperature())));
        vRespCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf((int)c.getValue().getRespiration())));
        vSysCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf((int)c.getValue().getSystolic())));
        vDiaCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf((int)c.getValue().getDiastolic())));
        vTimeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getRecordedAt() == null ? "" : dtFmt.format(c.getValue().getRecordedAt())));

        refreshTables();
    }

    private void refreshTables() {
        apptTable.setItems(FXCollections.observableArrayList(db.appointmentsFor(patientId)));
        rxTable.setItems(FXCollections.observableArrayList(
                db.allPrescriptions().stream().filter(r -> r.getPatientId().equals(patientId)).toList()));
        vitalTable.setItems(FXCollections.observableArrayList(db.vitalsFor(patientId)));
    }

    // ================= book consultation =================
    @FXML
    private void book(ActionEvent e) {
        LocalDate d = datePicker.getValue();
        if (d == null) {
            new Alert(Alert.AlertType.ERROR, "Choose a date").show();
            return;
        }
        LocalTime t = LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue());
        LocalDateTime dt = LocalDateTime.of(d, t);
        String loc = locationField.getText().isBlank() ? "Online" : locationField.getText().trim();

        db.addAppointment(patientId, specialistField.getText().trim(), dt, loc);

        specialistField.clear();
        locationField.clear();

        refreshTables();
    }

    // ================= prescription refill =================
    @FXML
    private void requestRefill(ActionEvent e) {
        if (medField.getText().isBlank()) {
            new Alert(Alert.AlertType.ERROR, "Enter a medicine").show();
            return;
        }

        String dose = doseField.getText().isBlank() ? "1 tab daily" : doseField.getText().trim();
        db.addPrescription(patientId, medField.getText().trim(), dose);

        medField.clear();
        doseField.clear();

        refreshTables();
    }

    // ================= vital signs =================
    @FXML
    private void saveVitals(ActionEvent e) {
        db.addVital(new VitalSign(patientId,
                pulseSpin.getValue(), tempSpin.getValue(), respSpin.getValue(),
                sysSpin.getValue(), diaSpin.getValue(), LocalDateTime.now()));
        refreshTables();
    }

    @FXML
    private void importVitalsCsv(ActionEvent e) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File f = fc.showOpenDialog(apptTable.getScene().getWindow());
        if (f != null) {
            try {
                for (var v : CsvVitals.read(patientId, f)) db.addVital(v);
                refreshTables();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "CSV import failed: " + ex.getMessage()).show();
            }
        }
    }

    @FXML
    private void logout(ActionEvent e) {
        try {
            javafx.scene.Scene scene = new javafx.scene.Scene(
                    javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml")),
                    600, 400);
            javafx.stage.Stage st = (javafx.stage.Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
            st.setScene(scene);
            st.centerOnScreen();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Logout failed: " + ex.getMessage()).show();
            ex.printStackTrace();
        }
    }

    @FXML
    private void startTelehealth(javafx.event.ActionEvent e) {
        javafx.stage.Window owner = ((javafx.scene.Node) e.getSource()).getScene().getWindow();
        TelehealthCallController.open(owner);
    }

}
