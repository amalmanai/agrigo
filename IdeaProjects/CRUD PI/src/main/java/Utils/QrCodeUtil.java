package Utils;

import Entites.User;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utilitaire centralisé pour les QR codes utilisateurs.
 * Utilise ZXing (déjà présent dans le projet).
 */
public class QrCodeUtil {

    private static final int QR_SIZE = 220;
    private static final String PREFIX = "AGRIGO-USER:";

    /**
     * Contenu encodé dans le QR d'un utilisateur.
     * Format: AGRIGO-USER:{id}:{email}
     */
    public static String buildUserPayload(User user) {
        return PREFIX + user.getId_user() + ":" + user.getEmail_user();
    }

    /**
     * Génère un PNG de QR code pour l'utilisateur et retourne le chemin absolu du fichier.
     */
    public static String generateUserQrPng(User user) throws WriterException, IOException {
        String payload = buildUserPayload(user);

        BitMatrix matrix = new MultiFormatWriter()
                .encode(payload, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

        Path dir = getQrBaseDirectory();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        String safeEmail = user.getEmail_user().replaceAll("[^a-zA-Z0-9._-]", "_");
        Path file = dir.resolve("user_" + user.getId_user() + "_" + safeEmail + ".png");
        ImageIO.write(image, "PNG", file.toFile());

        return file.toAbsolutePath().toString();
    }

    /**
     * Retourne le chemin attendu du QR code pour cet utilisateur (sans le générer).
     */
    public static Path getExpectedUserQrPath(User user) {
        String safeEmail = user.getEmail_user().replaceAll("[^a-zA-Z0-9._-]", "_");
        return getQrBaseDirectory().resolve("user_" + user.getId_user() + "_" + safeEmail + ".png");
    }

    /**
     * Décode le texte d'un QR code à partir d'un fichier image.
     */
    public static String decodeFromFile(File file) throws IOException, NotFoundException {
        BufferedImage bufferedImage = ImageIO.read(file);
        if (bufferedImage == null) {
            throw new IOException("Image non lisible ou format non supporté.");
        }

        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
    }

    /**
     * Vérifie qu'un texte de QR correspond bien au format attendu pour un utilisateur.
     */
    public static boolean isUserPayload(String text) {
        return text != null && text.startsWith(PREFIX);
    }

    /**
     * Retourne le répertoire de base où sont stockés les QR codes utilisateurs.
     */
    public static Path getQrBaseDirectory() {
        // Chemin demandé (Windows)
        return Path.of("C:\\Users\\Amal\\AgriGo\\user-qrs");
    }
}

