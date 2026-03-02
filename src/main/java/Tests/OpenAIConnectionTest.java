package Tests;

import Services.OpenAIService;

public class OpenAIConnectionTest {
    public static void main(String[] args) {
        System.out.println("=== Test de connexion OpenAI ===");
        
        try {
            OpenAIService service = new OpenAIService();
            System.out.println("Service OpenAI créé");
            
            // Test de connexion
            boolean isConnected = service.testConnection();
            System.out.println("Test de connexion: " + (isConnected ? "SUCCES" : "ECHEC"));
            
            if (isConnected) {
                // Test avec une question simple
                String question = "Bonjour, je suis un agriculteur en Tunisie. Quel conseil pouvez-vous me donner ?";
                System.out.println("Question test: " + question);
                
                String reponse = service.askAgriculturalQuestion(question);
                System.out.println("Réponse: " + reponse);
            }
            
            service.close();
            System.out.println("Test terminé");
            
        } catch (Exception e) {
            System.out.println("ERREUR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
