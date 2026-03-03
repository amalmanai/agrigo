package Utils;

import Entites.Tache;
import Entites.User;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Fonctionnalité avancée 1 : Export PDF des listes (utilisateurs et tâches).
 *
 * Design : Noir, Blanc et Vert — palette AgriGo
 *   - Fond page     : blanc pur (#FFFFFF)
 *   - Labels        : fond vert très clair (#F0F5F0), texte vert foncé (#1E6B1E)
 *   - Valeurs       : fond blanc, texte noir (#111111)
 *   - Bordures      : gris neutre (#CCCCCC)
 *   - Titre italique : vert (#1E6B1E)
 *   - Cachet        : bordure + texte vert (#1E6B1E)
 *   - Sections sig  : 2 colonnes — Responsable (pré-remplie) / Agent AgriGo (vide)
 */
public class ExportPdfService {

    // ── Palette couleurs ──────────────────────────────────────────────────────
    private static final Color COLOR_GREEN       = new Color(0x1E, 0x6B, 0x1E);
    private static final Color COLOR_GREEN_LIGHT = new Color(0xF0, 0xF5, 0xF0);
    private static final Color COLOR_GREEN_MID   = new Color(0x2E, 0x8B, 0x2E);
    private static final Color COLOR_INK         = new Color(0x11, 0x11, 0x11);
    private static final Color COLOR_RULE        = new Color(0xCC, 0xCC, 0xCC);
    private static final Color COLOR_WHITE       = Color.WHITE;
    private static final Color COLOR_FOOTER_GREY = new Color(0x88, 0x88, 0x88);

    // ── Polices ───────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   22, Font.BOLDITALIC, COLOR_INK);
    private static final Font FONT_TITLE_EM = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   22, Font.BOLDITALIC, COLOR_GREEN);
    private static final Font FONT_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA,          9, Font.NORMAL,     COLOR_FOOTER_GREY);
    private static final Font FONT_LABEL    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,     9, Font.BOLD,       COLOR_GREEN);
    private static final Font FONT_VALUE    = FontFactory.getFont(FontFactory.HELVETICA,           9, Font.NORMAL,     COLOR_INK);
    private static final Font FONT_STAMP    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,    10, Font.BOLD,       COLOR_GREEN);
    private static final Font FONT_STAMP_SUB= FontFactory.getFont(FontFactory.HELVETICA,          7, Font.NORMAL,     COLOR_GREEN);
    private static final Font FONT_SIG_LBL  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,     8, Font.BOLD,       new Color(0x44,0x44,0x44));
    private static final Font FONT_SIG_META = FontFactory.getFont(FontFactory.HELVETICA,           7, Font.ITALIC,     COLOR_FOOTER_GREY);
    private static final Font FONT_FOOTER   = FontFactory.getFont(FontFactory.HELVETICA,           8, Font.NORMAL,     COLOR_FOOTER_GREY);
    private static final Font FONT_QR_LBL   = FontFactory.getFont(FontFactory.HELVETICA,           7, Font.NORMAL,     COLOR_FOOTER_GREY);

    // ─────────────────────────────────────────────────────────────────────────
    //  EXPORT UTILISATEURS
    // ─────────────────────────────────────────────────────────────────────────
    public static String exportUsersToPdf(Set<User> users, String filePath)
            throws DocumentException, IOException {

        Document doc = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filePath));
        doc.open();

        // ── En-tête page ──────────────────────────────────────────────────────
        addPageHeader(doc, "Fiche(s) utilisateur", "AgriGo");

        if (users != null) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int index = 0;
            for (User u : users) {
                if (index++ > 0) doc.add(Chunk.NEWLINE);

                // ── Tableau détails + QR ─────────────────────────────────────
                PdfPTable details = buildUserDetailsTable(u);
                String qrContent  = buildUserQrContent(u, df);
                addCardWithQr(doc, details, qrContent);

                // ── Section signatures ────────────────────────────────────────
                doc.add(Chunk.NEWLINE);
                addSignatureSection(doc,
                        "Signature du responsable",
                        "Signature de l'agent AgriGo",
                        "Nom & Qualité : ________________________",
                        "Date : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                + "  ·  Visa AgriGo");

                // ── Cachet ────────────────────────────────────────────────────
                doc.add(Chunk.NEWLINE);
                doc.add(createStampElement());
            }
        }

        addPageFooter(doc, writer);
        doc.close();
        return filePath;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  EXPORT TÂCHES
    // ─────────────────────────────────────────────────────────────────────────
    public static String exportTachesToPdf(Set<Tache> taches, String filePath)
            throws DocumentException, IOException {

        Document doc = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filePath));
        doc.open();

        addPageHeader(doc, "Fiche(s) tâche", "AgriGo");

        if (taches != null) {
            DateTimeFormatter dfDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dfGen  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int index = 0;
            for (Tache t : taches) {
                if (index++ > 0) doc.add(Chunk.NEWLINE);

                PdfPTable details = buildTacheDetailsTable(t, dfDate);
                String qrContent  = buildTacheQrContent(t, dfDate, dfGen);
                addCardWithQr(doc, details, qrContent);

                doc.add(Chunk.NEWLINE);
                addSignatureSection(doc,
                        "Signature de l'agriculteur",
                        "Visa du superviseur",
                        "Nom & Qualité : ________________________",
                        "Date : " + LocalDateTime.now().format(dfDate)
                                + "  ·  Superviseur AgriGo");

                doc.add(Chunk.NEWLINE);
                doc.add(createStampElement());
            }
        }

        addPageFooter(doc, writer);
        doc.close();
        return filePath;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  EN-TÊTE PAGE  (logo + titre + sous-titre + ligne verte)
    // ─────────────────────────────────────────────────────────────────────────
    private static void addPageHeader(Document doc, String titleMain, String titleEm)
            throws DocumentException {

        // Logo aligné à droite dans un tableau 2 colonnes
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{4f, 1f});

        // Colonne gauche : titre + sous-titre
        Paragraph titlePara = new Paragraph();
        titlePara.add(new Chunk(titleMain + "\n", FONT_TITLE));
        titlePara.add(new Chunk(titleEm, FONT_TITLE_EM));
        titlePara.add(new Chunk("\n"));
        titlePara.add(new Chunk(
                "Généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                FONT_SUBTITLE));

        PdfPCell titleCell = new PdfPCell();
        titleCell.addElement(titlePara);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPaddingBottom(12);
        headerTable.addCell(titleCell);

        // Colonne droite : logo
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        logoCell.setVerticalAlignment(Element.ALIGN_TOP);
        Image logo = loadLogo();
        if (logo != null) {
            logo.scaleToFit(72, 72);
            logoCell.addElement(logo);
        }
        headerTable.addCell(logoCell);

        doc.add(headerTable);

        // Ligne séparatrice verte → gris
        drawGreenRule(doc);
        doc.add(Chunk.NEWLINE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LIGNE VERTE (divider)
    // ─────────────────────────────────────────────────────────────────────────
    private static void drawGreenRule(Document doc) throws DocumentException {
        PdfPTable rule = new PdfPTable(1);
        rule.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(" "));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColorBottom(COLOR_GREEN);
        cell.setBorderWidthBottom(1.5f);
        cell.setPaddingBottom(2);
        rule.addCell(cell);
        doc.add(rule);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TABLEAU DÉTAILS UTILISATEUR
    // ─────────────────────────────────────────────────────────────────────────
    private static PdfPTable buildUserDetailsTable(User u) throws DocumentException {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{1.2f, 3f});
        addDetailRow(t, "NOM",       safe(u.getNom_user()));
        addDetailRow(t, "PRÉNOM",    safe(u.getPrenom_user()));
        addDetailRow(t, "EMAIL",     safe(u.getEmail_user()));
        addDetailRow(t, "RÔLE",      safe(u.getRole_user()));
        addDetailRow(t, "TÉLÉPHONE", String.valueOf(u.getNum_user()));
        addDetailRow(t, "ADRESSE",   safe(u.getAdresse_user()));
        return t;
    }

    private static String buildUserQrContent(User u, DateTimeFormatter df) {
        return "Utilisateur AgriGo\n"
                + "Nom: "       + safe(u.getNom_user())    + "\n"
                + "Prénom: "    + safe(u.getPrenom_user()) + "\n"
                + "Email: "     + safe(u.getEmail_user())  + "\n"
                + "Rôle: "      + safe(u.getRole_user())   + "\n"
                + "Téléphone: " + u.getNum_user()          + "\n"
                + "Adresse: "   + safe(u.getAdresse_user())+ "\n"
                + "Généré le: " + LocalDateTime.now().format(df);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TABLEAU DÉTAILS TÂCHE
    // ─────────────────────────────────────────────────────────────────────────
    private static PdfPTable buildTacheDetailsTable(Tache t, DateTimeFormatter dfDate)
            throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 3f});
        addDetailRow(table, "TITRE",       safe(t.getTitre_tache()));
        addDetailRow(table, "DESCRIPTION", safe(t.getDescription_tache()));
        addDetailRow(table, "TYPE",        safe(t.getType_tache()));
        addDetailRow(table, "DATE",        t.getDate_tache() != null
                ? t.getDate_tache().toLocalDate().format(dfDate) : "");
        addDetailRow(table, "HEURE DÉBUT", t.getHeure_debut_tache() != null
                ? t.getHeure_debut_tache().toString().substring(0, 5) : "");
        addDetailRow(table, "HEURE FIN",   t.getHeure_fin_tache() != null
                ? t.getHeure_fin_tache().toString().substring(0, 5) : "");
        addDetailRow(table, "STATUT",      safe(t.getStatus_tache()));
        addDetailRow(table, "REMARQUE",    safe(t.getRemarque_tache()));
        return table;
    }

    private static String buildTacheQrContent(Tache t, DateTimeFormatter dfDate,
                                              DateTimeFormatter dfGen) {
        return "Tâche AgriGo\n"
                + "Titre: "     + safe(t.getTitre_tache())   + "\n"
                + "Type: "      + safe(t.getType_tache())    + "\n"
                + "Date: "      + (t.getDate_tache() != null
                ? t.getDate_tache().toLocalDate().format(dfDate) : "") + "\n"
                + "Début: "     + (t.getHeure_debut_tache() != null
                ? t.getHeure_debut_tache().toString().substring(0, 5) : "") + "\n"
                + "Fin: "       + (t.getHeure_fin_tache() != null
                ? t.getHeure_fin_tache().toString().substring(0, 5) : "") + "\n"
                + "Statut: "    + safe(t.getStatus_tache())  + "\n"
                + "Remarque: "  + safe(t.getRemarque_tache())+ "\n"
                + "Généré le: " + LocalDateTime.now().format(dfGen);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CARTE  (détails à gauche + QR à droite)
    // ─────────────────────────────────────────────────────────────────────────
    private static void addCardWithQr(Document doc, PdfPTable details, String qrContent)
            throws DocumentException, IOException {

        PdfPTable card = new PdfPTable(2);
        card.setWidthPercentage(100);
        card.setWidths(new float[]{3f, 1.5f});

        // Colonne gauche : tableau détails
        PdfPCell leftCell = new PdfPCell(details);
        leftCell.setBorder(Rectangle.BOX);
        leftCell.setBorderColor(COLOR_RULE);
        leftCell.setBorderWidth(1.5f);
        leftCell.setPadding(0);
        card.addCell(leftCell);

        // Colonne droite : QR code + label
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.BOX);
        rightCell.setBorderColor(COLOR_RULE);
        rightCell.setBorderWidth(1.5f);
        rightCell.setPadding(10);
        rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        try {
            Image qr = generateQrImage(qrContent);
            if (qr != null) {
                qr.scaleToFit(110, 110);
                qr.setAlignment(Element.ALIGN_CENTER);
                rightCell.addElement(qr);
            }
        } catch (Throwable ignored) { }

        // Label sous le QR
        Paragraph qrLabel = new Paragraph("Scan · agrigo.tn", FONT_QR_LBL);
        qrLabel.setAlignment(Element.ALIGN_CENTER);
        rightCell.addElement(qrLabel);
        card.addCell(rightCell);

        doc.add(card);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LIGNE DÉTAIL  (label vert clair | valeur blanc)
    // ─────────────────────────────────────────────────────────────────────────
    private static void addDetailRow(PdfPTable table, String label, String value) {
        // Cellule label
        PdfPCell lbl = new PdfPCell(new Phrase(label, FONT_LABEL));
        lbl.setBackgroundColor(COLOR_GREEN_LIGHT);
        lbl.setPadding(6);
        lbl.setBorder(Rectangle.BOTTOM | Rectangle.RIGHT);
        lbl.setBorderColor(COLOR_RULE);
        lbl.setBorderWidth(0.5f);
        lbl.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(lbl);

        // Cellule valeur
        PdfPCell val = new PdfPCell(new Phrase(value, FONT_VALUE));
        val.setBackgroundColor(COLOR_WHITE);
        val.setPadding(6);
        val.setBorder(Rectangle.BOTTOM);
        val.setBorderColor(COLOR_RULE);
        val.setBorderWidth(0.5f);
        val.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(val);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SECTION SIGNATURES  (2 colonnes)
    // ─────────────────────────────────────────────────────────────────────────
    private static void addSignatureSection(Document doc,
                                            String labelLeft, String labelRight,
                                            String metaLeft,  String metaRight)
            throws DocumentException, IOException {

        // Ligne séparatrice fine
        PdfPTable sep = new PdfPTable(1);
        sep.setWidthPercentage(100);
        PdfPCell sepCell = new PdfPCell(new Phrase(" "));
        sepCell.setBorder(Rectangle.BOTTOM);
        sepCell.setBorderColorBottom(COLOR_RULE);
        sepCell.setBorderWidthBottom(1f);
        sepCell.setPaddingBottom(4);
        sep.addCell(sepCell);
        doc.add(sep);
        doc.add(Chunk.NEWLINE);

        PdfPTable sigTable = new PdfPTable(2);
        sigTable.setWidthPercentage(100);
        sigTable.setWidths(new float[]{1f, 1f});

        sigTable.addCell(buildSigCell(labelLeft,  metaLeft,  true));
        sigTable.addCell(buildSigCell(labelRight, metaRight, false));

        doc.add(sigTable);
    }

    /**
     * Construit une cellule de signature.
     * @param label       Titre du champ (ex. "Signature du responsable")
     * @param meta        Texte sous la ligne (date, qualité…)
     * @param prefilled   Si true, insère l'image de signature pré-chargée
     */
    private static PdfPCell buildSigCell(String label, String meta, boolean prefilled)
            throws DocumentException, IOException {

        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(100);

        // Label
        PdfPCell lblCell = new PdfPCell(new Phrase(label.toUpperCase(), FONT_SIG_LBL));
        lblCell.setBorder(Rectangle.NO_BORDER);
        lblCell.setPaddingBottom(4);
        inner.addCell(lblCell);

        // Zone de signature (image ou espace vide)
        PdfPCell zoneCell = new PdfPCell();
        zoneCell.setFixedHeight(60f);
        zoneCell.setBorder(Rectangle.BOTTOM);
        zoneCell.setBorderColorBottom(COLOR_INK);
        zoneCell.setBorderWidthBottom(1.2f);
        zoneCell.setPadding(4);
        if (prefilled) {
            Image sig = loadSignatureImage();
            if (sig != null) {
                sig.scaleToFit(160, 50);
                sig.setAlignment(Element.ALIGN_CENTER);
                zoneCell.addElement(sig);
            }
        }
        inner.addCell(zoneCell);

        // Méta (date, qualité)
        PdfPCell metaCell = new PdfPCell(new Phrase(meta, FONT_SIG_META));
        metaCell.setBorder(Rectangle.NO_BORDER);
        metaCell.setPaddingTop(4);
        inner.addCell(metaCell);

        PdfPCell wrapper = new PdfPCell(inner);
        wrapper.setBorder(Rectangle.NO_BORDER);
        wrapper.setPadding(8);
        return wrapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CACHET ÉLECTRONIQUE  (vert, aligné à droite)
    // ─────────────────────────────────────────────────────────────────────────
    private static Element createStampElement() {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(32);
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
            // Fallback texte avec double bordure verte
            PdfPTable inner = new PdfPTable(1);
            inner.setWidthPercentage(100);

            PdfPCell line1 = new PdfPCell(new Phrase("Cachet électronique", FONT_STAMP));
            line1.setHorizontalAlignment(Element.ALIGN_CENTER);
            line1.setBorder(Rectangle.NO_BORDER);
            line1.setPaddingBottom(2);
            inner.addCell(line1);

            PdfPCell line2 = new PdfPCell(new Phrase("AGRIGO · CERTIFIÉ", FONT_STAMP_SUB));
            line2.setHorizontalAlignment(Element.ALIGN_CENTER);
            line2.setBorder(Rectangle.NO_BORDER);
            inner.addCell(line2);

            cell = new PdfPCell(inner);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8);
            cell.setBorderWidth(2f);
            cell.setBorderColor(COLOR_GREEN);
        }

        table.addCell(cell);
        return table;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PIED DE PAGE  (agrigo.tn · Export automatique  /  PAGE x)
    // ─────────────────────────────────────────────────────────────────────────
    private static void addPageFooter(Document doc, PdfWriter writer)
            throws DocumentException {
        doc.add(Chunk.NEWLINE);
        PdfPTable footer = new PdfPTable(2);
        footer.setWidthPercentage(100);
        footer.setWidths(new float[]{3f, 1f});

        PdfPCell left = new PdfPCell(new Phrase("agrigo.tn · Export automatique", FONT_FOOTER));
        left.setBorder(Rectangle.TOP);
        left.setBorderColorTop(COLOR_RULE);
        left.setBorderWidthTop(1.2f);
        left.setPaddingTop(8);
        footer.addCell(left);

        PdfPCell right = new PdfPCell(new Phrase("PAGE 1 / 1", FONT_FOOTER));
        right.setBorder(Rectangle.TOP);
        right.setBorderColorTop(COLOR_RULE);
        right.setBorderWidthTop(1.2f);
        right.setPaddingTop(8);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        footer.addCell(right);

        doc.add(footer);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  RESSOURCES
    // ─────────────────────────────────────────────────────────────────────────

    /** Logo AgriGo (haut droite) */
    private static Image loadLogo() {
        try {
            URL url = ExportPdfService.class.getResource("/assets/logoo.png");
            if (url == null) return null;
            return Image.getInstance(url);
        } catch (Exception e) { return null; }
    }

    /** Image du cachet (optionnelle) */
    private static Image loadStampImage() {
        try {
            URL url = ExportPdfService.class.getResource("/assets/stamp_agrigo.png");
            if (url == null) return null;
            Image img = Image.getInstance(url);
            img.scaleToFit(80, 80);
            return img;
        } catch (Exception e) { return null; }
    }

    /**
     * Signature pré-remplie (côté Responsable / Agriculteur).
     * Placez le fichier dans /assets/signature.png  (fond transparent recommandé).
     */
    private static Image loadSignatureImage() {
        try {
            URL url = ExportPdfService.class.getResource("/assets/signature.png");
            if (url == null) return null;
            return Image.getInstance(url);
        } catch (Exception e) { return null; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  QR CODE  — priorité : image statique AgriGo, fallback : génération ZXing
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retourne le QR code AgriGo officiel (vert sur blanc).
     * Charge d'abord l'image statique /assets/qrcode_agrigo.png.
     * Si introuvable, génère un QR code dynamique via ZXing avec le contenu fourni.
     *
     * @param content  Données à encoder dans le QR dynamique (fallback uniquement)
     * @return         Image prête à insérer dans le PDF, ou null en cas d'erreur totale
     */
    private static Image generateQrImage(String content)
            throws WriterException, BadElementException, IOException {

        // ── 1. Tentative : QR code statique officiel AgriGo ──────────────────
        Image staticQr = loadFixedQrImage();
        if (staticQr != null) return staticQr;

        // ── 2. Fallback : génération dynamique ZXing ──────────────────────────
        int size = 140;
        QRCodeWriter qrWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrWriter.encode(content, BarcodeFormat.QR_CODE, size, size);

        int w = bitMatrix.getWidth(), h = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF1E6B1E : 0xFFFFFFFF);

        return Image.getInstance(image, null);
    }

    /**
     * Charge le QR code officiel AgriGo depuis les ressources.
     * Fichier attendu : /assets/qrcode_agrigo.png
     * (QR vert #1E6B1E sur fond blanc, pointant vers agrigo.tn)
     */
    private static Image loadFixedQrImage() {
        try {
            URL url = ExportPdfService.class.getResource("/assets/qrcode_agrigo.png");
            if (url == null) return null;
            return Image.getInstance(url);
        } catch (Exception e) { return null; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UTILITAIRES
    // ─────────────────────────────────────────────────────────────────────────
    private static String safe(String value) {
        return value != null ? value : "";
    }
}