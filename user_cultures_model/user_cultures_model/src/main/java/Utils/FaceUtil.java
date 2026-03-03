package Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utilitaires simples pour "reconnaissance faciale" basée sur aHash (average hash).
 * Ce n'est pas une solution biométrique robuste — pour production utilisez une librairie spécialisée (OpenCV + modèles).
 */
public class FaceUtil {

    /**
     * Calcule l'aHash (64-bit) d'une BufferedImage.
     */
    public static long averageHash(BufferedImage img) {
        // redimensionner en 8x8
        BufferedImage resized = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resized.createGraphics();
        g.drawImage(img, 0, 0, 8, 8, null);
        g.dispose();

        // calculer la moyenne
        long sum = 0;
        int[] pixels = new int[64];
        int idx = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int rgb = resized.getRGB(x, y) & 0xFF;
                pixels[idx++] = rgb;
                sum += rgb;
            }
        }
        int avg = (int) (sum / 64);

        long hash = 0L;
        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] >= avg) {
                hash |= (1L << i);
            }
        }
        return hash;
    }

    /**
     * Hamming distance entre deux hashes 64-bit
     */
    public static int hammingDistance(long a, long b) {
        long x = a ^ b;
        // compter les bits
        return Long.bitCount(x);
    }

    /**
     * Compare deux fichiers image et retourne la distance (hamming) ; lève IOException si lecture échoue.
     */
    public static int compareImages(File f1, File f2) throws IOException {
        BufferedImage i1 = ImageIO.read(f1);
        BufferedImage i2 = ImageIO.read(f2);
        if (i1 == null || i2 == null) throw new IOException("Impossible de lire les images pour comparaison");
        long h1 = averageHash(i1);
        long h2 = averageHash(i2);
        return hammingDistance(h1, h2);
    }

    /**
     * Charge une resource classpath (si path commence par "classpath:") ou fichier normal.
     * Retourne un File (temp) si la source est une resource.
     */
    public static File resolveToFile(String path) throws IOException {
        if (path == null) return null;
        File f = new File(path);
        if (f.exists()) return f;
        // tenter classpath
        InputStream is = FaceUtil.class.getResourceAsStream(path.startsWith("/") ? path : "/" + path);
        if (is == null) return null;
        // écrire dans un fichier temporaire
        File tmp = File.createTempFile("face_resource_", ".tmp");
        try (java.io.FileOutputStream os = new java.io.FileOutputStream(tmp)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) != -1) os.write(buf, 0, r);
        } finally {
            try { is.close(); } catch (Exception ignored) {}
        }
        return tmp;
    }
}
