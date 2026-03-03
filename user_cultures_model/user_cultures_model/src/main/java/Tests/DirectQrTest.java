package Tests;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

// Import ZXing classes directly
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

public class DirectQrTest {
    public static void main(String[] args) {
        System.out.println("Direct QR Test");
        
        File qrFile = new File("C:\\Users\\Amal\\AgriGo\\user-qrs\\user_24_amalmanai658_gmail.com.png");
        
        if (!qrFile.exists()) {
            System.out.println("ERROR: QR file not found");
            return;
        }
        
        System.out.println("Testing file: " + qrFile.getName());
        System.out.println("File size: " + qrFile.length() + " bytes");
        
        try {
            // Read the image
            BufferedImage image = ImageIO.read(qrFile);
            if (image == null) {
                System.out.println("ERROR: Cannot read image file");
                return;
            }
            
            System.out.println("Image loaded: " + image.getWidth() + "x" + image.getHeight());
            
            // Try to decode
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
            Result result = new MultiFormatReader().decode(bitmap);
            String content = result.getText();
            
            System.out.println("SUCCESS: QR decoded");
            System.out.println("Content: " + content);
            
            // Check format
            if (content != null && content.startsWith("AGRIGO-USER:")) {
                System.out.println("SUCCESS: Valid AgriGo QR format");
                String[] parts = content.split(":");
                if (parts.length >= 3) {
                    System.out.println("User ID: " + parts[1]);
                    System.out.println("Email: " + parts[2]);
                }
            } else {
                System.out.println("ERROR: Invalid QR format");
                System.out.println("Expected: AGRIGO-USER:id:email");
                System.out.println("Got: " + content);
            }
            
        } catch (NotFoundException e) {
            System.out.println("ERROR: No QR code found in image");
            System.out.println("This might mean:");
            System.out.println("- The image is not a QR code");
            System.out.println("- The QR code is too blurry or distorted");
            System.out.println("- The QR code is too small or too large");
        } catch (IOException e) {
            System.out.println("ERROR: Cannot read image file");
            System.out.println("Details: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: Unexpected error");
            System.out.println("Type: " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
