package com.fahim.ths;

import com.fahim.ths.model.Appointment;
import com.fahim.ths.repo.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class StaffChangeTest {

    private DataStore db;

    @BeforeEach
    void setup() {
        db = DataStore.get();
        db._testReset();
    }

    @Test
    void updateAppointmentTimeAndLocation() {
        var oldTime = LocalDateTime.of(2025, 10, 10, 10, 0);
        var a = db.addAppointment("P001", "Dermatology", oldTime, "Clinic A");
        var newTime = LocalDateTime.of(2025, 10, 12, 15, 30);
        a.setTime(newTime);
        a.setLocation("Clinic B");

        var found = db.findAppointmentById(a.getId());
        assertEquals(newTime, found.getTime());
        assertEquals("Clinic B", found.getLocation());
    }
}
