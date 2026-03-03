package Tests;

import Services.AgriAssistantService;

public class TestAgriAssistant {
    public static void main(String[] args) {
        System.out.println("=== Test Assistant Agricole ===");
        
        AgriAssistantService service = new AgriAssistantService();
        
        // Test 1: Connexion
        System.out.println("Test connexion: " + (service.testConnection() ? "OK" : "ECHEC"));
        
        // Test 2: Questions variées
        String[] questions = {
            "Bonjour",
            "Quand planter les tomates en Tunisie ?",
            "Comment traiter les pucerons naturellement ?",
            "À quelle fréquence arroser les légumes en été ?",
            "Quel engrais NPK pour cultures maraîchères ?",
            "Question hors sujet: Quel temps fait-il ?"
        };
        
        for (String question : questions) {
            System.out.println("\n--- Question: " + question + " ---");
            String reponse = service.askAgriculturalQuestion(question);
            System.out.println("Réponse: " + reponse);
        }
        
        service.close();
        System.out.println("\n=== Test terminé ===");
    }
}
