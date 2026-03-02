package Services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de reconnaissance faciale simplifié qui contourne les problèmes OpenCV
 * Pour l'instant, il simule la reconnaissance faciale sans utiliser OpenCV
 */
public class SimpleFaceRecognitionService {
    
    private boolean isAvailable = false;
    
    public SimpleFaceRecognitionService() {
        // Pour l'instant, nous désactivons complètement la reconnaissance faciale
        // pour éviter les erreurs OpenCV
        System.out.println("Reconnaissance faciale simplifiée initialisée (mode simulation)");
        this.isAvailable = false;
    }
    
    /**
     * Vérifie si la reconnaissance faciale est disponible
     */
    public boolean isAvailable() {
        return isAvailable;
    }
    
    /**
     * Simule l'entraînement du modèle
     */
    public boolean trainFromDisk(String photosRoot) {
        System.out.println("Simulation d'entraînement depuis: " + photosRoot);
        
        File root = new File(photosRoot);
        if (!root.exists() || !root.isDirectory()) {
            System.err.println("Répertoire de photos introuvable: " + photosRoot);
            return false;
        }
        
        // Simuler la recherche de photos
        File[] userDirs = root.listFiles(File::isDirectory);
        if (userDirs == null || userDirs.length == 0) {
            System.out.println("Aucun dossier utilisateur trouvé pour l'entraînement");
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
        
        System.out.println("Simulation: " + totalPhotos + " photos trouvées pour " + userDirs.length + " utilisateurs");
        
        // Simuler un succès
        return totalPhotos > 0;
    }
    
    /**
     * Simule la prédiction (retourne toujours null pour désactiver)
     */
    public PredictionResult predictFromImagePath(String imagePath) {
        System.out.println("Simulation de prédiction pour: " + imagePath);
        return null; // Toujours retourner null pour désactiver la reconnaissance faciale
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
