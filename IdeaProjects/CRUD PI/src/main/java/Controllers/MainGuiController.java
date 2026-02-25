package Controllers;

import Entites.User;
import Utils.Session;
import io.jsonwebtoken.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuButton;
import javafx.stage.Stage;

public class MainGuiController {

    @FXML
    private MenuButton btnProfil;

    /** Ouvre "Mes tâches" (tâches du user connecté uniquement). Utilise btnProfil car le clic vient d'un MenuItem (pas un Node). */
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

    /** Ouvre "Mon profil" pour modifier le compte du user connecté */
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

    /** Récupère la Stage principale (via le MenuButton de la scène) quand l'action vient d'un MenuItem. */
    private Stage getStageForMenuAction() {
        if (btnProfil != null && btnProfil.getScene() != null && btnProfil.getScene().getWindow() instanceof Stage) {
            return (Stage) btnProfil.getScene().getWindow();
        }
        return null;
    }
    /** Ouvre la gestion des tâches : le user non-admin ne voit que ses tâches. */
    public void totache(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Tache/ListTache.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des tâches");
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger la page Gestion des tâches.").showAndWait();
        }
    }

    public void logoutuser(ActionEvent actionEvent) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("");
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}
