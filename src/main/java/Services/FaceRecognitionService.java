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
 * - Cascade Haar pour la detection
 * - LBPH pour l'entrainement / prediction
 */
public class FaceRecognitionService {

    private CascadeClassifier faceDetector;
    private LBPHFaceRecognizer recognizer;

    private static final String DEFAULT_CASCADE_PATH = "src/main/resources/assets/haarcascade_frontalface_default.xml";
    private static final String MODEL_PATH = "models/lbph_model.xml";
    private static final String DEFAULT_PHOTOS_ROOT = "user-photos";

    private boolean modelReady = false;

    public FaceRecognitionService() {
        File cascadeFile = new File(DEFAULT_CASCADE_PATH);
        if (!cascadeFile.exists()) {
            System.err.println("Fichier cascade introuvable: " + DEFAULT_CASCADE_PATH);
            System.err.println("La reconnaissance faciale sera desactivee.");
            faceDetector = null;
        } else {
            faceDetector = new CascadeClassifier(DEFAULT_CASCADE_PATH);
            if (faceDetector.empty()) {
                System.err.println("Le fichier cascade est vide ou invalide: " + DEFAULT_CASCADE_PATH);
                System.err.println("La reconnaissance faciale sera desactivee.");
                faceDetector = null;
            } else {
                System.out.println("Cascade classifier charge avec succes.");
            }
        }

        recognizer = LBPHFaceRecognizer.create();

        File mf = new File(MODEL_PATH);
        if (mf.exists() && mf.length() > 0) {
            try {
                recognizer.read(MODEL_PATH);
                modelReady = true;
                System.out.println("Face recognizer model charge depuis " + MODEL_PATH);
            } catch (Exception e) {
                System.err.println("Impossible de charger le modele: " + e.getMessage());
                modelReady = false;
            }
        }
    }

    public boolean isAvailable() {
        return faceDetector != null && !faceDetector.empty();
    }

    /** Detecte le visage principal, avec fallback pour images de faible resolution. */
    public Mat detectAndPrepareFace(Mat image) {
        if (image == null || image.empty()) return null;

        if (faceDetector == null) {
            System.out.println("La reconnaissance faciale est desactivee.");
            return null;
        }

        Mat gray = new Mat();
        opencv_imgproc.cvtColor(image, gray, opencv_imgproc.COLOR_BGR2GRAY);
        opencv_imgproc.equalizeHist(gray, gray);

        Rect faceRect = detectBestFace(gray);
        Mat faceSource = gray;

        // Fallback: agrandir les petites images (ex: 320x240) pour aider le cascade.
        if (faceRect == null && (gray.cols() < 500 || gray.rows() < 500)) {
            Mat upscaled = new Mat();
            opencv_imgproc.resize(gray, upscaled, new Size(gray.cols() * 2, gray.rows() * 2), 0, 0, opencv_imgproc.INTER_CUBIC);
            opencv_imgproc.equalizeHist(upscaled, upscaled);

            faceRect = detectBestFace(upscaled);
            if (faceRect != null) {
                faceSource = upscaled;
            }
        }

        if (faceRect == null) return null;

        Mat face = new Mat(faceSource, faceRect).clone();
        opencv_imgproc.resize(face, face, new Size(200, 200));
        return face;
    }

    private Rect detectBestFace(Mat gray) {
        RectVector faces = new RectVector();

        // Premiere passe, assez permissive.
        faceDetector.detectMultiScale(gray, faces, 1.08, 4, 0, new Size(30, 30), new Size());

        // Deuxieme passe si rien trouve.
        if (faces.size() == 0) {
            faceDetector.detectMultiScale(gray, faces, 1.05, 3, 0, new Size(24, 24), new Size());
        }

        if (faces.size() == 0) return null;

        // Retourner le plus grand visage detecte.
        Rect best = faces.get(0);
        long bestArea = (long) best.width() * best.height();
        for (int i = 1; i < faces.size(); i++) {
            Rect r = faces.get(i);
            long area = (long) r.width() * r.height();
            if (area > bestArea) {
                best = r;
                bestArea = area;
            }
        }
        return best;
    }

    public boolean trainFromDisk(String photosRoot) {
        File root = new File(photosRoot);
        if (!root.exists() || !root.isDirectory()) {
            System.err.println("Repertoire de photos introuvable: " + photosRoot);
            modelReady = false;
            return false;
        }

        List<Mat> faceMats = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();

        File[] userDirs = root.listFiles(File::isDirectory);
        if (userDirs == null) {
            modelReady = false;
            return false;
        }

        for (File d : userDirs) {
            int label;
            try {
                label = Integer.parseInt(d.getName());
            } catch (NumberFormatException ex) {
                System.out.println("Ignore (nom de dossier non entier): " + d.getName());
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
            System.err.println("Aucune image de visage trouvee pour l'entrainement.");
            modelReady = false;
            return false;
        }

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
            modelReady = true;
            System.out.println("Modele LBPH entraine et sauvegarde -> " + MODEL_PATH);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur pendant l'entrainement: " + e.getMessage());
            modelReady = false;
            return false;
        }
    }

    public PredictionResult predictFromImagePath(String imagePath) {
        Mat img = opencv_imgcodecs.imread(imagePath);
        Mat face = detectAndPrepareFace(img);
        if (face == null) {
            System.out.println("Aucun visage detecte dans l'image: " + imagePath);
            return null;
        }

        if (!ensureModelReady()) {
            System.err.println("Prediction impossible: modele LBPH indisponible (non entraine).");
            return null;
        }

        try {
            return predictPreparedFace(face);
        } catch (Exception e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("not computed yet") || msg.contains("not computed")) {
                System.err.println("Modele LBPH invalide/non calcule. Tentative de re-entrainement automatique...");
                modelReady = false;
                if (trainFromDisk(DEFAULT_PHOTOS_ROOT)) {
                    try {
                        return predictPreparedFace(face);
                    } catch (Exception retryEx) {
                        System.err.println("Prediction impossible apres re-entrainement: " + retryEx.getMessage());
                        return null;
                    }
                }
            }
            System.err.println("Prediction impossible: " + msg);
            return null;
        }
    }

    private PredictionResult predictPreparedFace(Mat face) {
        IntPointer labelPtr = new IntPointer(1);
        DoublePointer confPtr = new DoublePointer(1);
        recognizer.predict(face, labelPtr, confPtr);
        return new PredictionResult(labelPtr.get(0), confPtr.get(0));
    }

    private boolean ensureModelReady() {
        if (modelReady) return true;

        File model = new File(MODEL_PATH);
        if (model.exists() && model.length() > 0) {
            try {
                recognizer.read(MODEL_PATH);
                modelReady = true;
                return true;
            } catch (Exception e) {
                System.err.println("Lecture du modele impossible, re-entrainement requis: " + e.getMessage());
            }
        }

        return trainFromDisk(DEFAULT_PHOTOS_ROOT);
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
