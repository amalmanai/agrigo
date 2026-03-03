package Services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service OpenAI spécialisé pour l'assistance agricole
 */
public class OpenAIService {
    
    // Clé API OpenAI à fournir via une variable d'environnement
    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    
    private final OkHttpClient client;
    private final Gson gson;
    
    // Contexte système pour spécialiser l'IA en agriculture
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
    
    public OpenAIService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }
    
    /**
     * Envoie une question à OpenAI et retourne la réponse
     * @param userMessage La question de l'utilisateur
     * @return La réponse de l'IA spécialisée en agriculture
     */
    public String askAgriculturalQuestion(String userMessage) {
        try {
            // Construire le corps de la requête
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "gpt-3.5-turbo");
            requestBody.addProperty("max_tokens", 500);
            requestBody.addProperty("temperature", 0.7);
            
            // Ajouter les messages
            JsonArray messages = new JsonArray();
            
            // Message système
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", SYSTEM_PROMPT);
            messages.add(systemMessage);
            
            // Message utilisateur
            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", userMessage);
            messages.add(userMsg);
            
            requestBody.add("messages", messages);
            
            // Créer la requête HTTP
            RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(OPENAI_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            // Exécuter la requête
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Code HTTP: " + response.code() + " - " + response.body().string());
                }
                
                String responseBody = response.body().string();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                
                // Extraire la réponse
                JsonArray choices = jsonResponse.getAsJsonArray("choices");
                if (choices == null || choices.isEmpty()) {
                    return "Désolé, je n'ai pas pu générer de réponse.";
                }
                
                JsonObject choice = choices.get(0).getAsJsonObject();
                JsonObject message = choice.getAsJsonObject("message");
                return message.get("content").getAsString();
            }
                    
        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel à OpenAI: " + e.getMessage());
            return "Désolé, je rencontre actuellement des difficultés techniques. Veuillez réessayer plus tard ou contacter notre support.";
        }
    }
    
    /**
     * Teste la connexion à l'API OpenAI
     * @return true si la connexion fonctionne, false sinon
     */
    public boolean testConnection() {
        try {
            String testResponse = askAgriculturalQuestion("Bonjour, je suis un agriculteur. Peux-tu m'aider ?");
            return testResponse != null && !testResponse.contains("difficultés techniques");
        } catch (Exception e) {
            System.err.println("Test de connexion OpenAI échoué: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Ferme proprement le service
     */
    public void close() {
        // OkHttp se ferme automatiquement
    }
}
