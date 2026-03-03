package Tests;

import Services.HybridFaceRecognitionService;

public class TestHybridFaceRecognition {
    public static void main(String[] args) {
        System.out.println("=== Test Reconnaissance Faciale Hybride ===");
        
        try {
            HybridFaceRecognitionService fr = new HybridFaceRecognitionService();
            
            // Afficher le mode détecté
            System.out.println("Mode: " + fr.getMode());
            System.out.println("Disponible: " + fr.isAvailable());
            
            if (fr.isAvailable()) {
                // Test d'entraînement
                boolean trained = fr.trainFromDisk("user-photos");
                System.out.println("Entrainement: " + (trained ? "SUCCES" : "ECHEC"));
                
                // Test de prédiction
                HybridFaceRecognitionService.PredictionResult result = fr.predictFromImagePath("user-photos/24/photo1.png");
                System.out.println("Prediction: " + result);
            }
            
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== Test termine ===");
    }
}
