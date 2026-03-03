package Controllers;

import Entites.User;
import Services.ServiceUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.regex.Pattern;

public class AjouterUserController {

    @FXML
    private TextField champNom;
    @FXML
    private TextField champPrenom;
    @FXML
    private TextField champEmail;
    @FXML
    private TextField champNumero;
    @FXML
    private PasswordField champPassword;
    @FXML
    private TextField champAdresse;
    @FXML
    private ComboBox<String> comboRole;

    private final ServiceUser serviceUser = new ServiceUser();

    @FXML
    public void initialize() {
        comboRole.getSelectionModel().selectFirst();
    }

    @FXML
    public void creerUtilisateur(ActionEvent event) {
        String nom = champNom.getText().trim();
        String prenom = champPrenom.getText().trim();
        String email = champEmail.getText().trim();
        String phoneStr = champNumero.getText().trim();
        String pwd = champPassword.getText();
        String adr = champAdresse.getText().trim();
        String role = comboRole.getValue();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || phoneStr.isEmpty() || pwd.isEmpty()
                || role == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Email Invalide", "Veuillez saisir une adresse email valide.");
            return;
        }

        int num;
        try {
            num = Integer.parseInt(phoneStr);
            if (phoneStr.length() != 8) {
                showAlert(Alert.AlertType.ERROR, "Numéro Invalide",
                        "Le numéro de téléphone doit contenir exactement 8 chiffres.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Numéro Invalide",
                    "Le numéro de téléphone ne doit contenir que des chiffres.");
            return;
        }

        try {
            User newUser = new User(nom, prenom, email, role, num, pwd, adr);
            serviceUser.ajouter(newUser);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'utilisateur a été ajouté avec succès.");
            fermerFenetre();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter l'utilisateur: " + e.getMessage());
        }
    }

    @FXML
    public void annulerAction(ActionEvent event) {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) champNom.getScene().getWindow();
        stage.close();
    }

    private boolean isValidEmail(String email) {
        if (email == null)
            return false;
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.compile(regex).matcher(email).matches();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
