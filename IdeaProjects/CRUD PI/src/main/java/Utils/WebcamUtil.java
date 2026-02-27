package Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WebcamUtil {

    /**
     * Capture une image depuis la webcam par défaut et l'enregistre dans un fichier PNG fourni.
     * Utilise reflection pour appeler la librairie com.github.sarxos.webcam si elle est disponible.
     */
    public static boolean captureToFile(File outFile) throws IOException {
        try {
            Class<?> webcamClass = Class.forName("com.github.sarxos.webcam.Webcam");
            // getDefault()
            java.lang.reflect.Method getDefault = webcamClass.getMethod("getDefault");
            Object webcam = getDefault.invoke(null);
            if (webcam == null) {
                throw new IOException("Aucune webcam détectée");
            }
            // open()
            java.lang.reflect.Method open = webcamClass.getMethod("open");
            open.invoke(webcam);
            // getImage()
            java.lang.reflect.Method getImage = webcamClass.getMethod("getImage");
            Object imageObj = getImage.invoke(webcam);
            if (!(imageObj instanceof BufferedImage)) {
                throw new IOException("Impossible de capturer l'image depuis la webcam (type inattendu)");
            }
            BufferedImage image = (BufferedImage) imageObj;
            ImageIO.write(image, "PNG", outFile);
            // close()
            try {
                java.lang.reflect.Method close = webcamClass.getMethod("close");
                close.invoke(webcam);
            } catch (Exception ignored) {}
            return true;
        } catch (ClassNotFoundException cnf) {
            throw new IOException("La bibliothèque webcam-capture n'est pas présente", cnf);
        } catch (ReflectiveOperationException roe) {
            throw new IOException("Erreur lors de l'accès à la webcam: " + roe.getMessage(), roe);
        }
    }
}
