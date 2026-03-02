package Tests;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Test simple pour verifier si les fichiers QR sont des images valides
 */
public class SimpleQrTest {
    
    public static void main(String[] args) {
        System.out.println("=== Test simple des fichiers QR ===");
        
        File qrDir = new File("C:\\Users\\Amal\\AgriGo\\user-qrs");
        if (!qrDir.exists()) {
            System.out.println("Dossier QR non trouve: " + qrDir.getAbsolutePath());
            return;
        }
        
        File[] qrFiles = qrDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (qrFiles == null || qrFiles.length == 0) {
            System.out.println("Aucun fichier QR trouve");
            return;
        }
        
        System.out.println("Fichiers QR trouves: " + qrFiles.length);
        
        for (File qrFile : qrFiles) {
            System.out.println("\n--- Test: " + qrFile.getName() + " ---");
            
            // Verification basique du fichier
            if (!qrFile.exists()) {
                System.out.println("Fichier n'existe pas");
                continue;
            }
            
            if (!qrFile.canRead()) {
                System.out.println("Fichier non lisible");
                continue;
            }
            
            long size = qrFile.length();
            System.out.println("Taille: " + size + " bytes");
            
            if (size == 0) {
                System.out.println("Fichier vide");
                continue;
            }
            
            // Test si c'est une image valide
            try {
                BufferedImage image = ImageIO.read(qrFile);
                if (image == null) {
                    System.out.println("Format d'image invalide");
                    continue;
                }
                
                System.out.println("Image valide: " + image.getWidth() + "x" + image.getHeight() + " pixels");
                
                // Verifier si l'image semble etre un QR code (carre et petite)
                if (image.getWidth() == image.getHeight() && image.getWidth() < 500) {
                    System.out.println("Format compatible QR (carre, petite taille)");
                } else {
                    System.out.println("Format inhabituel pour un QR code");
                }
                
            } catch (IOException e) {
                System.out.println("Erreur de lecture: " + e.getMessage());
            }
        }
        
        System.out.println("\n=== Test termine ===");
        
        // Test specifique du fichier de l'utilisateur
        System.out.println("\n=== Test specifique pour amalmanai658 ===");
        File userQr = new File("C:\\Users\\Amal\\AgriGo\\user-qrs\\user_24_amalmanai658_gmail.com.png");
        if (userQr.exists()) {
            System.out.println("Fichier QR trouve: " + userQr.getName());
            System.out.println("Taille: " + userQr.length() + " bytes");
            System.out.println("Modifie: " + new java.util.Date(userQr.lastModified()));
        } else {
            System.out.println("Fichier QR non trouve pour amalmanai658");
        }
    }
}
