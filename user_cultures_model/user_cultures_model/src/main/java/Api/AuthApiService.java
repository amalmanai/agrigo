package Api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

/**
 * Module Auth : 2 API externes
 * 1) Validation d'email (PingUtil - gratuit)
 * 2) Vérification mot de passe compromis (Have I Been Pwned - k-anonymity)
 */
public class AuthApiService {

    private static final HttpClient httpClient = HttpClient.newBuilder().build();

    /** API 1 : Validation d'email (https://api.eva.pingutil.com/email) */
    public static CompletableFuture<EmailValidationResult> validateEmail(String email) {
        if (email == null || email.isBlank()) {
            return CompletableFuture.completedFuture(new EmailValidationResult(false, "Email vide"));
        }
        String encoded = java.net.URLEncoder.encode(email, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.eva.pingutil.com/email?email=" + encoded))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    if (res.statusCode() != 200)
                        return new EmailValidationResult(false, "Service indisponible");
                    try {
                        // Réponse JSON simple : {"valid": true/false, "message": "..."}
                        String body = res.body();
                        boolean valid = body.contains("\"valid\":true") || body.contains("\"deliverable\":true");
                        return new EmailValidationResult(valid, valid ? "Email valide" : "Email invalide ou non livrable");
                    } catch (Exception e) {
                        return new EmailValidationResult(false, "Erreur vérification");
                    }
                })
                .exceptionally(ex -> new EmailValidationResult(false, "Réseau: " + ex.getMessage()));
    }

    /** API 2 : Vérifier si le mot de passe a fuité (HIBP - k-anonymity, pas de clé) */
    public static CompletableFuture<PasswordBreachResult> checkPasswordBreached(String password) {
        if (password == null || password.length() < 5) {
            return CompletableFuture.completedFuture(new PasswordBreachResult(false, 0));
        }
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02X", b));
            }
            String hashStr = hex.toString();
            String prefix = hashStr.substring(0, 5);
            String suffix = hashStr.substring(5);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.pwnedpasswords.com/range/" + prefix))
                    .GET()
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(res -> {
                        if (res.statusCode() != 200)
                            return new PasswordBreachResult(false, 0);
                        String body = res.body();
                        int count = 0;
                        for (String line : body.split("\r\n|\n")) {
                            if (line.startsWith(suffix)) {
                                String[] parts = line.split(":");
                                if (parts.length >= 2)
                                    count = Integer.parseInt(parts[1].trim(), 10);
                                break;
                            }
                        }
                        return new PasswordBreachResult(count > 0, count);
                    })
                    .exceptionally(ex -> new PasswordBreachResult(false, 0));
        } catch (NoSuchAlgorithmException e) {
            return CompletableFuture.completedFuture(new PasswordBreachResult(false, 0));
        }
    }

    public static class EmailValidationResult {
        public final boolean valid;
        public final String message;
        public EmailValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
    }

    public static class PasswordBreachResult {
        public final boolean breached;
        public final int count;
        public PasswordBreachResult(boolean breached, int count) {
            this.breached = breached;
            this.count = count;
        }
    }
}
