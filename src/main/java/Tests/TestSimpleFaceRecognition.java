package Tests;

import Services.SimpleFaceRecognitionService;

public class TestSimpleFaceRecognition {
    public static void main(String[] args) {
        System.out.println("=== Test Simple Face Recognition ===");
        
        SimpleFaceRecognitionService service = new SimpleFaceRecognitionService();
        
        // Test de disponibilité
        System.out.println("Service disponible: " + service.isAvailable());
        
        // Test d'entraînement
        boolean trained = service.trainFromDisk("user-photos");
        System.out.println("Entraînement simulé: " + trained);
        
        // Test de prédiction
        SimpleFaceRecognitionService.PredictionResult result = service.predictFromImagePath("test.jpg");
        System.out.println("Résultat prédiction: " + result);
        
        System.out.println("=== Test terminé ===");
    }
}
