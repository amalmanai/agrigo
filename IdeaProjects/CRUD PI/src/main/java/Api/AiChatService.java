package Api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service générique pour appeler un assistant IA type ChatGPT.
 * IMPORTANT : vous devez fournir vous‑même la clé API et le modèle.
 */
public class AiChatService {

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    // À adapter selon votre fournisseur (OpenAI, autre…)
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    /**
     * Clé API lue depuis une variable d'environnement pour éviter de la mettre en dur dans le code.
     * Exemple sous Windows (PowerShell) :
     *   $env:OPENAI_API_KEY="votre_cle_ici"
     */
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");

    /**
     * Envoie la conversation complète à l'assistant IA et récupère la réponse texte.
     * @param conversationHistorique texte structuré (Utilisateur/Assistant) envoyé comme prompt.
     * @return CompletableFuture avec la réponse IA, ou null si l'appel échoue ou si aucune clé.
     */
    public static CompletableFuture<String> askAgrigoAssistant(String conversationHistorique) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            // Pas de clé → pas d'appel externe, on laisse le contrôleur utiliser son fallback
            return CompletableFuture.completedFuture(null);
        }

        // Prompt système : rôle officiel assistant AGRIGO
        String systemPrompt =
                "Tu es l’assistant officiel du service client AGRIGO. " +
                "Tu aides les agriculteurs et administrateurs à utiliser la plateforme (tâches, parcelles, irrigation, stocks, ventes, récoltes). " +
                "Tu réponds en français, avec des phrases courtes, simples, et des étapes concrètes.";

        String jsonBody = "{"
                + "\"model\":\"gpt-3.5-turbo\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":" + toJsonString(systemPrompt) + "},"
                + "{\"role\":\"user\",\"content\":" + toJsonString(conversationHistorique) + "}"
                + "],"
                + "\"temperature\":0.4"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        return null;
                    }
                    return extractFirstMessageContent(response.body());
                })
                .exceptionally(ex -> null);
    }

    /**
     * Nouvelle méthode : envoie la requête avec stream=true et appelle `onDelta` pour chaque fragment reçu.
     * Retourne un CompletableFuture qui complète avec la réponse complète (ou null si erreur / pas de clé).
     */
    public static CompletableFuture<String> askAgrigoAssistantStream(String conversationHistorique, Consumer<String> onDelta) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        String systemPrompt =
                "Tu es l’assistant officiel du service client AGRIGO. " +
                "Tu aides les agriculteurs et administrateurs à utiliser la plateforme (tâches, parcelles, irrigation, stocks, ventes, récoltes). " +
                "Tu réponds en français, avec des phrases courtes, simples, et des étapes concrètes.";

        String jsonBody = "{"
                + "\"model\":\"gpt-3.5-turbo\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":" + toJsonString(systemPrompt) + "},"
                + "{\"role\":\"user\",\"content\":" + toJsonString(conversationHistorique) + "}"
                + "],"
                + "\"temperature\":0.4,"
                + "\"stream\":true"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .header("Accept", "text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Use a background task to stream and call onDelta as fragments arrive
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder full = new StringBuilder();
            try {
                HttpResponse<java.util.stream.Stream<String>> resp = httpClient.send(request, BodyHandlers.ofLines());
                if (resp.statusCode() != 200) return null;
                java.util.stream.Stream<String> lines = resp.body();
                lines.forEach(line -> {
                    if (line == null || line.isEmpty()) return;
                    line = line.trim();
                    if (line.equals("data: [DONE]") || line.equals("[DONE]")) return;
                    if (line.startsWith("data: ")) {
                        String payload = line.substring(6);
                        String delta = extractDeltaContent(payload);
                        if (delta != null && !delta.isEmpty()) {
                            full.append(delta);
                            try { onDelta.accept(delta); } catch (Exception ignored) {}
                        }
                    } else {
                        // parfois la ligne contient la JSON directement
                        String delta = extractDeltaContent(line);
                        if (delta != null && !delta.isEmpty()) {
                            full.append(delta);
                            try { onDelta.accept(delta); } catch (Exception ignored) {}
                        }
                    }
                });
                return full.toString();
            } catch (Exception e) {
                return null;
            }
        });
    }

    private static String toJsonString(String s) {
        String escaped = s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
        return "\"" + escaped + "\"";
    }

    /**
     * Extraction très simple de choices[0].message.content depuis la réponse JSON.
     * Pour un projet sérieux, utilisez une vraie lib JSON (Jackson, Gson…).
     */
    private static String extractFirstMessageContent(String json) {
        String search = "\"message\":";
        int i = json.indexOf(search);
        if (i == -1) return null;
        String contentKey = "\"content\":\"";
        i = json.indexOf(contentKey, i);
        if (i == -1) return null;
        i += contentKey.length();
        int j = i;
        StringBuilder sb = new StringBuilder();
        while (j < json.length()) {
            char c = json.charAt(j);
            if (c == '\\') {
                if (j + 1 < json.length()) {
                    char next = json.charAt(j + 1);
                    if (next == 'n') {
                        sb.append('\n');
                        j += 2;
                        continue;
                    } else if (next == 'r') {
                        j += 2;
                        continue;
                    } else if (next == '"') {
                        sb.append('"');
                        j += 2;
                        continue;
                    } else if (next == '\\') {
                        sb.append('\\');
                        j += 2;
                        continue;
                    }
                }
            }
            if (c == '"') {
                break;
            }
            sb.append(c);
            j++;
        }
        return sb.toString().trim();
    }

    // Extraction d'un fragment delta.content dans la charge utile JSON du stream
    private static String extractDeltaContent(String json) {
        if (json == null) return null;
        String key = "\"content\":\"";
        int idx = json.indexOf(key);
        if (idx == -1) return null;
        idx += key.length();
        StringBuilder sb = new StringBuilder();
        int j = idx;
        while (j < json.length()) {
            char c = json.charAt(j);
            if (c == '\\') {
                if (j + 1 < json.length()) {
                    char next = json.charAt(j + 1);
                    if (next == 'n') {
                        sb.append('\n');
                        j += 2;
                        continue;
                    } else if (next == 'r') {
                        j += 2;
                        continue;
                    } else if (next == '"') {
                        sb.append('"');
                        j += 2;
                        continue;
                    } else if (next == '\\') {
                        sb.append('\\');
                        j += 2;
                        continue;
                    }
                }
            }
            if (c == '"') break;
            sb.append(c);
            j++;
        }
        return sb.toString();
    }
}
