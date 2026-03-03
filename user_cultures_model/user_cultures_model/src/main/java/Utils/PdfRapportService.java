package Utils;

import Services.Dto.LigneIrrigationDto;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Génère un PDF des irrigations passées après une date.
 */
public class PdfRapportService {

    private static final Font FONT_TITRE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font FONT_SOUS_TITRE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static File genererPdf(File destination,
                                  List<LigneIrrigationDto> irrigationsApresDate,
                                  LocalDate dateSeuil) {
        try {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, new FileOutputStream(destination));
            document.open();
            document.add(new Paragraph("AgriGo - Rapport irrigation", FONT_TITRE));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Irrigations passées après le " + dateSeuil.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), FONT_SOUS_TITRE));
            document.add(new Paragraph(" "));
            if (irrigationsApresDate != null && !irrigationsApresDate.isEmpty()) {
                ajouterTableIrrigations(document, irrigationsApresDate);
            } else {
                document.add(new Paragraph("Aucune irrigation enregistrée après cette date.", FONT_NORMAL));
            }
            document.close();
            return destination;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void ajouterTableIrrigations(Document document, List<LigneIrrigationDto> list) throws DocumentException {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(4f);
        table.setSpacingAfter(4f);
        float[] widths = {1.4f, 1.8f, 1.5f, 0.7f, 0.8f, 0.7f, 0.6f};
        table.setWidths(widths);
        table.addCell(cellHeader("Date"));
        table.addCell(cellHeader("Système"));
        table.addCell(cellHeader("Parcelle"));
        table.addCell(cellHeader("Durée"));
        table.addCell(cellHeader("Volume"));
        table.addCell(cellHeader("Humid.%"));
        table.addCell(cellHeader("Type"));
        for (LigneIrrigationDto d : list) {
            table.addCell(cell(d.getDateIrrigation() != null ? d.getDateIrrigation().format(DATE_FORMAT) : ""));
            table.addCell(cell(d.getNomSysteme() != null ? d.getNomSysteme() : ""));
            table.addCell(cell(d.getNomParcelle() != null ? d.getNomParcelle() : ""));
            table.addCell(cell(String.valueOf(d.getDureeMinutes())));
            table.addCell(cell(d.getVolumeEau() != null ? d.getVolumeEau().toPlainString() : ""));
            table.addCell(cell(d.getHumiditeAvant() != null ? d.getHumiditeAvant().toPlainString() : ""));
            table.addCell(cell(d.getTypeDeclenchement() != null ? d.getTypeDeclenchement() : ""));
        }
        document.add(table);
    }

    private static PdfPCell cellHeader(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
        c.setGrayFill(0.9f);
        c.setPadding(4f);
        return c;
    }

    private static PdfPCell cell(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "", FONT_NORMAL));
        c.setPadding(3f);
        return c;
    }
}
