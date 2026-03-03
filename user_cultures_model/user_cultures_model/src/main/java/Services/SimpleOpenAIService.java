package Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Service OpenAI simplifié sans dépendances externes
 */
public class SimpleOpenAIService {
    
    // Clé API OpenAI à fournir via une variable d'environnement
    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    
    private static final String SYSTEM_PROMPT = """
        Tu es un expert agricole et assistant pour la plateforme AgriGo. Tu aides les agriculteurs et utilisateurs avec :
        
        - Conseils sur les cultures (plantation, arrosage, récolte)
        - Gestion des maladies des plantes et ravageurs
        - Optimisation des ressources (eau, engrais, équipements)
        - Calendriers de plantation et récolte selon les saisons
        - Conseils sur les techniques agricoles modernes
        - Réponses aux questions sur la plateforme AgriGo
        
        Sois toujours :
        - Précis et pratique dans tes conseils
        - Courtois et professionnel
        - Concis mais complet
        - Adapté au contexte agricole (tunisien si possible)
        
        Si une question n'est pas liée à l'agriculture, redirige poliment vers le sujet agricole.
        """;
    
    /**
     * Envoie une question à OpenAI et retourne la réponse
     */
    public String askAgriculturalQuestion(String userMessage) {
        try {
            // Construire le corps de la requête JSON manuellement
            String jsonBody = buildJsonRequest(userMessage);
            
            // Créer la connexion HTTP
            URL url = new URL(OPENAI_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Configurer la requête
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            
            // Envoyer la requête
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Lire la réponse
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    
                    // Extraire le contenu de la réponse JSON
                    return extractContentFromResponse(response.toString());
                }
            } else {
                // Lire l'erreur
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    
                    StringBuilder errorResponse = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                    
                    System.err.println("Erreur HTTP " + responseCode + ": " + errorResponse.toString());
                }
                return "Désolé, une erreur technique s'est produite (code: " + responseCode + ").";
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel à OpenAI: " + e.getMessage());
            return "Désolé, je rencontre actuellement des difficultés techniques. Veuillez réessayer plus tard.";
        }
    }
    
    /**
     * Construit la requête JSON manuellement
     */
    private String buildJsonRequest(String userMessage) {
        return "{"
                + "\"model\":\"gpt-3.5-turbo\","
                + "\"max_tokens\":500,"
                + "\"temperature\":0.7,"
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + escapeJson(SYSTEM_PROMPT) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(userMessage) + "\"}"
                + "]"
                + "}";
    }
    
    /**
     * Extrait le contenu de la réponse JSON
     */
    private String extractContentFromResponse(String jsonResponse) {
        try {
            // Parser manuellement la réponse JSON
            int choicesIndex = jsonResponse.indexOf("\"choices\":[");
            if (choicesIndex == -1) {
                return "Réponse invalide du serveur.";
            }
            
            int messageIndex = jsonResponse.indexOf("\"message\":", choicesIndex);
            if (messageIndex == -1) {
                return "Format de réponse invalide.";
            }
            
            int contentIndex = jsonResponse.indexOf("\"content\":", messageIndex);
            if (contentIndex == -1) {
                return "Contenu de réponse non trouvé.";
            }
            
            int startQuote = jsonResponse.indexOf("\"", contentIndex + 11);
            int endQuote = jsonResponse.indexOf("\"", startQuote + 1);
            
            if (startQuote != -1 && endQuote != -1 && endQuote > startQuote) {
                return jsonResponse.substring(startQuote + 1, endQuote)
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"");
            }
            
            return "Impossible d'extraire le contenu de la réponse.";
            
        } catch (Exception e) {
            System.err.println("Erreur parsing JSON: " + e.getMessage());
            return "Erreur lors du traitement de la réponse.";
        }
    }
    
    /**
     * Échappe les caractères spéciaux pour JSON
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Teste la connexion à l'API OpenAI
     */
    public boolean testConnection() {
        try {
            String response = askAgriculturalQuestion("Bonjour");
            return response != null && !response.contains("difficultés techniques") && !response.contains("erreur");
        } catch (Exception e) {
            System.err.println("Test de connexion échoué: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Ferme proprement le service
     */
    public void close() {
        // Rien à fermer pour cette version simple
    }
}
