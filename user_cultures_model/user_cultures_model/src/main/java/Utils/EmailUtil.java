package Utils;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.util.Date;
import java.util.Properties;

public class EmailUtil {

    // Configuration SMTP
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_USERNAME = "amalmanai658@gmail.com";
    private static final String SMTP_PASSWORD = "fnkx durs faoc cbtp";
    private static final String SMTP_FROM = "amalmanai658@gmail.com";

    /**
     * Envoie un email avec pièce jointe.
     */
    public static void sendEmailWithAttachment(String to, String subject, String body, File attachment) throws MessagingException {
        final String host = SMTP_HOST;
        final String port = String.valueOf(SMTP_PORT);
        final String user = SMTP_USERNAME;
        final String pass = SMTP_PASSWORD;
        final String from = SMTP_FROM;

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
        msg.setSubject(subject);
        msg.setSentDate(new Date());

        MimeBodyPart textPart = new MimeBodyPart();
        try {
            textPart.setText(body, "utf-8");

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);

            if (attachment != null && attachment.exists()) {
                MimeBodyPart attachPart = new MimeBodyPart();
                DataSource source = new FileDataSource(attachment);
                attachPart.setDataHandler(new DataHandler(source));
                attachPart.setFileName(attachment.getName());
                multipart.addBodyPart(attachPart);
            }

            msg.setContent(multipart);

            Transport.send(msg);
        } catch (MessagingException e) {
            throw e;
        }
    }

    /**
     * Envoie un email simple avec un code de vérification.
     */
    public static void sendVerificationCode(String to, String code) throws MessagingException {
        final String host = SMTP_HOST;
        final String port = String.valueOf(SMTP_PORT);
        final String user = SMTP_USERNAME;
        final String pass = SMTP_PASSWORD;
        final String from = SMTP_FROM;

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
        msg.setSubject("Code de vérification AGRIGO");
        msg.setSentDate(new Date());

        String body = "Bonjour,\n\n" +
                "Votre code de vérification pour réinitialiser votre mot de passe AGRIGO est :\n\n" +
                "🔑 " + code + "\n\n" +
                "Ce code est valide pendant 10 minutes.\n\n" +
                "Cordialement,\n" +
                "L'équipe AGRIGO";

        msg.setText(body, "utf-8");
        Transport.send(msg);
    }
}
