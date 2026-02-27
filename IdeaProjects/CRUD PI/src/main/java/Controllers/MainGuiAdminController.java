package Controllers;

import Entites.User;
import Utils.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuButton;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainGuiAdminController {

    @FXML
    private MenuButton btnProfil;

    /** Ouvre "Mes tâches" (tâches du user connecté uniquement). Utilise btnProfil car le clic vient d'un MenuItem. */
    public void openMesTaches(ActionEvent actionEvent) {
        User current = Session.getCurrentUser();
        if (current == null) {
            new Alert(Alert.AlertType.WARNING, "Session expirée. Reconnectez-vous.").showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Tache/ListTache.fxml"));
            Parent root = loader.load();
            ListeTacheController ctrl = loader.getController();
            ctrl.setFilterByCurrentUser(true);
            Stage stage = getStageForMenuAction();
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("Mes tâches");
                stage.show();
            } else {
                new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir Mes tâches.").showAndWait();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger Mes tâches.").showAndWait();
        }
    }

    private Stage getStageForMenuAction() {
        if (btnProfil != null && btnProfil.getScene() != null && btnProfil.getScene().getWindow() instanceof Stage) {
            return (Stage) btnProfil.getScene().getWindow();
        }
        return null;
    }

    /** Ouvre "Mon profil" pour modifier le compte du user connecté. */
    public void openMonProfil(ActionEvent actionEvent) {
        User current = Session.getCurrentUser();
        if (current == null) {
            new Alert(Alert.AlertType.WARNING, "Session expirée. Reconnectez-vous.").showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierUser.fxml"));
            Parent root = loader.load();
            ModifierUserController ctrl = loader.getController();
            ctrl.setUser(current);
            ctrl.setSelfEdit(true);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon profil");
            stage.showAndWait();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger Mon profil.").showAndWait();
        }
    }
    public void todashboard(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des utilisateurs");
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger la page.").showAndWait();
        }
    }

    public void BtnGestionTache(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Tache/ListTache.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des tâches");
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger la page.").showAndWait();
        }
    }

    public void openStats(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StatsDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Statistiques");
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger les statistiques.").showAndWait();
        }
    }

    public void logout(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /** Ouvre la fenêtre du chatbot service client AGRIGO. */
    public void openChatbot(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Chatbot.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Assistant AGRIGO");
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible d’ouvrir le chatbot AGRIGO.").showAndWait();
        }
    }
}
