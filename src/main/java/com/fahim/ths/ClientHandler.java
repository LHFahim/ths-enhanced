package com.fahim.ths;

import com.fahim.ths.repo.Database;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket){ this.socket = socket; }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String line;
            while ((line = in.readLine()) != null) {
                Response resp;
                try {
                    Request req = JsonUtil.fromJson(line, Request.class);
                    resp = route(req);
                } catch (Exception ex) {
                    resp = Response.err("invalid request: " + ex.getMessage());
                }
                out.write(JsonUtil.toJson(resp));
                out.write("\n");
                out.flush();
            }
        } catch (IOException e) {
            // client disconnected or network error
        } finally {
            try { socket.close(); } catch (IOException ignore) {}
        }
    }

    private Response route(Request req) {
        if (req == null || req.op == null) return Response.err("missing op");

        switch (req.op) {
            case "PING": {
                var data = new HashMap<String,Object>();
                data.put("reply", "PONG");
                return Response.ok(data);
            }
            case "DB_HEALTH": {
                try (Connection c = Database.getAppConnection();
                     Statement st = c.createStatement()) {
                    ResultSet rs = st.executeQuery("SELECT COUNT(*) AS n FROM users");
                    int n = 0; if (rs.next()) n = rs.getInt("n");
                    var data = new HashMap<String,Object>();
                    data.put("users_count", n);
                    return Response.ok(data);
                } catch (Exception ex) {
                    return Response.err("db error: " + ex.getMessage());
                }
            }
            case "LOGIN": {
                try {
                    String email = (String) req.payload.get("email");
                    String password = (String) req.payload.get("password");
                    if (email == null || password == null) return Response.err("email and password required");

                    var user = com.fahim.ths.repo.UserDAO.validateLogin(email, password);
                    if (user == null) return Response.err("invalid email or password");
                    return Response.ok(user);
                } catch (Exception ex) {
                    return Response.err("login failed: " + ex.getMessage());
                }
            }

            case "SEND_MESSAGE": {
                try {
                    int sender = ((Double) req.payload.get("sender_id")).intValue();
                    int receiver = ((Double) req.payload.get("receiver_id")).intValue();
                    String body = (String) req.payload.get("body");
                    String attachment = (String) req.payload.getOrDefault("attachment", null);

                    com.fahim.ths.repo.MessageDAO.insert(sender, receiver, body, attachment);
                    return Response.ok(Map.of("status", "sent"));
                } catch (Exception ex) {
                    return Response.err("failed to send message: " + ex.getMessage());
                }
            }

            case "LIST_MESSAGES": {
                try {
                    int userA = ((Double) req.payload.get("userA")).intValue();
                    int userB = ((Double) req.payload.get("userB")).intValue();
                    var list = com.fahim.ths.repo.MessageDAO.listThread(userA, userB);
                    return Response.ok(Map.of("messages", list));
                } catch (Exception ex) {
                    return Response.err("failed to list messages: " + ex.getMessage());
                }
            }

            default:
                return Response.err("unknown op: " + req.op);
        }
    }
}
