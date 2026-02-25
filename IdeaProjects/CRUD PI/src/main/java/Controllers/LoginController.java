package Controllers;

import Api.AuthApiService;
import Entites.User;
import Services.ServiceUser;
import Utils.Session;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class LoginController {

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

    private final ServiceUser serviceUser = new ServiceUser();

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

        if (labelApiStatus != null) labelApiStatus.setText("");

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
            e.printStackTrace();
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
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Impossible de charger la page.");
            alert.showAndWait();
        }
    }
}
