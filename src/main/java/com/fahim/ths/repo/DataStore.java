package com.fahim.ths.repo;

import com.fahim.ths.model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class DataStore {
    private static final DataStore INSTANCE = new DataStore();
    public static DataStore get() { return INSTANCE; }

    private final Map<String, Patient> patients       = new HashMap<>();
    private final Map<String, Appointment> appointments = new LinkedHashMap<>();
    private final Map<String, Prescription> prescriptions = new LinkedHashMap<>();
    private final Map<String, Diagnosis>   diagnoses   = new LinkedHashMap<>();
    private final Map<String, ExternalBooking> extBookings = new LinkedHashMap<>();
    private final List<VitalSign> vitals = new ArrayList<>();
    private final List<VisitSummary> summaries = new ArrayList<>();

    private DataStore() {
        // here i am seeding demo patient so the portals have something to work with
        patients.put("P001", new Patient("P001", "Alice Patient", "alice@example.com"));
    }

    // ---------------- patients ----------------
    public Patient getPatient(String id) { return patients.get(id); }
    public Collection<Patient> allPatients() { return patients.values(); }

    // ---------------- appointments ----------------
    public Appointment addAppointment(String patientId, String specialist, LocalDateTime time, String loc) {
        String id = "A" + (appointments.size() + 1);
        Appointment a = new Appointment(id, patientId, specialist, time, loc);
        appointments.put(id, a);
        return a;
    }

    /** appointments for a specific patient - for patient portal */
    public List<Appointment> appointmentsFor(String patientId) {
        return appointments.values()
                .stream()
                .filter(a -> a.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    /** all appointments  doctor/staff portals */
    public Collection<Appointment> allAppointments() {
        return appointments.values();
    }


    public List<Appointment> getAppointments() {
        return new ArrayList<>(appointments.values());
    }


    public Appointment findAppt(String id) {
        return appointments.get(id);
    }


    public Appointment findAppointmentById(String id) {
        return findAppt(id);
    }


    public Prescription addPrescription(String patientId, String med, String dose) {
        String id = "RX" + (prescriptions.size() + 1);
        Prescription p = new Prescription(id, patientId, med, dose, false);
        prescriptions.put(id, p);
        return p;
    }

    public Collection<Prescription> allPrescriptions() {
        return prescriptions.values();
    }


    public void addVital(VitalSign v) {
        vitals.add(v);
    }

    public List<VitalSign> vitalsFor(String patientId) {
        return vitals.stream()
                .filter(v -> v.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }


    public void saveDiagnosis(String apptId, String notes, String plan) {
        diagnoses.put(apptId, new Diagnosis(apptId, notes, plan));
    }
    public Diagnosis getDiagnosis(String apptId) {
        return diagnoses.get(apptId);
    }

    public void addVisitSummary(VisitSummary vs) {
        summaries.add(vs);
    }

    public VisitSummary getVisitSummary(String appointmentId) {
        return summaries.stream()
                .filter(v -> v.getAppointmentId().equals(appointmentId))
                .findFirst()
                .orElse(null);
    }


    public ExternalBooking addExternal(String patientId, String facility, LocalDateTime time, String reason) {
        String id = "E" + (extBookings.size() + 1);
        ExternalBooking e = new ExternalBooking(id, patientId, facility, time, reason);
        extBookings.put(id, e);
        return e;
    }
    public Collection<ExternalBooking> allExternal() { return extBookings.values(); }


    public boolean approvePrescription(String id) {
        Prescription p = prescriptions.get(id);
        if (p == null) return false;

        p.setApproved(true);
        return true;
    }

    public boolean removePrescription(String id) {
        return prescriptions.remove(id) != null;
    }



    public void _testReset() {
        patients.clear();
        appointments.clear();
        prescriptions.clear();
        diagnoses.clear();
        extBookings.clear();
        vitals.clear();
        summaries.clear();
        // seed demo again
        patients.put("P001", new Patient("P001","Alice Patient","alice@gmail.com"));
    }


}
