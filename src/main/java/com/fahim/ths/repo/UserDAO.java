package com.fahim.ths.repo;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {


    public static Map<String, Object> validateLogin(String email, String password) throws SQLException {
        try (Connection c = Database.getAppConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, role, name, email FROM users WHERE email = ? AND pass_hash = ?")) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null; // no match

                Map<String, Object> u = new HashMap<>();
                u.put("id", rs.getInt("id"));
                u.put("role", rs.getString("role"));
                u.put("name", rs.getString("name"));
                u.put("email", rs.getString("email"));
                return u;
            }
        }
    }


    public static Map<String,Object> findByEmail(String email) throws SQLException {
        try (Connection c = Database.getAppConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, role, name, email FROM users WHERE email = ?")) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Map<String,Object> u = new HashMap<>();
                u.put("id", rs.getInt("id"));
                u.put("role", rs.getString("role"));
                u.put("name", rs.getString("name"));
                u.put("email", rs.getString("email"));
                return u;
            }
        }
    }
}
