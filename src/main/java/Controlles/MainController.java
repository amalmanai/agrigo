package Controlles;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private javafx.scene.control.Button btnDashboard;

    @FXML
    private javafx.scene.control.Button btnHistorique;

    @FXML
    private javafx.scene.control.Button btnSystemes;

    @FXML
    private void initialize() {
        showDashboard();
    }

    @FXML
    public void showDashboard() {
        setActiveNav(btnDashboard);
        URL url = MainController.class.getResource("/dashboard.fxml");
        if (url == null) {
            System.err.println("Ressource introuvable: dashboard.fxml");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Node view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            DashboardController dashboardController = loader.getController();
            if (dashboardController != null) {
                dashboardController.setMainController(this);
            }
        } catch (IOException e) {
            System.err.println("Erreur chargement dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void showSystemeIrrigation() {
        setActiveNav(btnSystemes);
        loadView("vue_simple.fxml");
    }

    @FXML
    public void showHistoriqueIrrigation() {
        setActiveNav(btnHistorique);
        loadView("vue_avancee.fxml");
    }

    private void setActiveNav(javafx.scene.control.Button active) {
        btnDashboard.getStyleClass().setAll("nav-item");
        btnHistorique.getStyleClass().setAll("nav-item");
        btnSystemes.getStyleClass().setAll("nav-item");
        if (active != null) active.getStyleClass().add("active");
    }

    private void loadView(String fxmlPath) {
        URL url = MainController.class.getResource("/" + fxmlPath);
        if (url == null) {
            System.err.println("Ressource introuvable: " + fxmlPath);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Node view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            System.err.println("Erreur chargement vue " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
