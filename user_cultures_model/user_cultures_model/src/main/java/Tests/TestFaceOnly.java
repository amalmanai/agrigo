package Tests;

import Services.SimpleFaceRecognitionService;

public class TestFaceOnly {
    public static void main(String[] args) {
        System.out.println("=== Test Reconnaissance Faciale Simple ===");
        
        SimpleFaceRecognitionService fr = new SimpleFaceRecognitionService();
        
        System.out.println("Disponible: " + fr.isAvailable());
        
        boolean trained = fr.trainFromDisk("user-photos");
        System.out.println("Entrainement: " + trained);
        
        SimpleFaceRecognitionService.PredictionResult result = fr.predictFromImagePath("test.jpg");
        System.out.println("Resultat: " + result);
        
        System.out.println("=== Test termine ===");
    }
}
