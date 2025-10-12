package com.fahim.ths.repo;

import java.sql.*;
import java.util.Properties;
import java.io.InputStream;


public class Database {

    private static String hostport, dbName, user, password;

    private static void loadProps() throws SQLException {
        if (hostport != null) return;
        try (InputStream is = Database.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) throw new SQLException("db.properties not found in classpath (src/main/resources)");
            Properties p = new Properties();
            p.load(is);
            hostport = p.getProperty("db.hostport", "localhost:3306").trim();
            dbName   = p.getProperty("db.name", "ths").trim();
            user     = p.getProperty("db.user");
            password = p.getProperty("db.password");
            if (user == null || password == null) {
                throw new SQLException("missing db.user / db.password in db.properties");
            }
        } catch (Exception e) {
            throw new SQLException("failed to load db.properties: " + e.getMessage(), e);
        }
    }

    private static String urlWithoutSchema() throws SQLException {
        loadProps();
        return "jdbc:mysql://" + hostport + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private static String urlWithSchema() throws SQLException {
        loadProps();
        return "jdbc:mysql://" + hostport + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }


    public static Connection getServerConnection() throws SQLException {
        return DriverManager.getConnection(urlWithoutSchema(), user, password);
    }


    public static Connection getAppConnection() throws SQLException {
        return DriverManager.getConnection(urlWithSchema(), user, password);
    }


    public static void ensureDatabaseExists() throws SQLException {
        loadProps();
        try (Connection conn = getServerConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
        }
    }
}
