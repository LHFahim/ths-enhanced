package com.fahim.ths.model;

public class Prescription {
    private String id;
    private String patientId;
    private String medicine;
    private String dosage;
    private boolean approved;

    public Prescription(String id,String patientId,String medicine,String dosage, boolean approved){
        this.id=id;
        this.patientId=patientId;
        this.medicine=medicine;
        this.dosage=dosage;
        this.approved=approved;
    }
    public String getId(){
        return id;
    }
    public String getPatientId(){
        return patientId;
    }
    public String getMedicine(){
        return medicine;
    }
    public String getDosage(){
        return dosage;
    }
    public boolean isApproved(){
        return approved;
    }
    public void setApproved(boolean a){
        this.approved=a;
    }
}
