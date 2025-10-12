package com.fahim.ths.model;

import java.time.LocalDateTime;

public class VitalSign {
    private String patientId;
    private double pulse;
    private double temperature;
    private double respiration;
    private double systolic;
    private double diastolic;
    private LocalDateTime recordedAt;

    public VitalSign(String patientId, double pulse, double temperature, double respiration, double systolic, double diastolic, LocalDateTime recordedAt){
        this.patientId=patientId;
        this.pulse=pulse;
        this.temperature=temperature;
        this.respiration=respiration;
        this.systolic=systolic;
        this.diastolic=diastolic;
        this.recordedAt=recordedAt;
    }
    public String getPatientId(){
        return patientId;
    }
    public double getPulse(){
        return pulse;
    }
    public double getTemperature(){
        return temperature;
    }
    public double getRespiration(){
        return respiration;
    }
    public double getSystolic(){
        return systolic;
    }
    public double getDiastolic(){
        return diastolic;
    }
    public LocalDateTime getRecordedAt(){
        return recordedAt;
    }
}
