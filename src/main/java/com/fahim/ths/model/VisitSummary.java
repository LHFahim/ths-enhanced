package com.fahim.ths.model;

import java.time.LocalDateTime;

public class VisitSummary {
    private final String appointmentId;
    private final String patientId;
    private final String diagnosis;
    private final String treatment;
    private final LocalDateTime createdAt;

    public VisitSummary(String appointmentId, String patientId, String diagnosis,
                        String treatment, LocalDateTime createdAt) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.createdAt = createdAt;
    }

    public String getAppointmentId() {
        return appointmentId;
    }
    public String getPatientId() {
        return patientId;
    }
    public String getDiagnosis() {
        return diagnosis;
    }
    public String getTreatment() {
        return treatment;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
