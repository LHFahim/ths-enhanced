package com.fahim.ths.controller;

import com.fahim.ths.Response;
import com.fahim.ths.ServerMain;
import com.fahim.ths.Session;
import com.fahim.ths.ThsClient;

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
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;

/**
 * PatientController:
 * - Appointments & Prescriptions remain backed by in-memory DataStore (as before).
 * - Vitals are now sent to the server (ADD_VITALS) and listed from MySQL (LIST_VITALS_FOR_PATIENT).
 *   The server will auto-create alerts if readings are abnormal.
 */
public class PatientController {

    private final DataStore db = DataStore.get(); // still used for appts/prescriptions demo
    // NOTE: patientId for vitals comes from Session.currentUserId() now (numeric from DB)

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

    // ================= vital signs (now backed by server/db) =================
    @FXML private Spinner<Double> pulseSpin, tempSpin, respSpin, sysSpin, diaSpin;
    @FXML private TableView<VitalSign> vitalTable;
    @FXML private TableColumn<VitalSign, String> vPulseCol;
    @FXML private TableColumn<VitalSign, String> vTempCol;
    @FXML private TableColumn<VitalSign, String> vRespCol;
    @FXML private TableColumn<VitalSign, String> vSysCol;
    @FXML private TableColumn<VitalSign, String> vDiaCol;
    @FXML private TableColumn<VitalSign, String> vTimeCol;

    private final DateTimeFormatter viewFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DateTimeFormatter sqlTsFmt = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true).optionalEnd()
            .toFormatter();

    // ================= initialization =================
    @FXML
    public void initialize() {
        // spinners (UI defaults)
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
                c.getValue().getTime() == null ? "" : viewFmt.format(c.getValue().getTime())));
        locationCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocation()));

        // prescription table
        rxIdCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        rxMedCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMedicine()));
        rxDoseCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDosage()));
        rxApprovedCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isApproved() ? "Yes" : "No"));

        // vital table (columns map to your VitalSign model)
        vPulseCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf((int)c.getValue().getPulse())));
        vTempCol.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.1f", c.getValue().getTemperature())));
        vRespCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf((int)c.getValue().getRespiration())));
        vSysCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf((int)c.getValue().getSystolic())));
        vDiaCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf((int)c.getValue().getDiastolic())));
        vTimeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getRecordedAt() == null ? "" : viewFmt.format(c.getValue().getRecordedAt())));

        refreshTables();
    }

    private void refreshTables() {
        // appointments & prescriptions remain local (as per your existing prototype)
        apptTable.setItems(FXCollections.observableArrayList(db.appointmentsFor("P001")));
        rxTable.setItems(FXCollections.observableArrayList(
                db.allPrescriptions().stream().filter(r -> r.getPatientId().equals("P001")).toList()));

        // vitals now come from the server/db
        refreshVitalsFromServer();
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

        // still using DataStore demo for now
        db.addAppointment("P001", specialistField.getText().trim(), dt, loc);

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
        // still using DataStore demo for now
        db.addPrescription("P001", medField.getText().trim(), dose);

        medField.clear();
        doseField.clear();

        refreshTables();
    }

    // ================= vital signs (server/db) =================
    @FXML
    private void saveVitals(ActionEvent e) {
        Integer meId = Session.currentUserId();
        if (meId == null) {
            new Alert(Alert.AlertType.ERROR, "Not logged in. Please sign in again.").show();
            return;
        }

        try {
            double pulse = pulseSpin.getValue();
            double temp  = tempSpin.getValue();
            double resp  = respSpin.getValue();
            double sys   = sysSpin.getValue();
            double dia   = diaSpin.getValue();

            ThsClient c = new ThsClient("127.0.0.1", ServerMain.PORT);
            Response r = c.send("ADD_VITALS", Map.of(
                    "patient_id", meId,
                    "pulse", pulse,
                    "temperature", temp,
                    "respiration", resp,
                    "systolic", sys,
                    "diastolic", dia
            ));

            if (!r.ok) {
                new Alert(Alert.AlertType.ERROR, "Failed to save vitals: " + r.error).show();
                return;
            }

            int created = ((Number) r.data.get("alerts_created")).intValue();
            refreshVitalsFromServer();

            if (created > 0) {
                new Alert(Alert.AlertType.WARNING, created + " alert(s) triggered for your last reading.").show();
            } else {
                new Alert(Alert.AlertType.INFORMATION, "Vitals saved successfully.").show();
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).show();
            ex.printStackTrace();
        }
    }

    @FXML
    private void importVitalsCsv(ActionEvent e) {
        Integer meId = Session.currentUserId();
        if (meId == null) {
            new Alert(Alert.AlertType.ERROR, "Not logged in.").show();
            return;
        }

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File f = fc.showOpenDialog(apptTable.getScene().getWindow());
        if (f != null) {
            try {
                int sent = 0;
                ThsClient c = new ThsClient("127.0.0.1", ServerMain.PORT);
                for (var v : CsvVitals.read("P001", f)) {
                    // send each row to server; server will evaluate alerts
                    Response r = c.send("ADD_VITALS", Map.of(
                            "patient_id", meId,
                            "pulse", v.getPulse(),
                            "temperature", v.getTemperature(),
                            "respiration", v.getRespiration(),
                            "systolic", v.getSystolic(),
                            "diastolic", v.getDiastolic(),
                            "notes", "csv-import"
                    ));
                    if (r.ok) sent++;
                }
                refreshVitalsFromServer();
                new Alert(Alert.AlertType.INFORMATION, "Imported " + sent + " vital record(s).").show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "CSV import failed: " + ex.getMessage()).show();
            }
        }
    }

    private void refreshVitalsFromServer() {
        Integer meId = Session.currentUserId();
        if (meId == null) {
            vitalTable.setItems(FXCollections.observableArrayList());
            return;
        }
        try {
            ThsClient c = new ThsClient("127.0.0.1", ServerMain.PORT);
            Response r = c.send("LIST_VITALS_FOR_PATIENT", Map.of("patient_id", meId, "limit", 100));
            if (!r.ok) {
                // fallback to local (kept for demo completeness)
                vitalTable.setItems(FXCollections.observableArrayList(db.vitalsFor("P001")));
                return;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) r.data.get("vitals");
            var list = rows.stream()
                    .map(this::toVitalSign)
                    .toList();

            vitalTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception ex) {
            // fallback to local if server not reachable
            vitalTable.setItems(FXCollections.observableArrayList(db.vitalsFor("P001")));
        }
    }

    private VitalSign toVitalSign(Map<String, Object> m) {
        // Your model: new VitalSign(patientId, pulse, temp, resp, sys, dia, recordedAt)
        String pid = String.valueOf(Session.currentUserId()); // display only
        double pulse = asDouble(m.get("pulse"));
        double temp  = asDouble(m.get("temperature"));
        double resp  = asDouble(m.get("respiration"));
        double sys   = asDouble(m.get("systolic"));
        double dia   = asDouble(m.get("diastolic"));
        LocalDateTime at = parseSqlTimestamp(String.valueOf(m.get("taken_at")));
        return new VitalSign(pid, pulse, temp, resp, sys, dia, at);
    }

    private double asDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return 0.0; }
    }

    private LocalDateTime parseSqlTimestamp(String s) {
        try { return LocalDateTime.parse(s, sqlTsFmt); }
        catch (Exception ignore) { return null; }
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
