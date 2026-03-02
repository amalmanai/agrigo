package Controllers;

import Entites.User;
import Utils.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.io.IOException;

public class MainGuiController {

    @FXML
    private Button totache;

    @FXML
    private MenuButton btnProfil;

    @FXML
    private Button logoutuser;

    // ----------------------------
    // ACTIONS MENU / SIDEBAR
    // ----------------------------

    @FXML
    public void totache(ActionEvent actionEvent) {
        loadFXMLInCurrentStage("/Tache/ListTache.fxml", "Gestion des tâches");
    }

    @FXML
    public void openMesTaches(ActionEvent actionEvent) {
        User current = Session.getCurrentUser();
        if (current == null) {
            new Alert(Alert.AlertType.WARNING, "Session expirée. Reconnectez-vous.").showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Tache/ListTache.fxml"));
            Parent root = loader.load();

            // Filtrer les tâches pour le user courant
            ListeTacheController ctrl = loader.getController();
            ctrl.setFilterByCurrentUser(true);

            Stage stage = getCurrentStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("Mes tâches");
                stage.show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger Mes tâches.").showAndWait();
        }
    }

    @FXML
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

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger Mon profil.").showAndWait();
        }
    }

    @FXML
    public void openAgriGame(ActionEvent actionEvent) {
        loadFXMLInCurrentStage("/AgriGame.fxml", "Jeu Agricole");
    }

    @FXML
    public void openAgriPuzzle(ActionEvent actionEvent) {
        loadFXMLInCurrentStage("/AgriPuzzle.fxml", "Puzzle Agricole");
    }

    @FXML
    public void openAgriChatbot(ActionEvent actionEvent) {
        loadFXMLInCurrentStage("/AgriChatbot.fxml", "Assistant Agricole AgriGo");
    }

    @FXML
    public void logoutuser(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();

            // Fermer la fenêtre principale
            Stage currentStage = getCurrentStage();
            if (currentStage != null) {
                currentStage.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger la page de connexion.").showAndWait();
        }
    }

    // ----------------------------
    // UTILITAIRES
    // ----------------------------

    /** Récupère la stage actuelle via btnProfil (MenuButton) */
    private Stage getCurrentStage() {
        if (btnProfil != null && btnProfil.getScene() != null && btnProfil.getScene().getWindow() instanceof Stage) {
            return (Stage) btnProfil.getScene().getWindow();
        }
        if (totache != null && totache.getScene() != null && totache.getScene().getWindow() instanceof Stage) {
            return (Stage) totache.getScene().getWindow();
        }
        if (logoutuser != null && logoutuser.getScene() != null && logoutuser.getScene().getWindow() instanceof Stage) {
            return (Stage) logoutuser.getScene().getWindow();
        }
        return null;
    }

    /** Charge un FXML dans la stage actuelle */
    private void loadFXMLInCurrentStage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = getCurrentStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle(title);
                stage.show();
            } else {
                new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir la page " + title).showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger la page " + title).showAndWait();
        }
    }
}