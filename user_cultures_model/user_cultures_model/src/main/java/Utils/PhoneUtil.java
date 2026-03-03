package Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PhoneUtil {

    private static final String CONFIG_PATH = "/src/main/resources/sms.properties";

    private static Properties loadConfig() {
        Properties p = new Properties();
        try (var is = PhoneUtil.class.getResourceAsStream("/sms.properties")) {
            if (is != null) {
                p.load(is);
                return p;
            }
        } catch (IOException ignored) {}
        try (var fis = new FileInputStream(System.getProperty("user.dir") + CONFIG_PATH)) {
            p.load(fis);
        } catch (IOException ignored) {}
        return p;
    }

    public static String normalizePhone(String raw) {
        return normalizePhone(raw, null);
    }

    public static String normalizePhone(String raw, String defaultCountryOverride) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;
        // remove anything except digits and plus
        s = s.replaceAll("[\\s()\\-]", "");
        // if starts with +, remove plus for our representation
        if (s.startsWith("+")) s = s.substring(1);
        // if starts with 00, strip it
        if (s.startsWith("00")) s = s.substring(2);

        String defaultCountry = defaultCountryOverride;
        if (defaultCountry == null || defaultCountry.isBlank()) {
            var cfg = loadConfig();
            defaultCountry = System.getenv().getOrDefault("SMS_DEFAULT_COUNTRY", cfg.getProperty("default_country", "216"));
        }

        // remove all non digits
        s = s.replaceAll("[^0-9]", "");
        if (s.isEmpty()) return null;

        // if begins with 0 -> replace with default country
        if (s.startsWith("0") && s.length() > 1) {
            s = defaultCountry + s.substring(1);
        } else if (s.length() <= 8) {
            // likely local short number, prefix with default country
            s = defaultCountry + s;
        }
        return s;
    }

    // Nouvelle méthode pour récupérer le code pays par défaut
    public static String getDefaultCountry() {
        var cfg = loadConfig();
        return System.getenv().getOrDefault("SMS_DEFAULT_COUNTRY", cfg.getProperty("default_country", "216"));
    }
}
