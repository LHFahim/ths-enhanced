package com.fahim.ths;

import com.fahim.ths.model.Appointment;
import com.fahim.ths.model.Prescription;
import com.fahim.ths.repo.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class DataStoreTest {

    private DataStore db;

    @BeforeEach
    void setup() {
        db = DataStore.get();
        db._testReset(); // isolate tests
    }

    @Test
    void addAndFindAppointment() {
        LocalDateTime dt = LocalDateTime.of(2025, 10, 10, 10, 0);
        Appointment a = db.addAppointment("P001", "Cardiology", dt, "Online");
        assertNotNull(a.getId());
        Appointment found = db.findAppointmentById(a.getId());
        assertNotNull(found);
        assertEquals("P001", found.getPatientId());
        assertEquals(dt, found.getTime());
        assertEquals("Online", found.getLocation());
    }

    @Test
    void addAndApprovePrescription() {
        Prescription rx = db.addPrescription("P001", "Amoxicillin", "500mg bid");
        assertNotNull(rx.getId());
        assertFalse(rx.isApproved(), "New RX should start unapproved");
        boolean ok = db.approvePrescription(rx.getId());
        assertTrue(ok);
        Prescription again = db.allPrescriptions().stream()
                .filter(p -> p.getId().equals(rx.getId()))
                .findFirst().orElseThrow();
        assertTrue(again.isApproved(), "RX should be approved now");
    }
}
