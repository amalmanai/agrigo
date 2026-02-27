package Services;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Service simple de reconnaissance faciale local utilisant OpenCV (Bytedeco)
 * - Cascade Haar pour la détection
 * - LBPH pour l'entraînement / prédiction
 */
public class FaceRecognitionService {

    private CascadeClassifier faceDetector;
    private LBPHFaceRecognizer recognizer;
    private static final String DEFAULT_CASCADE_PATH = "src/main/resources/assets/haarcascade_frontalface_default.xml";
    private static final String MODEL_PATH = "models/lbph_model.xml";

    public FaceRecognitionService() {
        faceDetector = new CascadeClassifier(DEFAULT_CASCADE_PATH);
        recognizer = LBPHFaceRecognizer.create();

        File mf = new File(MODEL_PATH);
        if (mf.exists()) {
            try {
                recognizer.read(MODEL_PATH);
                System.out.println("Face recognizer model chargé depuis " + MODEL_PATH);
            } catch (Exception e) {
                System.err.println("Impossible de charger le modèle: " + e.getMessage());
            }
        }
    }

    /** Détecte le premier visage et retourne une image grise redimensionnée 200x200 */
    public Mat detectAndPrepareFace(Mat image) {
        if (image == null || image.empty()) return null;

        Mat gray = new Mat();
        opencv_imgproc.cvtColor(image, gray, opencv_imgproc.COLOR_BGR2GRAY);

        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(gray, faces);

        if (faces.size() == 0) return null;

        Rect r = faces.get(0);
        Mat face = new Mat(gray, r).clone();
        opencv_imgproc.resize(face, face, new Size(200, 200));
        return face;
    }

    /** Entraîne le modèle depuis le dossier user-photos/{id}/*.png */
    public boolean trainFromDisk(String photosRoot) {
        File root = new File(photosRoot);
        if (!root.exists() || !root.isDirectory()) {
            System.err.println("Répertoire de photos introuvable: " + photosRoot);
            return false;
        }

        List<Mat> faceMats = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();

        File[] userDirs = root.listFiles(File::isDirectory);
        if (userDirs == null) return false;

        for (File d : userDirs) {
            int label;
            try {
                label = Integer.parseInt(d.getName());
            } catch (NumberFormatException ex) {
                System.out.println("Ignoré (nom de dossier non entier): " + d.getName());
                continue;
            }

            File[] imgs = d.listFiles((f, name) -> {
                String n = name.toLowerCase();
                return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg");
            });
            if (imgs == null) continue;

            for (File img : imgs) {
                Mat m = opencv_imgcodecs.imread(img.getAbsolutePath());
                Mat face = detectAndPrepareFace(m);
                if (face != null) {
                    faceMats.add(face);
                    labels.add(label);
                }
            }
        }

        if (faceMats.isEmpty()) {
            System.err.println("Aucune image de visage trouvée pour l'entraînement.");
            return false;
        }

        // Préparer les MatVector et labelsMat
        MatVector images = new MatVector(faceMats.size());
        for (int i = 0; i < faceMats.size(); i++) images.put(i, faceMats.get(i));

        Mat labelsMat = new Mat(faceMats.size(), 1, opencv_core.CV_32SC1);
        IntBuffer buf = labelsMat.createBuffer();
        for (int i = 0; i < labels.size(); i++) buf.put(i, labels.get(i));

        try {
            recognizer.train(images, labelsMat);

            File modelsDir = new File("models");
            if (!modelsDir.exists()) modelsDir.mkdirs();

            recognizer.save(MODEL_PATH);
            System.out.println("Modèle LBPH entraîné et sauvegardé -> " + MODEL_PATH);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur pendant l'entraînement: " + e.getMessage());
            return false;
        }
    }

    /** Prédit le label à partir d'une image */
    public PredictionResult predictFromImagePath(String imagePath) {
        Mat img = opencv_imgcodecs.imread(imagePath);
        Mat face = detectAndPrepareFace(img);
        if (face == null) {
            System.out.println("Aucun visage détecté dans l'image: " + imagePath);
            return null;
        }

        IntPointer labelPtr = new IntPointer(1);
        DoublePointer confPtr = new DoublePointer(1);

        try {
            recognizer.predict(face, labelPtr, confPtr);
            return new PredictionResult(labelPtr.get(0), confPtr.get(0));
        } catch (Exception e) {
            System.err.println("Prédiction impossible: " + e.getMessage());
            return null;
        }
    }

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