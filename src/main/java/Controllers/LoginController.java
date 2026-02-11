package Controllers;

import Entites.User;
import Services.ServiceUser;
import Utils.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField tfemaillogin;

    @FXML
    private PasswordField tfmotDePasselogin;

    private final ServiceUser serviceUser = new ServiceUser();

    @FXML
    private void loginaction(ActionEvent event) {
        String email = tfemaillogin.getText().trim();
        String password = tfmotDePasselogin.getText().trim();

        // =================== CONTROLE DE SAISIE ===================
        if (email.isEmpty() || password.isEmpty()) {
            showAlert("⚠ Tous les champs sont obligatoires !");
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

        // =================== AUTHENTIFICATION ===================
        try {
            User user = serviceUser.authenticate(email, password);

            if (user != null) {
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

            } else {
                showAlert("⚠ Email ou mot de passe incorrect !");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("⚠ Une erreur est survenue lors de la connexion !");
        }
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
            stage.setTitle("Register User");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erreur");
            alert.setContentText("Impossible de charger la page RegisterUser.fxml");
            alert.showAndWait();
        }
    }

    public void pickImageAction(ActionEvent actionEvent) {
    }

    public void takePictureAction(ActionEvent actionEvent) {
    }
}
