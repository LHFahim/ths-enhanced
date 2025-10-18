package com.fahim.ths.repo;

import java.sql.*;
import java.util.*;

public class AlertDAO {

    public static void insert(int patientId, String type, String severity, String message) throws SQLException {
        try (Connection c = Database.getAppConnection();
             PreparedStatement ps = c.prepareStatement("""
                 INSERT INTO alerts(patient_id, type, severity, message)
                 VALUES (?, ?, ?, ?)
             """)) {
            ps.setInt(1, patientId);
            ps.setString(2, type);
            ps.setString(3, severity);
            ps.setString(4, message);
            ps.executeUpdate();
        }
    }

    public static List<Map<String,Object>> listForPatient(int patientId, int limit) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        try (Connection c = Database.getAppConnection();
             PreparedStatement ps = c.prepareStatement("""
                 SELECT id, type, severity, message, created_at, acknowledged_at
                 FROM alerts
                 WHERE patient_id = ?
                 ORDER BY created_at DESC
                 LIMIT ?
             """)) {
            ps.setInt(1, patientId);
            ps.setInt(2, Math.max(limit, 1));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> m = new HashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("type", rs.getString("type"));
                m.put("severity", rs.getString("severity"));
                m.put("message", rs.getString("message"));
                m.put("created_at", rs.getTimestamp("created_at").toString());
                m.put("acknowledged_at", rs.getTimestamp("acknowledged_at"));
                list.add(m);
            }
        }
        return list;
    }
}
