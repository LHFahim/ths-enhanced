package com.fahim.ths.model;

import java.time.LocalDateTime;

public class ExternalBooking {
    private String id;
    private String patientId;
    private String facility;
    private LocalDateTime time;
    private String reason;

    public ExternalBooking(String id,String patientId,String facility,LocalDateTime time,String reason){
        this.id=id;
        this.patientId=patientId;
        this.facility=facility;
        this.time=time;
        this.reason=reason;
    }
    public String getId(){
        return id;
    }
    public String getPatientId(){
        return patientId;
    }
    public String getFacility(){
        return facility;
    }
    public LocalDateTime getTime(){
        return time;
    }
    public String getReason(){
        return reason;
    }
}
