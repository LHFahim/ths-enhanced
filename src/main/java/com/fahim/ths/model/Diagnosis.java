package com.fahim.ths.model;

public class Diagnosis {
    private String appointmentId;
    private String notes;
    private String treatmentPlan;

    public Diagnosis(String appointmentId, String notes, String treatmentPlan){
        this.appointmentId=appointmentId;
        this.notes=notes;
        this.treatmentPlan=treatmentPlan;
    }
    public String getAppointmentId(){
        return appointmentId;
    }
    public String getNotes(){
        return notes;
    }
    public String getTreatmentPlan(){
        return treatmentPlan;
    }
}
