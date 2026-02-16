package org.example.GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomePage extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Démarrage de l'application...");

            // Vérifier si le fichier FXML existe
            var fxmlUrl = getClass().getResource("/menu.fxml");
            System.out.println("URL du FXML: " + fxmlUrl);

            if (fxmlUrl == null) {
                System.err.println("ERREUR: menu.fxml introuvable!");
                showError("Fichier menu.fxml non trouvé!");
                return;
            }

            // Charger le FXML
            Parent root = FXMLLoader.load(fxmlUrl);
            System.out.println("FXML chargé avec succès!");

            // Créer la scène
            Scene scene = new Scene(root, 1100, 820);

            // Charger le CSS
            applyStylesheet(scene);

            // Configurer la fenêtre
            primaryStage.setTitle("AgriGo - Gestion Agricole");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            System.out.println("Application démarrée avec succès!");

        } catch (Exception e) {
            System.err.println("Erreur de chargement : " + e.getMessage());
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }

    private void applyStylesheet(Scene scene) {
        try {
            var cssUrl = getClass().getResource("/app.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS chargé: " + cssUrl);
            } else {
                System.out.println("CSS non trouvé (optionnel)");
            }
        } catch (Exception e) {
            System.out.println("Erreur CSS: " + e.getMessage());
        }
    }

    private void showError(String message) {
        System.err.println(message);
    }
}