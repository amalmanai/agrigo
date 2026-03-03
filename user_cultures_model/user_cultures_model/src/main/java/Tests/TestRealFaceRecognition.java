package Tests;

import Services.FaceRecognitionService;

public class TestRealFaceRecognition {
    public static void main(String[] args) {
        System.out.println("=== Test Reconnaissance Faciale OpenCV ===");
        
        try {
            FaceRecognitionService fr = new FaceRecognitionService();
            
            // Test de disponibilité
            System.out.println("Service disponible: " + fr.isAvailable());
            
            if (fr.isAvailable()) {
                // Test d'entraînement
                boolean trained = fr.trainFromDisk("user-photos");
                System.out.println("Entraînement réussi: " + trained);
                
                // Test de prédiction si des photos existent
                FaceRecognitionService.PredictionResult result = fr.predictFromImagePath("user-photos/24/photo1.png");
                System.out.println("Résultat prédiction: " + result);
            } else {
                System.out.println("La reconnaissance faciale n'est pas disponible");
            }
            
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== Test terminé ===");
    }
}
