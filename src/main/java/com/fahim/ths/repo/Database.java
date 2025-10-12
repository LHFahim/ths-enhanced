package com.fahim.ths.repo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

/* simple jdbc connector that reads db.properties from classpath */
public class Database {

    private static String url, user, password;

    private static void loadProps() throws SQLException {
        if (url != null) return;
        try (InputStream is = Database.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) throw new SQLException("db.properties not found in classpath (src/main/resources)");
            Properties p = new Properties();
            p.load(is);
            url = p.getProperty("db.url");
            user = p.getProperty("db.user");
            password = p.getProperty("db.password");
            if (url == null || user == null || password == null)
                throw new SQLException("missing db.url/db.user/db.password in db.properties");
        } catch (Exception e) {
            throw new SQLException("failed to load db.properties: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        loadProps();
        return DriverManager.getConnection(url, user, password);
    }
}
