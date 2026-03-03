package Tests;

import Utils.QrCodeUtil;
import java.io.File;

public class QrDecodeTest {
    public static void main(String[] args) {
        System.out.println("QR Decode Test");
        
        // Test with your specific QR file
        File qrFile = new File("C:\\Users\\Amal\\AgriGo\\user-qrs\\user_24_amalmanai658_gmail.com.png");
        
        if (!qrFile.exists()) {
            System.out.println("ERROR: QR file not found");
            return;
        }
        
        System.out.println("Testing file: " + qrFile.getName());
        System.out.println("File size: " + qrFile.length() + " bytes");
        
        try {
            // Test basic decode
            String content = QrCodeUtil.decodeFromFile(qrFile);
            System.out.println("SUCCESS: QR decoded");
            System.out.println("Content: " + content);
            
            // Test if it's valid user payload
            if (QrCodeUtil.isUserPayload(content)) {
                System.out.println("SUCCESS: Valid AgriGo user QR");
                
                String[] parts = content.split(":");
                if (parts.length >= 3) {
                    System.out.println("User ID: " + parts[1]);
                    System.out.println("Email: " + parts[2]);
                }
            } else {
                System.out.println("ERROR: Not a valid AgriGo QR");
                System.out.println("Expected format: AGRIGO-USER:id:email");
                System.out.println("Got: " + content);
            }
            
        } catch (Exception e) {
            System.out.println("ERROR: Failed to decode QR");
            System.out.println("Error type: " + e.getClass().getSimpleName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
