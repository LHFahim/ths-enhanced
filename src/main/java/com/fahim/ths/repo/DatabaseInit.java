package com.fahim.ths.repo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/* creates database + tables programmatically; run at app start */
public class DatabaseInit {

    public static void init() throws SQLException {
        // create DB if not exists
        Database.ensureDatabaseExists();

        // create tables in that DB
        try (Connection conn = Database.getAppConnection()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {

                st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users(
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  role ENUM('PATIENT','DOCTOR','ADMIN') NOT NULL,
                  name VARCHAR(100) NOT NULL,
                  email VARCHAR(120) NOT NULL UNIQUE,
                  pass_hash VARCHAR(255) NOT NULL,
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB;
                """);

                st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS patients(
                  id INT PRIMARY KEY,
                  medicare_no VARCHAR(40),
                  dob DATE,
                  FOREIGN KEY (id) REFERENCES users(id)
                ) ENGINE=InnoDB;
                """);

                st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS doctors(
                  id INT PRIMARY KEY,
                  provider_no VARCHAR(40),
                  specialty VARCHAR(80),
                  FOREIGN KEY (id) REFERENCES users(id)
                ) ENGINE=InnoDB;
                """);

                st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS appointments(
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  patient_id INT NOT NULL,
                  doctor_id INT NOT NULL,
                  start_time DATETIME NOT NULL,
                  end_time DATETIME NOT NULL,
                  status VARCHAR(20) DEFAULT 'BOOKED',
                  notes TEXT,
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  FOREIGN KEY(patient_id) REFERENCES users(id),
                  FOREIGN KEY(doctor_id) REFERENCES users(id)
                ) ENGINE=InnoDB;
                """);

                st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS vitals(
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  patient_id INT NOT NULL,
                  taken_at DATETIME NOT NULL,
                  pulse DOUBLE,
                  temperature DOUBLE,
                  respiration DOUBLE,
                  systolic_bp DOUBLE,
                  diastolic_bp DOUBLE,
                  notes TEXT,
                  FOREIGN KEY(patient_id) REFERENCES users(id)
                ) ENGINE=InnoDB;
                """);

                /* feature 1: messaging */
                st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS messages(
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  sender_user_id INT NOT NULL,
                  receiver_user_id INT NOT NULL,
                  body TEXT NOT NULL,
                  attachment_path VARCHAR(255),
                  sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  read_at TIMESTAMP NULL,
                  FOREIGN KEY(sender_user_id) REFERENCES users(id),
                  FOREIGN KEY(receiver_user_id) REFERENCES users(id)
                ) ENGINE=InnoDB;
                """);

                /* feature 2: vitals alerts */
                st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS alerts(
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  patient_id INT NOT NULL,
                  type ENUM('BP','TEMP','RESP','PULSE') NOT NULL,
                  severity ENUM('INFO','WARN','CRIT') NOT NULL,
                  message TEXT NOT NULL,
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  acknowledged_at TIMESTAMP NULL,
                  FOREIGN KEY(patient_id) REFERENCES users(id)
                ) ENGINE=InnoDB;
                """);

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }


    public static void seed() throws SQLException {
        try (Connection conn = Database.getAppConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate("""
            INSERT INTO users(role,name,email,pass_hash)
            SELECT 'DOCTOR','dr smith','drsmith@example.com','$2a$10$placeholderhash'
            WHERE NOT EXISTS(SELECT 1 FROM users WHERE email='drsmith@example.com');
            """);
            st.executeUpdate("""
            INSERT INTO users(role,name,email,pass_hash)
            SELECT 'PATIENT','alice patient','alice@example.com','$2a$10$placeholderhash'
            WHERE NOT EXISTS(SELECT 1 FROM users WHERE email='alice@example.com');
            """);
            st.executeUpdate("""
            INSERT IGNORE INTO doctors(id,provider_no,specialty)
            SELECT id,'PROV001','general' FROM users WHERE email='drsmith@example.com';
            """);
            st.executeUpdate("""
            INSERT IGNORE INTO patients(id,medicare_no,dob)
            SELECT id,'MED123','1995-01-10' FROM users WHERE email='alice@example.com';
            """);
        }
    }
}
