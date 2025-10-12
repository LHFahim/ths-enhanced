package com.fahim.ths.util;

import com.fahim.ths.model.VisitSummary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;

public class PdfExporter {

    public static void exportVisitSummary(File file, VisitSummary vs) throws Exception {
        if (vs == null) throw new IllegalArgumentException("No visit summary found");

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs.newLineAtOffset(50, 750);
                cs.showText("Visit Summary");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(50, 720);
                cs.showText("Appointment ID: " + vs.getAppointmentId());
                cs.newLineAtOffset(0, -18);
                cs.showText("Patient ID: " + vs.getPatientId());
                cs.newLineAtOffset(0, -18);
                cs.showText("Diagnosis: " + vs.getDiagnosis());
                cs.newLineAtOffset(0, -18);
                cs.showText("Treatment: " + vs.getTreatment());
                cs.newLineAtOffset(0, -18);
                cs.showText("Created At: " + vs.getCreatedAt());
                cs.endText();
            }

            doc.save(file);
        }
    }
}
