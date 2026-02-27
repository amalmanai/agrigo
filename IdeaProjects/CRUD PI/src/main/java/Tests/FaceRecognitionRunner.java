package Tests;

import Services.FaceRecognitionService;

public class FaceRecognitionRunner {
    public static void main(String[] args) {
        FaceRecognitionService fr = new FaceRecognitionService();
        boolean trained = fr.trainFromDisk("user-photos");
        System.out.println("Train result: " + trained);
        if (args.length > 0) {
            String testImage = args[0];
            FaceRecognitionService.PredictionResult res = fr.predictFromImagePath(testImage);
            System.out.println("Prediction: " + res);
        } else {
            System.out.println("Aucun chemin d'image de test fourni en argument.");
        }
    }
}

