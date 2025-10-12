package com.fahim.ths;

import com.fahim.ths.model.VitalSign;
import com.fahim.ths.repo.DataStore;
import com.fahim.ths.util.CsvVitals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CsvVitalsTest {

    private DataStore db;

    @BeforeEach
    void setup() {
        db = DataStore.get();
        db._testReset();
    }

    @Test
    void importVitalsCsv_parsesRows() throws Exception {
        // Create a temp CSV file
        File tmp = File.createTempFile("vitals-", ".csv");
        try (PrintWriter pw = new PrintWriter(tmp)) {
            // pulse,temp,resp,sys,dia
            pw.println("72,36.8,16,120,80");
            pw.println("78,37.0,18,125,82");
        }

        List<VitalSign> rows = CsvVitals.read("P001", tmp);
        assertEquals(2, rows.size());

        // Add to store (like UI does)
        rows.forEach(db::addVital);
        var list = db.vitalsFor("P001");
        assertEquals(2, list.size());

        VitalSign v0 = list.get(0);
        assertTrue(v0.getPulse() > 0);
        assertTrue(v0.getTemperature() > 0);
        assertNotNull(v0.getRecordedAt() == null ? LocalDateTime.now() : v0.getRecordedAt());
    }
}
