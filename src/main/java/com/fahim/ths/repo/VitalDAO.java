package com.fahim.ths.repo;

import java.sql.*;
import java.util.*;

public class VitalDAO {

    public static void insert(int patientId, Timestamp takenAt,
                              Double pulse, Double temp, Double resp,
                              Double sys, Double dia, String notes) throws SQLException {
        try (Connection c = Database.getAppConnection();
             PreparedStatement ps = c.prepareStatement("""
                INSERT INTO vitals(patient_id, taken_at, pulse, temperature, respiration, systolic_bp, diastolic_bp, notes)
                VALUES (?, COALESCE(?, CURRENT_TIMESTAMP), ?, ?, ?, ?, ?, ?)
             """)) {
            ps.setInt(1, patientId);
            if (takenAt == null) ps.setNull(2, Types.TIMESTAMP); else ps.setTimestamp(2, takenAt);
            if (pulse == null) ps.setNull(3, Types.DOUBLE); else ps.setDouble(3, pulse);
            if (temp  == null) ps.setNull(4, Types.DOUBLE); else ps.setDouble(4, temp);
            if (resp  == null) ps.setNull(5, Types.DOUBLE); else ps.setDouble(5, resp);
            if (sys   == null) ps.setNull(6, Types.DOUBLE); else ps.setDouble(6, sys);
            if (dia   == null) ps.setNull(7, Types.DOUBLE); else ps.setDouble(7, dia);
            if (notes == null) ps.setNull(8, Types.VARCHAR); else ps.setString(8, notes);
            ps.executeUpdate();
        }
    }

    public static List<Map<String, Object>> listForPatient(int patientId, int limit) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection c = Database.getAppConnection();
             PreparedStatement ps = c.prepareStatement("""
                 SELECT id, taken_at, pulse, temperature, respiration, systolic_bp, diastolic_bp, notes
                 FROM vitals
                 WHERE patient_id = ?
                 ORDER BY taken_at DESC
                 LIMIT ?
             """)) {
            ps.setInt(1, patientId);
            ps.setInt(2, Math.max(limit, 1));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("taken_at", rs.getTimestamp("taken_at").toString());
                m.put("pulse",      rs.getObject("pulse"));
                m.put("temperature",rs.getObject("temperature"));
                m.put("respiration",rs.getObject("respiration"));
                m.put("systolic",   rs.getObject("systolic_bp"));
                m.put("diastolic",  rs.getObject("diastolic_bp"));
                m.put("notes",      rs.getString("notes"));
                list.add(m);
            }
        }
        return list;
    }
}
