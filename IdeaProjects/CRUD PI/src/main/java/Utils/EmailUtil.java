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

    /**
     * Envoie un email avec pièce jointe. Les paramètres SMTP doivent être fournis via variables d'environnement :
     * SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASSWORD, SMTP_FROM.
     */
    public static void sendEmailWithAttachment(String to, String subject, String body, File attachment) throws MessagingException {
        final String host = System.getenv("SMTP_HOST");
        String port = System.getenv("SMTP_PORT");
        final String user = System.getenv("SMTP_USER");
        final String pass = System.getenv("SMTP_PASSWORD");
        final String passVal = pass == null ? "" : pass;
        String from = System.getenv("SMTP_FROM");

        if (host == null || host.isEmpty()) throw new IllegalStateException("SMTP_HOST not configured");
        if (port == null || port.isEmpty()) port = "587";
        if (user == null || user.isEmpty()) throw new IllegalStateException("SMTP_USER not configured");
        if (from == null || from.isEmpty()) from = user;

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, passVal);
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
}
