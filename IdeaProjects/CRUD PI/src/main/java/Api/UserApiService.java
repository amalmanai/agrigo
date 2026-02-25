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
 * Module User (admin) : 2 API externes
 * 1) Gravatar : URL de l'avatar à partir de l'email
 * 2) Random User API : générer un utilisateur de démo
 */
public class UserApiService {

    private static final HttpClient httpClient = HttpClient.newBuilder().build();

    /** API 1 : URL Gravatar pour un email (pas d'appel HTTP, juste construction URL) */
    public static String getGravatarUrl(String email, int size) {
        if (email == null || email.isBlank()) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return "https://www.gravatar.com/avatar/" + sb + "?s=" + size + "&d=identicon";
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /** API 2 : Récupérer un utilisateur aléatoire (Random User API) pour démo */
    public static CompletableFuture<RandomUserResult> fetchRandomUser() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://randomuser.me/api/?inc=name,email,phone,location&noinfo"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    if (res.statusCode() != 200)
                        return new RandomUserResult(null, null, null, null, null);
                    String body = res.body();
                    try {
                        String first = extractJsonString(body, "first");
                        String last = extractJsonString(body, "last");
                        String email = extractJsonString(body, "email");
                        String phoneRaw = extractJsonString(body, "phone");
                        String phone = phoneRaw != null ? phoneRaw.replaceAll("[^0-9]", "") : "";
                        if (phone.length() > 12) phone = phone.substring(0, 12);
                        if (phone.isEmpty()) phone = "0";
                        String city = extractJsonString(body, "city");
                        if (city == null) city = extractJsonString(body, "state");
                        return new RandomUserResult(first, last, email, phone, city != null ? city : "");
                    } catch (Exception e) {
                        return new RandomUserResult(null, null, null, null, null);
                    }
                })
                .exceptionally(ex -> new RandomUserResult(null, null, null, null, null));
    }

    private static String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int i = json.indexOf(search);
        if (i == -1) return null;
        i += search.length();
        int j = json.indexOf("\"", i);
        if (j == -1) return null;
        return json.substring(i, j);
    }

    public static class RandomUserResult {
        public final String first;
        public final String last;
        public final String email;
        public final String phone;
        public final String address;
        public RandomUserResult(String first, String last, String email, String phone, String address) {
            this.first = first;
            this.last = last;
            this.email = email;
            this.phone = phone;
            this.address = address;
        }
        public boolean isValid() {
            return first != null && last != null && email != null;
        }
    }
}
