package com.fahim.ths.repo;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class AppointmentDAO {

    // insert new appointment
    public static void insert(int patientId, int doctorId, LocalDateTime start, LocalDateTime end,
                              String location, String notes) throws SQLException {
        try (Connection c = Database.getAppConnection();
             PreparedStatement ps = c.prepareStatement("""
                INSERT INTO appointments(patient_id, doctor_id, start_time, end_time, status, notes)
                VALUES (?,?,?,?, 'BOOKED', ?)
             """)) {
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setTimestamp(3, Timestamp.valueOf(start));
            ps.setTimestamp(4, Timestamp.valueOf(end));

            // combine both location + notes safely
            String combinedNotes = (location == null || location.isBlank())
                    ? notes
                    : (notes == null || notes.isBlank() ? location : (location + " — " + notes));

            ps.setString(5, combinedNotes);
            ps.executeUpdate();
        }
    }

    // list appointments for patient
    public static List<Map<String,Object>> listForPatient(int patientId) throws SQLException {
        String sql = """
          SELECT a.id, a.start_time, a.end_time, a.status, a.notes,
                 u.name AS doctor_name, a.doctor_id, a.patient_id, a.created_at
          FROM appointments a
          JOIN users u ON u.id = a.doctor_id
          WHERE a.patient_id = ?
          ORDER BY a.start_time DESC
        """;
        return listBy(sql, patientId);
    }

    // list appointments for doctor
    public static List<Map<String,Object>> listForDoctor(int doctorId) throws SQLException {
        String sql = """
          SELECT a.id, a.start_time, a.end_time, a.status, a.notes,
                 u.name AS patient_name, a.patient_id, a.doctor_id, a.created_at
          FROM appointments a
          JOIN users u ON u.id = a.patient_id
          WHERE a.doctor_id = ?
          ORDER BY a.start_time DESC
        """;
        return listBy(sql, doctorId);
    }

    // shared helper
    private static List<Map<String,Object>> listBy(String sql, int id) throws SQLException {
        List<Map<String,Object>> out = new ArrayList<>();
        try (Connection c = Database.getAppConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> m = new HashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("start_time", rs.getTimestamp("start_time").toString());
                m.put("end_time", rs.getTimestamp("end_time").toString());
                m.put("status", rs.getString("status"));
                m.put("notes", rs.getString("notes"));
                m.put("patient_id", rs.getObject("patient_id"));
                m.put("doctor_id",  rs.getObject("doctor_id"));
                if (hasColumn(rs, "patient_name")) m.put("patient_name", rs.getString("patient_name"));
                if (hasColumn(rs, "doctor_name"))  m.put("doctor_name", rs.getString("doctor_name"));
                m.put("created_at", rs.getTimestamp("created_at").toString());
                out.add(m);
            }
        }
        return out;
    }

    private static boolean hasColumn(ResultSet rs, String name) {
        try { return rs.findColumn(name) > 0; } catch (SQLException e) { return false; }
    }


    public static void update(int id, LocalDateTime start, LocalDateTime end,
                              String location, String status, String notes) throws SQLException {
        try (Connection c = Database.getAppConnection();
             PreparedStatement ps = c.prepareStatement("""
             UPDATE appointments
             SET start_time = ?, end_time = ?, notes = ?, status = ?
             WHERE id = ?
         """)) {
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));

            // safely merge updated location/notes
            String combinedNotes = (location == null || location.isBlank())
                    ? notes
                    : (notes == null || notes.isBlank() ? location : (location + " — " + notes));

            ps.setString(3, combinedNotes);
            ps.setString(4, status);
            ps.setInt(5, id);
            ps.executeUpdate();
        }
    }
}
