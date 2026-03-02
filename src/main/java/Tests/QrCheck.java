package Tests;

import java.io.File;

public class QrCheck {
    public static void main(String[] args) {
        System.out.println("QR Check - Basic test");
        
        File qrDir = new File("C:\\Users\\Amal\\AgriGo\\user-qrs");
        if (!qrDir.exists()) {
            System.out.println("ERROR: Directory not found");
            return;
        }
        
        File[] files = qrDir.listFiles();
        if (files == null) {
            System.out.println("ERROR: Cannot list files");
            return;
        }
        
        System.out.println("Files found: " + files.length);
        
        for (File f : files) {
            if (f.getName().endsWith(".png")) {
                System.out.println("QR: " + f.getName() + " - Size: " + f.length() + " bytes");
            }
        }
        
        // Check specific user file
        File userFile = new File("C:\\Users\\Amal\\AgriGo\\user-qrs\\user_24_amalmanai658_gmail.com.png");
        if (userFile.exists()) {
            System.out.println("SUCCESS: User QR file found - " + userFile.length() + " bytes");
        } else {
            System.out.println("ERROR: User QR file not found");
        }
    }
}
