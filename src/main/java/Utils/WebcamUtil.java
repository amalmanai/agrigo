package Utils;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

public class WebcamUtil {

    /**
     * Capture une image depuis la webcam par defaut et l'enregistre dans un fichier PNG fourni.
     * Utilise reflection pour appeler la librairie com.github.sarxos.webcam si elle est disponible.
     */
    public static boolean captureToFile(File outFile) throws IOException {
        Object webcam = null;
        Class<?> webcamClass;
        try {
            webcamClass = Class.forName("com.github.sarxos.webcam.Webcam");
            Method getDefault = webcamClass.getMethod("getDefault");
            webcam = getDefault.invoke(null);
            if (webcam == null) {
                throw new IOException("Aucune webcam detectee");
            }

            // Tenter de choisir la meilleure resolution disponible.
            try {
                Method getViewSizes = webcamClass.getMethod("getViewSizes");
                Object sizesObj = getViewSizes.invoke(webcam);
                int len = Array.getLength(sizesObj);
                Dimension best = null;
                int bestArea = -1;
                for (int i = 0; i < len; i++) {
                    Object item = Array.get(sizesObj, i);
                    if (item instanceof Dimension) {
                        Dimension d = (Dimension) item;
                        int area = d.width * d.height;
                        if (area > bestArea) {
                            bestArea = area;
                            best = d;
                        }
                    }
                }
                if (best != null) {
                    Method setViewSize = webcamClass.getMethod("setViewSize", Dimension.class);
                    setViewSize.invoke(webcam, best);
                }
            } catch (Exception ignored) {
                // Resolution non configurable, on continue.
            }

            Method open = webcamClass.getMethod("open");
            open.invoke(webcam);

            Method getImage = webcamClass.getMethod("getImage");
            BufferedImage image = null;

            // Laisser la camera se stabiliser (frames initiales parfois sombres/floues).
            for (int i = 0; i < 6; i++) {
                try {
                    Thread.sleep(180);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }

                Object imageObj = getImage.invoke(webcam);
                if (imageObj instanceof BufferedImage) {
                    BufferedImage candidate = (BufferedImage) imageObj;
                    if (candidate.getWidth() > 0 && candidate.getHeight() > 0) {
                        image = candidate;
                    }
                }
            }

            if (image == null) {
                throw new IOException("Impossible de capturer l'image depuis la webcam");
            }

            ImageIO.write(image, "PNG", outFile);
            return true;

        } catch (ClassNotFoundException cnf) {
            throw new IOException("La bibliotheque webcam-capture n'est pas presente", cnf);
        } catch (ReflectiveOperationException roe) {
            throw new IOException("Erreur lors de l'acces a la webcam: " + roe.getMessage(), roe);
        } finally {
            if (webcam != null) {
                try {
                    Class<?> wc = Class.forName("com.github.sarxos.webcam.Webcam");
                    Method close = wc.getMethod("close");
                    close.invoke(webcam);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
