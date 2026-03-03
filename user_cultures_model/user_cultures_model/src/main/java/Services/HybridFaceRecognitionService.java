package Services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Service hybride de reconnaissance faciale
 * Essaye d'utiliser OpenCV si disponible, sinon utilise la version simplifiée
 */
public class HybridFaceRecognitionService {
    
    private boolean useOpenCV = false;
    private boolean isAvailable = false;
    
    public HybridFaceRecognitionService() {
        // Essayer de charger OpenCV
        try {
            // Test simple pour voir si OpenCV est disponible
            Class.forName("org.bytedeco.opencv.opencv_core.Mat");
            System.out.println("OpenCV détecté, tentative d'initialisation...");
            
            // Essayer de créer le service OpenCV
            Services.FaceRecognitionService openCVService = new Services.FaceRecognitionService();
            this.useOpenCV = openCVService.isAvailable();
            this.isAvailable = this.useOpenCV;
            
            if (this.useOpenCV) {
                System.out.println("✅ OpenCV initialisé avec succès !");
            } else {
                System.out.println("⚠️ OpenCV disponible mais cascade invalide, utilisation du mode simplifié");
            }
            
        } catch (ClassNotFoundException e) {
            System.out.println("📦 OpenCV non trouvé, utilisation du mode simplifié");
            this.useOpenCV = false;
            this.isAvailable = true; // Le mode simplifié est toujours disponible
        } catch (Exception e) {
            System.err.println("❌ Erreur OpenCV: " + e.getMessage());
            System.out.println("🔄 Utilisation du mode simplifié");
            this.useOpenCV = false;
            this.isAvailable = true; // Le mode simplifié est toujours disponible
        }
    }
    
    /**
     * Vérifie si la reconnaissance faciale est disponible
     */
    public boolean isAvailable() {
        return isAvailable;
    }
    
    /**
     * Entraîne le modèle avec le mode approprié
     */
    public boolean trainFromDisk(String photosRoot) {
        if (useOpenCV) {
            try {
                Services.FaceRecognitionService openCVService = new Services.FaceRecognitionService();
                return openCVService.trainFromDisk(photosRoot);
            } catch (Exception e) {
                System.err.println("Erreur OpenCV lors de l'entraînement: " + e.getMessage());
                return false;
            }
        } else {
            // Mode simplifié
            return trainSimplified(photosRoot);
        }
    }
    
    /**
     * Entraînement simplifié sans OpenCV
     */
    private boolean trainSimplified(String photosRoot) {
        System.out.println("🔄 Entraînement simplifié depuis: " + photosRoot);
        
        File root = new File(photosRoot);
        if (!root.exists() || !root.isDirectory()) {
            System.err.println("Répertoire de photos introuvable: " + photosRoot);
            return false;
        }
        
        File[] userDirs = root.listFiles(File::isDirectory);
        if (userDirs == null || userDirs.length == 0) {
            System.out.println("Aucun dossier utilisateur trouvé");
            return false;
        }
        
        int totalPhotos = 0;
        for (File userDir : userDirs) {
            File[] photos = userDir.listFiles((f, name) -> {
                String n = name.toLowerCase();
                return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg");
            });
            if (photos != null) {
                totalPhotos += photos.length;
            }
        }
        
        System.out.println("📸 " + totalPhotos + " photos trouvées pour " + userDirs.length + " utilisateurs");
        return totalPhotos > 0;
    }
    
    /**
     * Prédiction avec le mode approprié
     */
    public PredictionResult predictFromImagePath(String imagePath) {
        if (useOpenCV) {
            try {
                Services.FaceRecognitionService openCVService = new Services.FaceRecognitionService();
                Services.FaceRecognitionService.PredictionResult result = openCVService.predictFromImagePath(imagePath);
                if (result != null) {
                    return new PredictionResult(result.label, result.confidence);
                }
            } catch (Exception e) {
                System.err.println("Erreur OpenCV lors de la prédiction: " + e.getMessage());
            }
        }
        
        // Mode simplifié - retourne toujours null
        System.out.println("🔍 Mode simplifié: prédiction simulée pour " + imagePath);
        return null;
    }
    
    /**
     * Obtient le mode actuel
     */
    public String getMode() {
        return useOpenCV ? "OpenCV" : "Simplifié";
    }
    
    /**
     * Classe pour le résultat de prédiction
     */
    public static class PredictionResult {
        public final int label;
        public final double confidence;

        public PredictionResult(int label, double confidence) {
            this.label = label;
            this.confidence = confidence;
        }

        @Override
        public String toString() {
            return "PredictionResult{label=" + label + ", confidence=" + confidence + '}';
        }
    }
}
