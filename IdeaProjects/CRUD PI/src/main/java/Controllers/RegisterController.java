package Controllers;

import Entites.User;
import Services.ServiceUser;
import Utils.QrCodeUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class RegisterController implements Initializable {

    @FXML private TextField tfNom;
    @FXML private TextField tfprenom;
    @FXML private TextField tfemail;
    @FXML private Label labelEmailApi;
    @FXML private PasswordField tfmotDePasse;
    @FXML private PasswordField pfConfirmMotDePasse;
    @FXML private Label labelPasswordApi;
    @FXML private TextField tfTelephone;
    @FXML private TextField tfAddresse;
    @FXML private ComboBox<String> tfroleeee;

    ServiceUser serviceUser = new ServiceUser();

    // Regex email fiable
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\d+");

    @FXML
    void registerAction(ActionEvent event) {
        try {

            String nom = tfNom.getText().trim();
            String prenom = tfprenom.getText().trim();
            String email = tfemail.getText().trim().toLowerCase();
            String password = tfmotDePasse.getText();
            String confirmPassword = pfConfirmMotDePasse.getText();
            String tel = tfTelephone.getText().trim();
            String adresse = tfAddresse.getText().trim();
            String role = tfroleeee.getValue();

            // ================= CHAMPS VIDES =================
            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() ||
                    password.isEmpty() || confirmPassword.isEmpty() ||
                    tel.isEmpty() || adresse.isEmpty() || role == null) {

                alert("⚠ Tous les champs sont obligatoires !");
                return;
            }

            // ================= EMAIL =================
            if (!EMAIL_PATTERN.matcher(email).matches() ||
                    !email.contains("@") || !email.contains(".")) {

                alert("⚠ Email invalide !");
                return;
            }

            // ================= TELEPHONE =================
            String telDigits = tel.replaceAll("\\D", "");
            if (!PHONE_PATTERN.matcher(telDigits).matches()) {
                alert("⚠ Numéro de téléphone invalide !");
                return;
            }

            int numTel;
            try {
                numTel = Integer.parseInt(telDigits);
            } catch (Exception e) {
                alert("⚠ Numéro trop long !");
                return;
            }

            // ================= PASSWORD =================
            if (!password.equals(confirmPassword)) {
                alert("⚠ Les mots de passe ne correspondent pas !");
                return;
            }

            if (password.length() < 6) {
                alert("⚠ Mot de passe minimum 6 caractères !");
                return;
            }

            // ================= EMAIL EXISTE =================
            if (serviceUser.getOneByEmail(email) != null) {
                alert("⚠ Cet email est déjà utilisé !");
                return;
            }

            // ================= CREATION USER =================
            User u = new User(
                    nom,
                    prenom,
                    email,
                    role,
                    numTel,
                    password,
                    adresse
            );

            serviceUser.ajouter(u);

            User created = serviceUser.getOneByEmail(email);

            if (created != null) {
                try {
                    String qrPath = QrCodeUtil.generateUserQrPng(created);
                    alert("✅ Inscription réussie !\nQR Code généré:\n" + qrPath);
                } catch (Exception e) {
                    alert("Inscription réussie mais QR code non généré.");
                }

                // Proposer à l'utilisateur d'ajouter une photo maintenant
                ButtonType takePhoto = new ButtonType("Prendre une photo");
                ButtonType pickFile = new ButtonType("Choisir un fichier");
                ButtonType later = new ButtonType("Plus tard", ButtonBar.ButtonData.CANCEL_CLOSE);
                Alert ask = new Alert(Alert.AlertType.CONFIRMATION);
                ask.setTitle("Ajouter une photo de profil");
                ask.setHeaderText("Souhaitez-vous ajouter une photo maintenant ?");
                ask.getButtonTypes().setAll(takePhoto, pickFile, later);

                Optional<ButtonType> choice = ask.showAndWait();
                if (choice.isPresent() && choice.get() == takePhoto) {
                    // Capture via webcam
                    try {
                        String tmpDir = System.getProperty("java.io.tmpdir");
                        File tmp = new File(tmpDir, "register_face_" + System.currentTimeMillis() + ".png");
                        boolean ok = Utils.WebcamUtil.captureToFile(tmp);
                        if (ok && tmp.exists()) {
                            saveUserPhoto(tmp, created.getId_user());
                        } else {
                            alert("Impossible de capturer la photo depuis la webcam.");
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        alert("Erreur lors de la capture webcam : " + ioe.getMessage());
                    }
                } else if (choice.isPresent() && choice.get() == pickFile) {
                    // Ouvrir FileChooser
                    try {
                        FileChooser chooser = new FileChooser();
                        chooser.setTitle("Choisir une photo de profil");
                        chooser.getExtensionFilters().addAll(
                                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
                        );
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        File sel = chooser.showOpenDialog(stage);
                        if (sel != null) {
                            saveUserPhoto(sel, created.getId_user());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        alert("Erreur lors de la sélection du fichier : " + ex.getMessage());
                    }
                }

                // Lancer l'entraînement du modèle en arrière-plan
                new Thread(() -> {
                    try {
                        Services.FaceRecognitionService fr = new Services.FaceRecognitionService();
                        boolean trained = fr.trainFromDisk("user-photos");
                        Platform.runLater(() -> {
                            if (trained) alert("Modèle de reconnaissance entraîné.");
                            else alert("Aucun image détectée pour l'entraînement ou erreur.");
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> alert("Erreur pendant l'entraînement du modèle: " + e.getMessage()));
                    }
                }).start();
            }

            clear();

            // ================= REDIRECTION LOGIN =================
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            alert("Erreur : " + e.getMessage());
        }
    }

    private void saveUserPhoto(File src, int userId) throws IOException {
        if (src == null || !src.exists()) throw new IOException("Fichier source invalide");
        File userDir = new File("user-photos" + File.separator + userId);
        if (!userDir.exists()) userDir.mkdirs();
        File dest = new File(userDir, "photo1.png");
        Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Mettre à jour le chemin dans la base (chemin relatif)
        User u = serviceUser.getOneByID(userId);
        if (u != null) {
            u.setPhotoPath(dest.getAbsolutePath());
            serviceUser.modifier(u);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        ObservableList<String> roles = FXCollections.observableArrayList(
                "ouvrier agricole",
                "agriculteur client",
                "admin"
        );
        tfroleeee.setItems(roles);

        // Validation email en temps réel
        tfemail.textProperty().addListener((obs, oldVal, newVal) -> {
            if (EMAIL_PATTERN.matcher(newVal).matches()) {
                labelEmailApi.setText("Email valide");
                labelEmailApi.setStyle("-fx-text-fill: green;");
            } else {
                labelEmailApi.setText("Email invalide");
                labelEmailApi.setStyle("-fx-text-fill: red;");
            }
        });
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Information");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void clear() {
        tfNom.clear();
        tfprenom.clear();
        tfemail.clear();
        tfmotDePasse.clear();
        pfConfirmMotDePasse.clear();
        tfTelephone.clear();
        tfAddresse.clear();
        tfroleeee.getSelectionModel().clearSelection();
    }

    @FXML
    public void takePictureAction(ActionEvent actionEvent) {
    }

    @FXML
    public void tfroleeee(ActionEvent actionEvent) {
    }

    @FXML
    public void pickImageAction(ActionEvent actionEvent) {
    }

    @FXML
    public void handleLabelClick(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            alert("Impossible de charger la page login.");
        }
    }

    @FXML
    public void openMapPicker(ActionEvent actionEvent) {
        try {
            Stage owner = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Stage stage = new Stage();
            stage.initOwner(owner);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Choisir une adresse");

            WebView webView = new WebView();
            WebEngine engine = webView.getEngine();

            Scene scene = new Scene(webView, 900, 600);
            stage.setScene(scene);

            URL mapUrl = getClass().getResource("/map/map.html");

            if (mapUrl == null) {
                alert("Carte introuvable.");
                return;
            }

            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("app", new JsBridge(stage));
                }
            });

            engine.load(mapUrl.toExternalForm());
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            alert("Erreur carte.");
        }
    }

    public class JsBridge {
        private final Stage stage;

        public JsBridge(Stage stage) {
            this.stage = stage;
        }

        public void onLocationSelected(double lat, double lng, String address) {
            Platform.runLater(() -> tfAddresse.setText(address));
        }

        public void closePicker() {
            Platform.runLater(stage::close);
        }
    }
}

