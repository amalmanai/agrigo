package Controlles;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import java.io.IOException;

public class MenuController {
    @FXML private Button btnCultures;
    @FXML private Button btnParcelles;

    @FXML
    void goToCultures(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/page1.fxml"));
        btnCultures.getScene().setRoot(root);
    }

    @FXML
    void goToParcelles(ActionEvent event) throws IOException {
        // You will need to create pageParcelle.fxml later
        Parent root = FXMLLoader.load(getClass().getResource("/pageParcelle.fxml"));
        btnParcelles.getScene().setRoot(root);
    }
}