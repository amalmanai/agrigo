package Controllers;

import Entites.User;
import Services.ServiceUser;
import io.jsonwebtoken.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class RegisterController implements Initializable {

    @FXML private TextField tfNom;
    @FXML private TextField tfprenom;
    @FXML private TextField tfemail;
    @FXML private PasswordField tfmotDePasse;
    @FXML private PasswordField pfConfirmMotDePasse;
    @FXML private TextField tfTelephone;
    @FXML private TextField tfAddresse;
    @FXML private ComboBox<String> tfroleeee;

    ServiceUser serviceUser = new ServiceUser();

    // Pattern pour email simple
    private static final Pattern EMAIL_PATTERN = Pattern.compile(".+@.+\\..+");

    // Pattern pour téléphone uniquement chiffres
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d+");

    @FXML
    void registerAction(ActionEvent event) {
        try {
            // =================== CHAMPS OBLIGATOIRES ===================
            if (tfNom.getText().isEmpty() ||
                    tfprenom.getText().isEmpty() ||
                    tfemail.getText().isEmpty() ||
                    tfmotDePasse.getText().isEmpty() ||
                    pfConfirmMotDePasse.getText().isEmpty() ||
                    tfTelephone.getText().isEmpty() ||
                    tfAddresse.getText().isEmpty() ||
                    tfroleeee.getValue() == null) {

                alert("⚠ Tous les champs sont obligatoires !");
                return;
            }

            // =================== EMAIL VALIDATION ===================
            String email = tfemail.getText().trim();
            if (!email.contains("@")) {
                alert("⚠ Email invalide !");
                return;
            }

            // =================== TELEPHONE VALIDATION ===================
            String tel = tfTelephone.getText().trim();
            if (!tel.matches("\\d+")) {
                alert("⚠ Le numéro de téléphone doit contenir uniquement des chiffres !");
                return;
            }
            int numTel = Integer.parseInt(tel);

            // =================== PASSWORD VALIDATION ===================
            String password = tfmotDePasse.getText();
            String confirmPassword = pfConfirmMotDePasse.getText();
            if (!password.equals(confirmPassword)) {
                alert("⚠ Les mots de passe ne correspondent pas !");
                return;
            }
            if (password.length() < 6) {
                alert("⚠ Le mot de passe doit contenir au moins 6 caractères !");
                return;
            }

            // =================== CREATION USER ===================
            User u = new User(
                    tfNom.getText().trim(),
                    tfprenom.getText().trim(),
                    email,
                    tfroleeee.getValue(),
                    numTel,
                    password,
                    tfAddresse.getText().trim()
            );

            serviceUser.ajouter(u);

            alert("✅ Inscription réussie !");
            clear();

            // =================== REDIRECT BASED ON ROLE ===================
            String fxmlFile;
            String title;

            if ("Admin".equalsIgnoreCase(u.getRole_user())) {
                fxmlFile = "/LoginUser.fxml";
                title = "";
            } else {
                fxmlFile = "/LoginUser.fxml";
                title = "";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (NumberFormatException e) {
            alert("⚠ Numéro de téléphone invalide !");
        } catch (IOException e) {
            alert("⚠ Impossible de charger la page !");
            e.printStackTrace();
        } catch (Exception e) {
            alert("⚠ Une erreur est survenue !");
            e.printStackTrace();
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

    public void takePictureAction(ActionEvent actionEvent) {
    }

    public void tfroleeee(ActionEvent actionEvent) {
    }

    public void pickImageAction(ActionEvent actionEvent) {
    }

    public void handleLabelClick(ActionEvent actionEvent) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            // Set the new scene
            stage.setScene(new Scene(root));
            stage.setTitle("Register User"); // Optional: set title
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erreur");
            alert.setContentText("Impossible de charger la page RegisterUser.fxml");
            alert.showAndWait();
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}
