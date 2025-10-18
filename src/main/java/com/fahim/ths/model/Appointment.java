


package com.fahim.ths.model;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

public class Appointment {

    private String id;

    @SerializedName("doctor_id")
    private String doctorId;

    @SerializedName("patient_id")
    private String patientId;

    @SerializedName("patient_name")
    private String patientName;

    @SerializedName("start_time")
    private String startTimeRaw;

    @SerializedName("end_time")
    private String endTimeRaw;

    private String status;

    @SerializedName("notes")
    private String location; // clinic / hospital / online

    private String specialist; // for display

    // 👇 IMPORTANT: mark as transient so Gson doesn't reflect into LocalDateTime
    private transient LocalDateTime time;

    public Appointment() {}

    public Appointment(String id, String patientId, String specialist, LocalDateTime time, String location){
        this.id = id;
        this.patientId = patientId;
        this.specialist = specialist;
        this.time = time;
        this.location = location;
    }

    public String getId(){ return id; }
    public String getDoctorId(){ return doctorId; }
    public String getPatientId(){ return patientId; }
    public String getPatientName(){ return patientName; }
    public String getStatus(){ return status; }
    public String getLocation(){ return location; }
    public String getSpecialist(){ return specialist == null ? "Consultation" : specialist; }
    public void setSpecialist(String s){ this.specialist = s; }
    public void setTime(LocalDateTime t){ this.time = t; }
    public void setLocation(String l){ this.location = l; }

    /** Parse start_time on demand */
    public LocalDateTime getTime(){
        if (time != null) return time;
        if (startTimeRaw == null) return null;
        try { return LocalDateTime.parse(startTimeRaw.replace(' ', 'T')); }
        catch (Exception e) { return null; }
    }
}
