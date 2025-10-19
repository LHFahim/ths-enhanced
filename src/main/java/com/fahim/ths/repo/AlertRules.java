package com.fahim.ths.repo;

import java.sql.Connection;
import java.sql.SQLException;

public class AlertRules {

    // evaluate vitals; create alerts (server-side) if abnormal
    public static int evaluateAndCreateAlerts(Connection c, int patientId,
                                              Double pulse, Double temp, Double resp,
                                              Double sys, Double dia) throws SQLException {
        int count = 0;


        if (sys != null && dia != null) {
            if (sys >= 180 || dia >= 120) {
                AlertDAO.insert(patientId, "BP", "CRIT",
                        "Hypertensive crisis suspected (" + sys.intValue() + "/" + dia.intValue() + " mmHg)");
                count++;
            } else if (sys >= 140 || dia >= 90) {
                AlertDAO.insert(patientId, "BP", "WARN",
                        "Blood pressure elevated (" + sys.intValue() + "/" + dia.intValue() + " mmHg)");
                count++;
            }
        }


        if (temp != null) {
            if (temp >= 39.5) {
                AlertDAO.insert(patientId, "TEMP", "CRIT",
                        String.format("High fever (%.1f°C)", temp));
                count++;
            } else if (temp >= 38.0) {
                AlertDAO.insert(patientId, "TEMP", "WARN",
                        String.format("Temperature elevated (%.1f°C)", temp));
                count++;
            }
        }


        if (resp != null && resp >= 24) {
            AlertDAO.insert(patientId, "RESP", "WARN",
                    String.format("Respiration fast (%.0f breaths/min)", resp));
            count++;
        }


        if (pulse != null) {
            if (pulse >= 140) {
                AlertDAO.insert(patientId, "PULSE", "CRIT",
                        String.format("Pulse very high (%.0f bpm)", pulse));
                count++;
            } else if (pulse >= 120 || pulse <= 50) {
                AlertDAO.insert(patientId, "PULSE", "WARN",
                        String.format("Pulse abnormal (%.0f bpm)", pulse));
                count++;
            }
        }

        return count;
    }
}
