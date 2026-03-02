package Tests;

import Services.SimpleOpenAIService;

public class TestSimpleOpenAI {
    public static void main(String[] args) {
        System.out.println("=== Test SimpleOpenAI ===");
        
        SimpleOpenAIService service = new SimpleOpenAIService();
        
        // Test de connexion
        System.out.println("Test de connexion...");
        boolean isConnected = service.testConnection();
        System.out.println("Connexion: " + (isConnected ? "OK" : "ECHEC"));
        
        if (isConnected) {
            // Test avec une question agricole
            String question = "Quand planter les tomates en Tunisie ?";
            System.out.println("Question: " + question);
            
            String reponse = service.askAgriculturalQuestion(question);
            System.out.println("Reponse: " + reponse);
        }
        
        service.close();
        System.out.println("Test termine");
    }
}
