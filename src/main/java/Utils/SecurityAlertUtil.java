package Utils;

import Entites.User;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class SecurityAlertUtil {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_USERNAME = "amalmanai658@gmail.com";
    private static final String SMTP_PASSWORD = "fnkxdursfaoccbtp";

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Appelé lorsqu'un utilisateur fait une tentative de connexion échouée
     * Si failedAttempts >= 3, capture la webcam et envoie un email.
     */
    public static void handleFailedLogin(User user, int failedAttempts) {

        if (user == null || user.getEmail_user() == null) return;

        File snapshot = null;

        // Si 3 tentatives échouées, capture l'image
        if (failedAttempts >= 3) {
            snapshot = captureUserImage();
        }

        try {
            sendSecurityEmail(user, failedAttempts, snapshot);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ouvre la webcam, capture une image, la sauvegarde localement et retourne le fichier
     */
    private static File captureUserImage() {
        Webcam webcam = null;
        File snapshotFile = new File("intrusion.png");

        try {
            webcam = Webcam.getDefault();
            if (webcam != null) {
                webcam.setViewSize(WebcamResolution.VGA.getSize());
                webcam.open();

                BufferedImage image = webcam.getImage();
                if (image != null) {
                    ImageIO.write(image, "PNG", snapshotFile);
                    System.out.println("Image capturée et sauvegardée: " + snapshotFile.getAbsolutePath());
                }
            } else {
                System.err.println("Aucune webcam détectée !");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (webcam != null && webcam.isOpen()) {
                webcam.close();
            }
        }

        return snapshotFile.exists() ? snapshotFile : null;
    }

    /**
     * Envoie l'email de sécurité
     */
    private static void sendSecurityEmail(User user,
                                          int failedAttempts,
                                          File snapshot) throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));

        jakarta.mail.Session mailSession = jakarta.mail.Session.getInstance(
                props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                    }
                }
        );

        String to = user.getEmail_user();
        String subject = "Alerte de sécurité – Tentatives de connexion échouées";
        String dateTime = LocalDateTime.now().format(FORMATTER);

        StringBuilder body = new StringBuilder();
        body.append("Bonjour ").append(user.getNom_user()).append(",\n\n");
        body.append("Nous avons détecté ").append(failedAttempts)
                .append(" tentatives de connexion échouées à votre compte AgriGo.\n");
        body.append("Date et heure : ").append(dateTime).append("\n\n");
        body.append("Si ce n'était pas vous, changez votre mot de passe immédiatement.\n\n");
        body.append("Message automatique.");

        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(SMTP_USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject, "UTF-8");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(body.toString(), "UTF-8");

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);

        if (snapshot != null && snapshot.exists()) {
            MimeBodyPart imagePart = new MimeBodyPart();
            try {
                imagePart.attachFile(snapshot);
                imagePart.setFileName("intrusion.png");
            } catch (IOException e) {
                e.printStackTrace();
            }
            multipart.addBodyPart(imagePart);
        }

        message.setContent(multipart);

        Transport.send(message);

        System.out.println("Email d'alerte envoyé à " + to);
    }
}