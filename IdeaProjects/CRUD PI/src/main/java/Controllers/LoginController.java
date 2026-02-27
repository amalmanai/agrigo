package Controllers;

import Api.AuthApiService;
import Entites.User;
import Services.ServiceUser;
import Utils.QrCodeUtil;
import Utils.SecurityAlertUtil;
import Utils.Session;
import Utils.FaceUtil;
import Utils.WebcamUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.SnapshotParameters;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField tfemaillogin;

    @FXML
    private PasswordField tfmotDePasselogin;

    @FXML
    private TextField tfmotDePasseloginVisible;

    @FXML
    private Button btnTogglePassword;

    @FXML
    private Label labelApiStatus;

    @FXML
    private ImageView captchaImageView;

    @FXML
    private TextField tfCaptcha;

    @FXML
    private Label labelCaptchaStatus;

    private final ServiceUser serviceUser = new ServiceUser();

    private final SecureRandom random = new SecureRandom();
    private String currentCaptcha;

    // Compteur de tentatives de connexion échouées pour la session courante
    private int failedLoginAttempts = 0;

    private static final int FACE_MATCH_THRESHOLD = 12; // distance hamming max pour considérer que c'est le même visage

    @FXML
    private void initialize() {
        refreshCaptcha();
        if (labelCaptchaStatus != null) labelCaptchaStatus.setText("");
    }

    private String getCurrentPassword() {
        if (tfmotDePasselogin.isVisible()) {
            return tfmotDePasselogin.getText().trim();
        } else {
            return tfmotDePasseloginVisible.getText().trim();
        }
    }

    @FXML
    private void loginaction(ActionEvent event) {
        String email = tfemaillogin.getText().trim();
        String password = getCurrentPassword();
        String captchaInput = tfCaptcha != null ? tfCaptcha.getText().trim().replace(" ", "") : "";

        if (labelApiStatus != null) labelApiStatus.setText("");
        if (labelCaptchaStatus != null) labelCaptchaStatus.setText("");

        // =================== CONTROLE DE SAISIE ===================
        if (email.isEmpty() || password.isEmpty()) {
            showAlert("⚠ Tous les champs sont obligatoires !");
            return;
        }

        if (currentCaptcha == null || currentCaptcha.isBlank()) {
            refreshCaptcha();
            showAlert("⚠ CAPTCHA non prêt. Réessayez.");
            return;
        }

        if (captchaInput.isEmpty()) {
            if (labelCaptchaStatus != null) labelCaptchaStatus.setText("Veuillez saisir le CAPTCHA.");
            return;
        }

        if (!captchaInput.equalsIgnoreCase(currentCaptcha)) {
            if (labelCaptchaStatus != null) labelCaptchaStatus.setText("CAPTCHA incorrect. Veuillez réessayer.");
            refreshCaptcha();
            return;
        }

        if (!email.contains("@")) {
            showAlert("⚠ Email invalide !");
            return;
        }

        if (password.length() < 6) {
            showAlert("⚠ Le mot de passe doit contenir au moins 6 caractères !");
            return;
        }

        // =================== API 1 : Validation email (module Auth) ===================
        if (labelApiStatus != null) labelApiStatus.setText("Vérification email...");
        AuthApiService.validateEmail(email).thenAccept(r -> Platform.runLater(() -> {
            if (labelApiStatus != null) labelApiStatus.setText(r.valid ? "✓ " + r.message : "⚠ " + r.message);
        }));

        // =================== API 2 : Mot de passe compromis (module Auth) ===================
        AuthApiService.checkPasswordBreached(password).thenAccept(r -> Platform.runLater(() -> {
            if (r.breached && labelApiStatus != null)
                labelApiStatus.setText(labelApiStatus.getText() + " | Mot de passe compromis (" + r.count + " fuites).");
        }));

        // =================== AUTHENTIFICATION ===================
        try {
            User user = serviceUser.authenticate(email, password);
            if (user == null) {
                failedLoginAttempts++;
                showAlert("⚠ Email ou mot de passe incorrect !");

                if (failedLoginAttempts >= 3) {
                    // Tenter de récupérer l'utilisateur par email (pour envoyer l'alerte)
                    User u = serviceUser.getOneByEmail(email);
                    if (u != null) {
                        SecurityAlertUtil.handleFailedLogin(u, failedLoginAttempts);
                    }
                    // On peut réinitialiser le compteur après l'alerte
                    failedLoginAttempts = 0;
                }
                return;
            }

            // Connexion réussie -> reset du compteur
            failedLoginAttempts = 0;
            handleSuccessfulAuthentication(user, event);
        } catch (Exception e) {
            logger.error("Erreur lors de la tentative de connexion", e);
            showAlert("⚠ Une erreur est survenue lors de la connexion !");
        }
    }

    @FXML
    private void refreshCaptcha() {
        currentCaptcha = generateCaptchaText(6);

        if (captchaImageView != null) {
            captchaImageView.setImage(renderCaptchaImage(currentCaptcha));
        }

        if (tfCaptcha != null) tfCaptcha.clear();
        if (labelCaptchaStatus != null) labelCaptchaStatus.setText("");
    }

    private String generateCaptchaText(int length) {
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    private Image renderCaptchaImage(String text) {
        double w = 130;
        double h = 40;

        Canvas canvas = new Canvas(w, h);
        GraphicsContext g = canvas.getGraphicsContext2D();

        // Background
        g.setFill(Color.web("#F3F4F6"));
        g.fillRoundRect(0, 0, w, h, 8, 8);
        g.setStroke(Color.web("#D1D5DB"));
        g.setLineWidth(1);
        g.strokeRoundRect(0.5, 0.5, w - 1, h - 1, 8, 8);

        // Noise lines
        g.setLineWidth(1);
        for (int i = 0; i < 7; i++) {
            g.setStroke(Color.rgb(17, 24, 39, 0.12));
            double x1 = random.nextDouble() * w;
            double y1 = random.nextDouble() * h;
            double x2 = random.nextDouble() * w;
            double y2 = random.nextDouble() * h;
            g.strokeLine(x1, y1, x2, y2);
        }

        // Text
        g.setFill(Color.web("#166534"));
        g.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        double x = 14;
        for (int i = 0; i < text.length(); i++) {
            double y = 26 + (random.nextDouble() * 6 - 3);
            g.fillText(String.valueOf(text.charAt(i)), x, y);
            x += 18;
        }

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, null);
    }

    @FXML
    private void togglePasswordVisibility() {
        if (tfmotDePasselogin.isVisible()) {
            // Passer en mode visible
            tfmotDePasseloginVisible.setText(tfmotDePasselogin.getText());
            tfmotDePasselogin.setVisible(false);
            tfmotDePasselogin.setManaged(false);

            tfmotDePasseloginVisible.setVisible(true);
            tfmotDePasseloginVisible.setManaged(true);

            // œil barré quand le mot de passe est visible
            btnTogglePassword.setText("🙈");
        } else {
            // Repasser en mode caché
            tfmotDePasselogin.setText(tfmotDePasseloginVisible.getText());
            tfmotDePasseloginVisible.setVisible(false);
            tfmotDePasseloginVisible.setManaged(false);

            tfmotDePasselogin.setVisible(true);
            tfmotDePasselogin.setManaged(true);

            // œil ouvert quand le mot de passe est caché
            btnTogglePassword.setText("👁");
        }
    }

    @FXML
    private void loginWithQr(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Sélectionnez votre code QR AgriGo");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = chooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            String text = QrCodeUtil.decodeFromFile(file);
            if (!QrCodeUtil.isUserPayload(text)) {
                showAlert("⚠ Ce QR code ne correspond pas à un compte AgriGo.");
                return;
            }

            String[] parts = text.split(":");
            if (parts.length < 3) {
                showAlert("⚠ QR code utilisateur invalide.");
                return;
            }

            int userId = Integer.parseInt(parts[1]);
            String email = parts[2];

            User user = serviceUser.getOneByEmail(email);
            if (user == null || user.getId_user() != userId) {
                showAlert("⚠ Compte introuvable pour ce QR code.");
                return;
            }

            handleSuccessfulAuthentication(user, event);

        } catch (Exception e) {
            logger.error("Erreur lors de la lecture du QR code", e);
            showAlert("⚠ Impossible de lire ce QR code.");
        }
    }

    private void handleSuccessfulAuthentication(User user, ActionEvent event) throws java.io.IOException {
        if (user == null) {
            showAlert("⚠ Email ou mot de passe incorrect !");
            return;
        }

        // Stocker l’utilisateur connecté dans la session
        Session.setCurrentUser(user);

        showAlert("✅ Connexion réussie ! Bienvenue " + user.getNom_user());

        // =================== LOAD DASHBOARD BASED ON ROLE ===================
        String fxmlFile;
        String title;

        if ("Admin".equalsIgnoreCase(user.getRole_user())) {
            fxmlFile = "/MainGuiAdmin.fxml";
            title = "Admin Dashboard";
        } else {
            fxmlFile = "/MainGui.fxml";
            title = "User Dashboard";
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent dashboardRoot = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(dashboardRoot));
        stage.setTitle(title);
        stage.show();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleLabelClick(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RegisterUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Inscription");
            stage.show();
        } catch (Exception e) {
            logger.error("Impossible de charger la page d'inscription", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erreur");
            alert.setContentText("Impossible de charger la page d'inscription.");
            alert.showAndWait();
        }
    }

    public void handleResetPassword(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ResetPassword.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Réinitialiser le mot de passe");
            stage.show();
        } catch (Exception e) {
            logger.error("Impossible de charger la page de reset", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Impossible de charger la page.");
            alert.showAndWait();
        }
    }

    @FXML
    public void loginWithFace(ActionEvent event) {
        // Capture une photo via webcam
        File tmp = null;
        try {
            String tmpDir = System.getProperty("java.io.tmpdir");
            tmp = new File(tmpDir, "face_login_" + System.currentTimeMillis() + ".png");
            WebcamUtil.captureToFile(tmp);
        } catch (IOException e) {
            logger.error("Impossible d'accéder à la webcam", e);
            showAlert("⚠ Impossible d'accéder à la webcam : " + e.getMessage());
            return;
        }

        try {
            // Première tentative : utiliser le modèle LBPH si disponible
            try {
                Services.FaceRecognitionService fr = new Services.FaceRecognitionService();
                Services.FaceRecognitionService.PredictionResult pr = fr.predictFromImagePath(tmp.getAbsolutePath());
                if (pr != null) {
                    // LBPH : valeur plus faible = meilleure correspondance
                    double threshold = 60.0; // seuil à calibrer
                    if (pr.confidence <= threshold) {
                        User u = serviceUser.getOneByID(pr.label);
                        if (u != null) {
                            // authentifier
                            try {
                                handleSuccessfulAuthentication(u, event);
                                return;
                            } catch (IOException ioe) {
                                logger.error("Erreur lors de l'ouverture du tableau de bord", ioe);
                                showAlert("⚠ Erreur lors de l'ouverture du tableau de bord : " + ioe.getMessage());
                                return;
                            }
                        }
                    }
                    // sinon on tombe en fallback
                }
            } catch (Exception e) {
                // Si erreur avec OpenCV/Bytedeco ou modèle manquant, on loggue et on continue en fallback
                logger.info("LBPH prediction échouée ou modèle absent, utilisation du fallback aHash: " + e.getMessage());
            }

            // Fallback : Parcourir tous les utilisateurs et comparer leur photo via FaceUtil (aHash)
            java.util.Set<User> users = serviceUser.getAll();
            User best = null;
            int bestDistance = Integer.MAX_VALUE;

            for (User u : users) {
                String photoPath = u.getPhotoPath();
                if (photoPath == null || photoPath.isBlank()) continue;
                File userPhoto;
                try {
                    userPhoto = FaceUtil.resolveToFile(photoPath);
                } catch (IOException ioe) {
                    // Impossible de lire/résoudre l'image du user -> ignorer cet utilisateur
                    continue;
                }
                if (userPhoto == null || !userPhoto.exists()) continue;
                try {
                    int dist = FaceUtil.compareImages(tmp, userPhoto);
                    if (dist < bestDistance) {
                        bestDistance = dist;
                        best = u;
                    }
                } catch (IOException ex) {
                    // ignorer ce user si erreur lecture image
                    logger.warn("Impossible de comparer les images pour user id {}: {}", u.getId_user(), ex.getMessage());
                }
            }

            if (best != null && bestDistance <= FACE_MATCH_THRESHOLD) {
                // authentifier
                try {
                    handleSuccessfulAuthentication(best, event);
                } catch (IOException ioe) {
                    logger.error("Erreur lors de l'ouverture du tableau de bord", ioe);
                    showAlert("⚠ Erreur lors de l'ouverture du tableau de bord : " + ioe.getMessage());
                }
            } else {
                showAlert("⚠ Aucun visage correspondant trouvé.");
            }
        } finally {
            if (tmp != null && tmp.exists()) {
                try { tmp.delete(); } catch (Exception ignored) {}
            }
        }
    }

}
