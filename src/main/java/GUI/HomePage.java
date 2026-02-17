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
            var css = getClass().getResource("/app.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            primaryStage.setTitle("AgriGo - Irrigation Management Suite");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1100);
            primaryStage.setMinHeight(820);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Erreur de chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
