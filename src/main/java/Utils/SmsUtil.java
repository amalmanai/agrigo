package Utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Petit utilitaire pour envoyer des SMS via l'API REST de Vonage (anciennement Nexmo).
 *
 * Configuration (ordre de priorité) :
 * 1) Variables d'environnement VONAGE_API_KEY / VONAGE_API_SECRET / SMS_FROM
 * 2) Fichier classpath `/sms.properties` ou `src/main/resources/sms.properties` contenant :
 *    api_key=YOUR_KEY
 *    api_secret=YOUR_SECRET
 *    sms_from=AGRIGO
 *
 * IMPORTANT : ne commitez jamais vos clés en clair dans un repo public. Préférez des variables d'environnement
 * ou un mécanisme de vault en production.
 */
public class SmsUtil {

    private static final String CONFIG_PATH = "/src/main/resources/sms.properties";

    private static Properties loadConfig() throws IOException {
        Properties p = new Properties();
        // essayer d'abord le classpath
        try (var is = SmsUtil.class.getResourceAsStream("/sms.properties")) {
            if (is != null) {
                p.load(is);
                return p;
            }
        }
        // fallback sur chemin relatif au projet (utile en IDE)
        try (var fis = new FileInputStream(System.getProperty("user.dir") + CONFIG_PATH)) {
            p.load(fis);
        } catch (IOException ignored) {
            // si non trouvé, on retourne une Properties vide — on gérera via variables d'environnement
        }
        return p;
    }

    public static boolean sendSms(String to, String text) throws IOException {
        Properties cfg = loadConfig();

        // Variables d'environnement prennent la priorité si présentes
        String apiKey = System.getenv().getOrDefault("VONAGE_API_KEY", cfg.getProperty("api_key", ""));
        String apiSecret = System.getenv().getOrDefault("VONAGE_API_SECRET", cfg.getProperty("api_secret", ""));
        String from = System.getenv().getOrDefault("SMS_FROM", cfg.getProperty("sms_from", "AGRIGO"));

        if (apiKey.isBlank() || apiSecret.isBlank()) {
            throw new IOException("Configuration SMS incomplète : api_key/api_secret manquants (variables d'environnement ou sms.properties)");
        }

        // normalisation du numéro : garder uniquement les chiffres (supprime le '+')
        String normalizedTo = to == null ? "" : to.trim();
        if (normalizedTo.isEmpty()) throw new IOException("Numéro de téléphone vide");
        // supprimer tout sauf chiffres
        normalizedTo = normalizedTo.replaceAll("[^0-9]", "");
        if (normalizedTo.isEmpty()) throw new IOException("Numéro de téléphone invalide après normalisation");

        String url = "https://rest.nexmo.com/sms/json";

        String postData = "api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
                + "&api_secret=" + URLEncoder.encode(apiSecret, StandardCharsets.UTF_8)
                + "&to=" + URLEncoder.encode(normalizedTo, StandardCharsets.UTF_8)
                + "&from=" + URLEncoder.encode(from, StandardCharsets.UTF_8)
                + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);

        byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);

        // log de diagnostic (ne pas afficher le secret)
        String maskedApiKey = apiKey.length() > 4 ? "****" + apiKey.substring(apiKey.length() - 4) : apiKey;
        String maskedTo = normalizedTo.length() > 4 ? "****" + normalizedTo.substring(normalizedTo.length() - 4) : normalizedTo;
        System.out.println("SmsUtil: sending SMS to=" + maskedTo + " from=" + from + " api_key=" + maskedApiKey);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);

        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            out.write(postDataBytes);
            out.flush();
        }

        int status = conn.getResponseCode();
        BufferedReader in;
        if (status >= 200 && status < 400) {
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        }
        StringBuilder resp = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            resp.append(line).append('\n');
        }
        in.close();

        String response = resp.toString();
        System.out.println("SmsUtil: httpStatus=" + status + " response=" + response);

        // essayer d'extraire le status et l'eventuel error-text pour une erreur plus claire
        String extractedStatus = null;
        String extractedError = null;
        try {
            int idxStatus = response.indexOf("\"status\":");
            if (idxStatus >= 0) {
                int start = response.indexOf('"', idxStatus + 9);
                int end = response.indexOf('"', start + 1);
                if (start >= 0 && end > start) extractedStatus = response.substring(start + 1, end);
            }
            int idxErr = response.indexOf("\"error-text\":");
            if (idxErr >= 0) {
                int start = response.indexOf('"', idxErr + 13);
                int end = response.indexOf('"', start + 1);
                if (start >= 0 && end > start) extractedError = response.substring(start + 1, end);
            }
        } catch (Exception ignored) {}

        boolean ok = response.contains("\"status\":\"0\"") || response.contains("\"status\":0");
        if (!ok) {
            String msg = "Envoi SMS échoué (httpStatus=" + status + ")";
            if (extractedStatus != null) msg += " status=" + extractedStatus;
            if (extractedError != null) msg += " error=" + extractedError;
            msg += " response=" + response;
            throw new IOException(msg);
        }
        return true;
    }
}
