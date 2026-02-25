package Controllers;

import Services.ServiceUser;
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
import javafx.stage.Stage;

import java.io.IOException;

public class ResetPasswordController {

    @FXML private TextField tfEmail;
    @FXML private PasswordField pfNewPassword;
    @FXML private PasswordField pfConfirmPassword;
    @FXML private Label labelMessage;

    private final ServiceUser serviceUser = new ServiceUser();

    @FXML
    void resetAction(ActionEvent event) {
        if (labelMessage != null) labelMessage.setText("");

        String email = tfEmail.getText().trim();
        String newPass = pfNewPassword.getText();
        String confirm = pfConfirmPassword.getText();

        if (email.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            showAlert("⚠ Tous les champs sont obligatoires.");
            return;
        }
        if (!email.contains("@")) {
            showAlert("⚠ Email invalide.");
            return;
        }
        if (newPass.length() < 6) {
            showAlert("⚠ Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }
        if (!newPass.equals(confirm)) {
            showAlert("⚠ Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            boolean updated = serviceUser.updatePasswordByEmail(email, newPass);
            if (updated) {
                showAlert("✅ Mot de passe réinitialisé. Vous pouvez vous connecter.");
                backToLogin(event);
            } else {
                showAlert("⚠ Aucun compte associé à cet email.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("⚠ Erreur lors de la réinitialisation.");
        }
    }

    @FXML
    void backToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger la page de connexion.").showAndWait();
        }
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}
