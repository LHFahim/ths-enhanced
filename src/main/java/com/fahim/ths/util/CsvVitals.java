package com.fahim.ths.util;

import com.fahim.ths.model.VitalSign;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CsvVitals {
    public static List<VitalSign> read(String patientId, File file) throws IOException {
        List<VitalSign> list = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;

            while((line = br.readLine())!=null){
                if(line.trim().isEmpty() || line.startsWith("#")) continue;

                String[] t = line.split(",");

                if(t.length<5) continue;

                double pulse = Double.parseDouble(t[0]);
                double temp = Double.parseDouble(t[1]);
                double resp = Double.parseDouble(t[2]);
                double sys = Double.parseDouble(t[3]);
                double dia = Double.parseDouble(t[4]);

                list.add(new VitalSign(patientId,pulse,temp,resp,sys,dia, LocalDateTime.now()));
            }
        }
        return list;
    }
}
