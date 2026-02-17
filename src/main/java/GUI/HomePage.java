package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HomePage extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/menu.fxml"));
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            primaryStage.setTitle("AgriGo - Gestion des récoltes");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement du menu : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyStylesheet(Scene scene) {
        var css = getClass().getResource("/app.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }
}
