package Controlles;

import Entites.HistoriqueIrrigation;
import Entites.SystemeIrrigation;
import Services.HistoriqueIrrigationCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DetailsController {

    @FXML
    private Label lblSysteme;

    @FXML
    private TableView<HistoriqueIrrigation> tvHistorique;

    @FXML
    private TableColumn<HistoriqueIrrigation, Timestamp> colDate;

    @FXML
    private TableColumn<HistoriqueIrrigation, Integer> colDuree;

    @FXML
    private TableColumn<HistoriqueIrrigation, BigDecimal> colVolume;

    @FXML
    private TableColumn<HistoriqueIrrigation, BigDecimal> colHumidite;

    @FXML
    private TableColumn<HistoriqueIrrigation, String> colType;

    @FXML
    private Button btnRetour;

    private final HistoriqueIrrigationCRUD service = new HistoriqueIrrigationCRUD();
    private final ObservableList<HistoriqueIrrigation> data = FXCollections.observableArrayList();

    private SystemeIrrigation systeme;

    @FXML
    private void initialize() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateIrrigation"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeMinutes"));
        colVolume.setCellValueFactory(new PropertyValueFactory<>("volumeEau"));
        colHumidite.setCellValueFactory(new PropertyValueFactory<>("humiditeAvant"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeDeclenchement"));

        tvHistorique.setItems(data);
    }

    public void setSysteme(SystemeIrrigation systeme) {
        this.systeme = systeme;
        if (systeme != null) {
            lblSysteme.setText("Historique du système : " + systeme.getNomSysteme());
            chargerHistorique();
        }
    }

    private void chargerHistorique() {
        if (systeme == null) {
            return;
        }
        data.clear();
        try {
            data.addAll(service.afficherParSysteme(systeme.getIdSysteme()));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/page1.fxml"));
            btnRetour.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
