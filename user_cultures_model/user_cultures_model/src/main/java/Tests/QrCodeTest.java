package Tests;

import Utils.QrCodeUtil;
import Entites.User;
import java.io.File;

/**
 * Test pour vérifier la lecture des QR codes
 */
public class QrCodeTest {
    
    public static void main(String[] args) {
        System.out.println("=== Test de lecture QR codes ===");
        
        // Test avec un fichier QR existant
        File qrDir = new File("C:\\Users\\Amal\\AgriGo\\user-qrs");
        if (!qrDir.exists()) {
            System.out.println("❌ Dossier QR non trouvé: " + qrDir.getAbsolutePath());
            return;
        }
        
        File[] qrFiles = qrDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (qrFiles == null || qrFiles.length == 0) {
            System.out.println("❌ Aucun fichier QR trouvé dans: " + qrDir.getAbsolutePath());
            return;
        }
        
        System.out.println("📁 Fichiers QR trouvés: " + qrFiles.length);
        
        for (File qrFile : qrFiles) {
            System.out.println("\n--- Test: " + qrFile.getName() + " ---");
            try {
                String content = QrCodeUtil.decodeFromFile(qrFile);
                System.out.println("✅ QR décodé avec succès:");
                System.out.println("   Contenu: " + content);
                
                if (QrCodeUtil.isUserPayload(content)) {
                    System.out.println("✅ Format QR valide pour AgriGo");
                    String[] parts = content.split(":");
                    if (parts.length >= 3) {
                        System.out.println("   ID: " + parts[1]);
                        System.out.println("   Email: " + parts[2]);
                    }
                } else {
                    System.out.println("❌ Format QR invalide pour AgriGo");
                }
                
            } catch (Exception e) {
                System.out.println("❌ Erreur de lecture: " + e.getMessage());
            }
        }
        
        System.out.println("\n=== Test terminé ===");
    }
}
