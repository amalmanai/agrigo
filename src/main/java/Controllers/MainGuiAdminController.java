package Controllers;

import Entites.User;
import Utils.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuButton;
import javafx.stage.Stage;
import java.io.IOException;

public class MainGuiAdminController {

    @FXML
    private MenuButton btnProfil;
    @FXML
    private javafx.scene.layout.BorderPane rootPane;

    @FXML
    public void initialize() {
        if (Controlles.DashBoardController.preferredDarkMode && rootPane != null) {
            rootPane.getStyleClass().add("theme-dark");
        }
    }

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
                Scene scene = new Scene(root);
                applyDarkTheme(scene, root);
                stage.setScene(scene);
                stage.setTitle("Mes tâches");
                stage.show();
            } else {
                new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir Mes tâches.").showAndWait();
            }
        } catch (java.io.IOException e) {
            System.err.println("openMesTaches error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible de charger Mes tâches.").showAndWait();
        }
    }

    private Stage getStageForMenuAction() {
        if (btnProfil != null && btnProfil.getScene() != null && btnProfil.getScene().getWindow() instanceof Stage) {
            return (Stage) btnProfil.getScene().getWindow();
        }
        return null;
    }

    private void applyDarkTheme(Scene scene, Parent root) {
        try {
            var css = getClass().getResource("/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
        } catch (Exception ignored) {}
        if (Controlles.DashBoardController.preferredDarkMode && root != null) {
            root.getStyleClass().add("theme-dark");
        }
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
            Scene scene = new Scene(root);
            applyDarkTheme(scene, root);
            stage.setScene(scene);
            stage.setTitle("Mon profil");
            stage.showAndWait();
        } catch (java.io.IOException e) {
            System.err.println("openMonProfil error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible de charger Mon profil.").showAndWait();
        }
    }
    public void todashboard(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            applyDarkTheme(scene, root);
            stage.setScene(scene);
            stage.setTitle("Gestion des utilisateurs");
            stage.show();
        } catch (java.io.IOException e) {
            System.err.println("todashboard error: " + e.getMessage());
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
            System.err.println("BtnGestionTache error: " + e.getMessage());
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
            System.err.println("openStats error: " + e.getMessage());
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
            System.err.println("logout error: " + e.getMessage());
        }
    }

    /** Ouvre le mini‑jeu AGRI depuis l'interface admin */
    public void openAgriGame(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgriGame.fxml"));
            // Récupérer la stage depuis la fenêtre actuelle
            Stage stage = (Stage) btnProfil.getScene().getWindow();
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Jeu AGRI");
            stage.show();
        } catch (IOException e) {
            System.err.println("openAgriGame admin error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le jeu AGRI.").showAndWait();
        }
    }

    /** Ouvre le jeu AgriPuzzle depuis l'interface admin */
    public void openAgriPuzzle(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgriPuzzle.fxml"));
            Parent root = loader.load();
            // Récupérer la stage depuis la fenêtre actuelle
            Stage stage = (Stage) btnProfil.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Agri Puzzle");
            stage.show();
        } catch (IOException e) {
            System.err.println("openAgriPuzzle admin error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le jeu AgriPuzzle.").showAndWait();
        }
    }

    /** Ouvre le chatbot agricole OpenAI depuis l'interface admin */
    public void openAgriChatbot(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgriChatbot.fxml"));
            Parent root = loader.load();
            // Récupérer la stage depuis la fenêtre actuelle
            Stage stage = (Stage) btnProfil.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Assistant Agricole AgriGo");
            stage.show();
        } catch (IOException e) {
            System.err.println("openAgriChatbot admin error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir l'assistant agricole.").showAndWait();
        }
    }

    /** Retour à l'écran précédent */
    public void retour(ActionEvent actionEvent) {
        try {
            String fxml = (Utils.Session.getCurrentUser() != null
                    && "admin".equalsIgnoreCase(Utils.Session.getCurrentUser().getRole_user()))
                    ? "/Dashboard.fxml"
                    : "/menu.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            try {
                java.net.URL css = getClass().getResource("/app.css");
                if (css != null) scene.getStylesheets().add(css.toExternalForm());
            } catch (Exception ignored) {}
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            System.err.println("retour error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible de revenir à l'écran précédent.").showAndWait();
        }
    }

}
