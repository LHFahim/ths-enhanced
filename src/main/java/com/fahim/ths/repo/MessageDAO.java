package com.fahim.ths.repo;

import java.sql.*;
import java.util.*;

public class MessageDAO {

    // insert a new message
    public static void insert(int senderId, int receiverId, String body, String attachmentPath) throws SQLException {
        try (Connection c = Database.getAppConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO messages(sender_user_id,receiver_user_id,body,attachment_path) VALUES(?,?,?,?)")) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, body);
            ps.setString(4, attachmentPath);
            ps.executeUpdate();
        }
    }

    // list all messages between two users
    public static List<Map<String,Object>> listThread(int userA, int userB) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        try (Connection c = Database.getAppConnection();
             PreparedStatement ps = c.prepareStatement("""
                 SELECT id, sender_user_id, receiver_user_id, body, sent_at, read_at
                 FROM messages
                 WHERE (sender_user_id=? AND receiver_user_id=?)
                    OR (sender_user_id=? AND receiver_user_id=?)
                 ORDER BY sent_at
             """)) {
            ps.setInt(1, userA);
            ps.setInt(2, userB);
            ps.setInt(3, userB);
            ps.setInt(4, userA);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> m = new HashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("sender", rs.getInt("sender_user_id"));
                m.put("receiver", rs.getInt("receiver_user_id"));
                m.put("body", rs.getString("body"));
                m.put("sent_at", rs.getTimestamp("sent_at").toString());
                m.put("read_at", rs.getTimestamp("read_at"));
                list.add(m);
            }
        }
        return list;
    }
}
