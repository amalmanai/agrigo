package Utils;

import Entites.Tache;
import Entites.User;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Fonctionnalité avancée 1 : Export PDF des listes (utilisateurs et tâches).
 */
public class ExportPdfService {

    private static final Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font stampFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

    public static String exportUsersToPdf(Set<User> users, String filePath) throws DocumentException, IOException {
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, new FileOutputStream(filePath));
        doc.open();

        // Logo AgriGo en haut
        Image logo = loadLogo();
        if (logo != null) {
            logo.setAlignment(Element.ALIGN_RIGHT);
            doc.add(logo);
        }

        Paragraph title = new Paragraph("Fiche(s) utilisateur - AgriGo", titleFont);
        title.setAlignment(Element.ALIGN_LEFT);
        doc.add(title);
        doc.add(new Paragraph("Généré le " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), cellFont));
        doc.add(Chunk.NEWLINE);

        if (users != null) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int index = 0;
            for (User u : users) {
                index++;
                if (index > 1) {
                    doc.add(Chunk.NEWLINE);
                }

                // Détails verticaux (label / valeur)
                PdfPTable details = new PdfPTable(2);
                details.setWidthPercentage(100);
                details.setWidths(new float[]{1.2f, 3f});
                details.addCell(detailCell("Nom", true));
                details.addCell(detailCell(safe(u.getNom_user()), false));
                details.addCell(detailCell("Prénom", true));
                details.addCell(detailCell(safe(u.getPrenom_user()), false));
                details.addCell(detailCell("Email", true));
                details.addCell(detailCell(safe(u.getEmail_user()), false));
                details.addCell(detailCell("Rôle", true));
                details.addCell(detailCell(safe(u.getRole_user()), false));
                details.addCell(detailCell("Téléphone", true));
                details.addCell(detailCell(String.valueOf(u.getNum_user()), false));
                details.addCell(detailCell("Adresse", true));
                details.addCell(detailCell(safe(u.getAdresse_user()), false));

                // QR code avec les détails
                String qrContent = "Utilisateur AgriGo\n"
                        + "Nom: " + safe(u.getNom_user()) + "\n"
                        + "Prénom: " + safe(u.getPrenom_user()) + "\n"
                        + "Email: " + safe(u.getEmail_user()) + "\n"
                        + "Rôle: " + safe(u.getRole_user()) + "\n"
                        + "Téléphone: " + u.getNum_user() + "\n"
                        + "Adresse: " + safe(u.getAdresse_user()) + "\n"
                        + "Généré le: " + LocalDateTime.now().format(df);

                PdfPTable card = new PdfPTable(2);
                card.setWidthPercentage(100);
                card.setWidths(new float[]{3f, 1.5f});

                PdfPCell leftCell = new PdfPCell(details);
                leftCell.setBorder(Rectangle.BOX);
                leftCell.setPadding(8);
                card.addCell(leftCell);

                PdfPCell rightCell = new PdfPCell();
                rightCell.setBorder(Rectangle.BOX);
                rightCell.setPadding(8);
                try {
                    Image qr = generateQrImage(qrContent);
                    if (qr != null) {
                        qr.scaleToFit(120, 120);
                        qr.setAlignment(Element.ALIGN_CENTER);
                        rightCell.addElement(qr);
                    }
                } catch (Throwable e) {
                    // en cas d'erreur QR (librairie manquante, etc.), on ignore et on continue
                }
                card.addCell(rightCell);

                doc.add(card);

                // Cachet virtuel en dessous
                doc.add(Chunk.NEWLINE);
                doc.add(createStampElement());
            }
        }

        doc.close();
        return filePath;
    }

    public static String exportTachesToPdf(Set<Tache> taches, String filePath) throws DocumentException, IOException {
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, new FileOutputStream(filePath));
        doc.open();

        Image logo = loadLogo();
        if (logo != null) {
            logo.setAlignment(Element.ALIGN_RIGHT);
            doc.add(logo);
        }

        Paragraph title = new Paragraph("Fiche(s) tâche - AgriGo", titleFont);
        title.setAlignment(Element.ALIGN_LEFT);
        doc.add(title);
        doc.add(new Paragraph("Généré le " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), cellFont));
        doc.add(Chunk.NEWLINE);

        if (taches != null) {
            DateTimeFormatter dfDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dfGen = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int index = 0;
            for (Tache t : taches) {
                index++;
                if (index > 1) {
                    doc.add(Chunk.NEWLINE);
                }

                PdfPTable details = new PdfPTable(2);
                details.setWidthPercentage(100);
                details.setWidths(new float[]{1.5f, 3f});
                details.addCell(detailCell("Titre", true));
                details.addCell(detailCell(safe(t.getTitre_tache()), false));
                details.addCell(detailCell("Description", true));
                details.addCell(detailCell(safe(t.getDescription_tache()), false));
                details.addCell(detailCell("Type", true));
                details.addCell(detailCell(safe(t.getType_tache()), false));
                details.addCell(detailCell("Date", true));
                details.addCell(detailCell(t.getDate_tache() != null ? t.getDate_tache().toLocalDate().format(dfDate) : "", false));
                details.addCell(detailCell("Heure début", true));
                details.addCell(detailCell(t.getHeure_debut_tache() != null ? t.getHeure_debut_tache().toString().substring(0, 5) : "", false));
                details.addCell(detailCell("Heure fin", true));
                details.addCell(detailCell(t.getHeure_fin_tache() != null ? t.getHeure_fin_tache().toString().substring(0, 5) : "", false));
                details.addCell(detailCell("Statut", true));
                details.addCell(detailCell(safe(t.getStatus_tache()), false));
                details.addCell(detailCell("Remarque", true));
                details.addCell(detailCell(safe(t.getRemarque_tache()), false));

                String qrContent = "Tâche AgriGo\n"
                        + "Titre: " + safe(t.getTitre_tache()) + "\n"
                        + "Type: " + safe(t.getType_tache()) + "\n"
                        + "Date: " + (t.getDate_tache() != null ? t.getDate_tache().toLocalDate().format(dfDate) : "") + "\n"
                        + "Début: " + (t.getHeure_debut_tache() != null ? t.getHeure_debut_tache().toString().substring(0, 5) : "") + "\n"
                        + "Fin: " + (t.getHeure_fin_tache() != null ? t.getHeure_fin_tache().toString().substring(0, 5) : "") + "\n"
                        + "Statut: " + safe(t.getStatus_tache()) + "\n"
                        + "Remarque: " + safe(t.getRemarque_tache()) + "\n"
                        + "Généré le: " + LocalDateTime.now().format(dfGen);

                PdfPTable card = new PdfPTable(2);
                card.setWidthPercentage(100);
                card.setWidths(new float[]{3f, 1.5f});

                PdfPCell leftCell = new PdfPCell(details);
                leftCell.setBorder(Rectangle.BOX);
                leftCell.setPadding(8);
                card.addCell(leftCell);

                PdfPCell rightCell = new PdfPCell();
                rightCell.setBorder(Rectangle.BOX);
                rightCell.setPadding(8);
                try {
                    Image qr = generateQrImage(qrContent);
                    if (qr != null) {
                        qr.scaleToFit(120, 120);
                        qr.setAlignment(Element.ALIGN_CENTER);
                        rightCell.addElement(qr);
                    }
                } catch (Throwable e) {
                    // on ignore toute erreur liée au QR code pour ne pas bloquer l'export
                }
                card.addCell(rightCell);

                doc.add(card);

                doc.add(Chunk.NEWLINE);
                doc.add(createStampElement());
            }
        }

        doc.close();
        return filePath;
    }

    private static PdfPCell cell(String text, boolean header) {
        PdfPCell c = new PdfPCell(new Phrase(text, header ? headerFont : cellFont));
        c.setPadding(4);
        if (header) c.setBackgroundColor(new java.awt.Color(0x22, 0x8B, 0x22));
        return c;
    }

    private static PdfPCell detailCell(String text, boolean header) {
        PdfPCell c = new PdfPCell(new Phrase(text, header ? headerFont : cellFont));
        c.setPadding(5);
        if (header) {
            c.setBackgroundColor(new java.awt.Color(0xF0, 0xF0, 0xF0));
        }
        return c;
    }

    private static String safe(String value) {
        return value != null ? value : "";
    }

    private static Image loadLogo() {
        try {
            URL url = ExportPdfService.class.getResource("/assets/logoooooooooo.png");
            if (url == null) return null;
            Image img = Image.getInstance(url);
            img.scaleToFit(80, 80);
            return img;
        } catch (Exception e) {
            return null;
        }
    }

    private static Image loadStampImage() {
        try {
            URL url = ExportPdfService.class.getResource("/assets/stamp_agrigo.png");
            if (url == null) return null;
            Image img = Image.getInstance(url);
            img.scaleToFit(80, 80);
            return img;
        } catch (Exception e) {
            return null;
        }
    }

    private static Element createStampElement() {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(30);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Image stamp = loadStampImage();
        PdfPCell cell;
        if (stamp != null) {
            stamp.setAlignment(Element.ALIGN_CENTER);
            cell = new PdfPCell(stamp, true);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(4);
            cell.setBorder(Rectangle.NO_BORDER);
        } else {
            // Fallback texte si l'image de cachet n'est pas trouvée
            Phrase phrase = new Phrase("Cachet électronique\nAgriGo", stampFont);
            cell = new PdfPCell(phrase);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(6);
            cell.setBorderWidth(2f);
            cell.setBorderColor(new java.awt.Color(0xCC, 0x00, 0x00));
        }

        table.addCell(cell);
        return table;
    }

    private static Image generateQrImage(String content) throws WriterException, BadElementException, IOException {
        int size = 140;
        QRCodeWriter qrWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrWriter.encode(content, BarcodeFormat.QR_CODE, size, size);

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int black = 0xFF000000;
        int white = 0xFFFFFFFF;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? black : white);
            }
        }
        return Image.getInstance(image, null);
    }
}
