package com.fahim.ths.model;

import java.time.LocalDateTime;

public class Appointment {
    private String id;
    private String patientId;
    private String specialist;
    private LocalDateTime time;
    private String location; // it can be clinic / hospital / online

    public Appointment(String id, String patientId, String specialist, LocalDateTime time, String location){
        this.id=id;
        this.patientId=patientId;
        this.specialist=specialist;
        this.time=time;
        this.location=location;
    }

    public String getId(){
        return id;
    }
    public String getPatientId(){
        return patientId;
    }
    public String getSpecialist(){
        return specialist;
    }
    public LocalDateTime getTime(){
        return time;
    }
    public String getLocation(){
        return location;
    }
    public void setTime(LocalDateTime t){
        this.time=t;
    }
    public void setLocation(String l){
        this.location=l;
    }
}
