package Controllers;

import Services.ServiceUser;
import Utils.EmailUtil;
import Utils.WebcamUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import jakarta.mail.MessagingException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class ResetPasswordController implements Initializable {

    @FXML private RadioButton rbEmail;
    @FXML private RadioButton rbSMS;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private TextField tfCode;
    @FXML private PasswordField pfNewPassword;
    @FXML private PasswordField pfConfirmPassword;
    @FXML private Label labelMessage;
    @FXML private Button btnSendCode;
    @FXML private Button btnVerifyCode;
    @FXML private Button btnReset;

    private final ServiceUser serviceUser = new ServiceUser();

    // stockage temporaire du code et état
    private String verificationCode = null;
    private boolean verified = false;

    // compteur d'échecs de vérification
    private int failedVerifyAttempts = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ToggleGroup tg = new ToggleGroup();
        rbEmail.setToggleGroup(tg);
        rbSMS.setToggleGroup(tg);
        rbEmail.setSelected(true);

        // initial states
        tfEmail.setDisable(false);
        tfPhone.setDisable(true);
        btnReset.setDisable(true);
        btnVerifyCode.setDisable(true); // disabled until a code is sent

        // listener pour basculer les champs
        rbEmail.setOnAction(e -> {
            tfEmail.setDisable(false);
            tfPhone.setDisable(true);
            labelMessage.setText("");
        });
        rbSMS.setOnAction(e -> {
            tfEmail.setDisable(true);
            tfPhone.setDisable(false);
            labelMessage.setText("");
        });
    }

    @FXML
    void sendCodeAction(ActionEvent event) {
        labelMessage.setText("");
        verificationCode = null;
        verified = false;
        btnReset.setDisable(true);
        failedVerifyAttempts = 0; // reset attempts on new code

        if (rbEmail.isSelected()) {
            String email = tfEmail.getText().trim();
            if (email.isEmpty() || !email.contains("@")) {
                showAlert("⚠ Email invalide.");
                return;
            }
            if (serviceUser.getOneByEmail(email) == null) {
                showAlert("⚠ Aucun compte associé à cet email.");
                return;
            }
            // générer le code
            verificationCode = generateCode();
            // TODO: appeler une vraie méthode d'envoi d'email
            System.out.println("Code envoyé par email à " + email + " : " + verificationCode);
            showAlert("Code de vérification envoyé par email. (En dev, code affiché dans la console)");
        } else {
            String phone = tfPhone.getText().trim();
            if (phone.isEmpty()) {
                showAlert("⚠ Numéro de téléphone requis.");
                return;
            }
            if (serviceUser.getOneByPhone(phone) == null) {
                showAlert("⚠ Aucun compte associé à ce numéro.");
                return;
            }
            verificationCode = generateCode();
            // TODO: appeler un service SMS réel
            System.out.println("Code envoyé par SMS à " + phone + " : " + verificationCode);
            showAlert("Code de vérification envoyé par SMS. (En dev, code affiché dans la console)");
        }
        tfCode.setText("");
        btnVerifyCode.setDisable(false); // enable verify button after sending
    }

    @FXML
    void verifyCodeAction(ActionEvent event) {
        String entered = tfCode.getText().trim();
        if (verificationCode == null) {
            showAlert("⚠ Veuillez d'abord demander un code.");
            return;
        }
        if (entered.isEmpty()) {
            showAlert("⚠ Entrez le code de vérification.");
            return;
        }
        if (entered.equals(verificationCode)) {
            verified = true;
            btnReset.setDisable(false);
            labelMessage.setText("✅ Code vérifié. Vous pouvez maintenant réinitialiser le mot de passe.");
            // lock further code operations
            btnVerifyCode.setDisable(true);
            btnSendCode.setDisable(true);
            failedVerifyAttempts = 0;
        } else {
            failedVerifyAttempts++;
            showAlert("⚠ Code invalide. Tentative " + failedVerifyAttempts + " sur 3.");
            labelMessage.setText("⚠ Code invalide. Tentative " + failedVerifyAttempts + " sur 3.");

            if (failedVerifyAttempts >= 3) {
                // capture et envoi de l'alerte
                handleSecurityAlert();
                // reset compteur et empêcher nouvelles vérifications jusqu'à nouvel envoi
                btnVerifyCode.setDisable(true);
                btnSendCode.setDisable(true);
            }
        }
    }

    private void handleSecurityAlert() {
        // déterminer l'email destinataire : si méthode email on l'utilise, sinon on cherche l'email associé au téléphone
        String recipient = null;
        String contactInfo = null;
        if (rbEmail.isSelected()) {
            recipient = tfEmail.getText().trim();
            contactInfo = recipient;
        } else {
            String phone = tfPhone.getText().trim();
            contactInfo = phone;
            var user = serviceUser.getOneByPhone(phone);
            if (user != null) recipient = user.getEmail_user();
        }

        if (recipient == null || recipient.isEmpty()) {
            // si aucun email trouvé, on informe localement
            showAlert("⚠ 3 tentatives échouées — impossible d'envoyer l'alerte (email destinataire introuvable).");
            return;
        }

        File tmp = null;
        try {
            String tmpDir = System.getProperty("java.io.tmpdir");
            tmp = new File(tmpDir, "intruder_" + System.currentTimeMillis() + ".png");
            WebcamUtil.captureToFile(tmp);
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("⚠ 3 tentatives échouées — la capture webcam a échoué : " + ex.getMessage());
            return;
        }

        // préparer et envoyer l'email
        String subject = "Alerte sécurité : tentatives de réinitialisation";
        String body = "Nous avons détecté 3 tentatives de réinitialisation pour le compte lié à : " + contactInfo + "\n" +
                "La photo capturée par la webcam est jointe à ce message.";
        try {
            EmailUtil.sendEmailWithAttachment(recipient, subject, body, tmp);
            showAlert("⚠ 3 tentatives échouées — alerte envoyée à " + recipient);
        } catch (MessagingException mex) {
            mex.printStackTrace();
            showAlert("⚠ Échec lors de l'envoi de l'alerte par email : " + mex.getMessage());
        } finally {
            if (tmp != null && tmp.exists()) {
                try { tmp.delete(); } catch (Exception ignored) {}
            }
        }
    }

    @FXML
    void resetAction(ActionEvent event) {
        if (labelMessage != null) labelMessage.setText("");

        String newPass = pfNewPassword.getText();
        String confirm = pfConfirmPassword.getText();

        if (!verified) {
            showAlert("⚠ Veuillez vérifier le code avant de réinitialiser.");
            return;
        }

        if (newPass.isEmpty() || confirm.isEmpty()) {
            showAlert("⚠ Tous les champs de mot de passe sont obligatoires.");
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
            boolean updated = false;
            if (rbEmail.isSelected()) {
                String email = tfEmail.getText().trim();
                updated = serviceUser.updatePasswordByEmail(email, newPass);
            } else {
                String phone = tfPhone.getText().trim();
                updated = serviceUser.updatePasswordByPhone(phone, newPass);
            }
            if (updated) {
                showAlert("✅ Mot de passe réinitialisé. Vous pouvez vous connecter.");
                backToLogin(event);
            } else {
                showAlert("⚠ Échec de la réinitialisation : compte introuvable.");
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

    private String generateCode() {
        Random r = new Random();
        int code = 100000 + r.nextInt(900000);
        return String.valueOf(code);
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}