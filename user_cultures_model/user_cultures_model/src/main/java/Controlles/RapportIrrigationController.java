package Controlles;

import Entites.Parcelle;
import Services.Dto.BilanHydriqueDto;
import Services.Dto.ConsommationParcelleDto;
import Services.Dto.LigneIrrigationDto;
import Services.Dto.RecommandationIrrigationDto;
import Services.IrrigationApiService;
import Services.IrrigationMetierAvanceService;
import Services.ParcelleCRUD;
import Utils.PdfRapportService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RapportIrrigationController {

    @FXML private ComboBox<Parcelle> cbParcelle;
    @FXML private DatePicker dateDebutBilan;
    @FXML private DatePicker dateFinBilan;
    @FXML private Label labelResultatBilan;
    @FXML private ComboBox<Parcelle> cbParcelleConso;
    @FXML private DatePicker dateDebutApi;
    @FXML private DatePicker dateFinApi;
    @FXML private Label labelResultatConso;
    @FXML private TableView<RecommandationIrrigationDto> tableRecommandations;
    @FXML private TableColumn<RecommandationIrrigationDto, String> colRecSysteme;
    @FXML private TableColumn<RecommandationIrrigationDto, String> colRecParcelle;
    @FXML private TableColumn<RecommandationIrrigationDto, String> colRecPriorite;
    @FXML private TableColumn<RecommandationIrrigationDto, String> colRecMotif;
    @FXML private DatePicker datePicker;
    @FXML private TableView<LigneIrrigationDto> tableApresDate;
    @FXML private TableColumn<LigneIrrigationDto, String> colDate;
    @FXML private TableColumn<LigneIrrigationDto, String> colSysteme;
    @FXML private TableColumn<LigneIrrigationDto, String> colParcelle;
    @FXML private TableColumn<LigneIrrigationDto, Integer> colDuree;
    @FXML private TableColumn<LigneIrrigationDto, BigDecimal> colVolume;
    @FXML private TableColumn<LigneIrrigationDto, BigDecimal> colHumidite;
    @FXML private TableColumn<LigneIrrigationDto, String> colType;

    private final IrrigationMetierAvanceService metier = new IrrigationMetierAvanceService();
    private final IrrigationApiService apiService = new IrrigationApiService();
    private final ParcelleCRUD parcelleCRUD = new ParcelleCRUD();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        try {
            List<Parcelle> parcelles = parcelleCRUD.afficherToutes();
            javafx.collections.ObservableList<Parcelle> obs = javafx.collections.FXCollections.observableArrayList(parcelles);
            cbParcelle.setItems(obs);
            cbParcelleConso.setItems(javafx.collections.FXCollections.observableArrayList(parcelles));
            if (!parcelles.isEmpty()) {
                cbParcelle.getSelectionModel().selectFirst();
                cbParcelleConso.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            labelResultatBilan.setText("Erreur chargement parcelles: " + e.getMessage());
        }
        dateDebutBilan.setValue(LocalDate.now().minusMonths(1));
        dateFinBilan.setValue(LocalDate.now());
        dateDebutApi.setValue(LocalDate.now().minusMonths(1));
        dateFinApi.setValue(LocalDate.now());

        colRecSysteme.setCellValueFactory(new PropertyValueFactory<>("nomSysteme"));
        colRecParcelle.setCellValueFactory(new PropertyValueFactory<>("nomParcelle"));
        colRecPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        colRecMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));

        colDate.setCellValueFactory(cb -> {
            LigneIrrigationDto d = cb.getValue();
            if (d == null || d.getDateIrrigation() == null) return new javafx.beans.property.SimpleStringProperty("");
            return new javafx.beans.property.SimpleStringProperty(d.getDateIrrigation().format(DATE_FORMAT));
        });
        colSysteme.setCellValueFactory(new PropertyValueFactory<>("nomSysteme"));
        colParcelle.setCellValueFactory(new PropertyValueFactory<>("nomParcelle"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeMinutes"));
        colVolume.setCellValueFactory(new PropertyValueFactory<>("volumeEau"));
        colHumidite.setCellValueFactory(new PropertyValueFactory<>("humiditeAvant"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeDeclenchement"));

        datePicker.setValue(LocalDate.now().minusMonths(1));
    }

    @FXML
    private void calculerBilan() {
        Parcelle p = cbParcelle.getSelectionModel().getSelectedItem();
        if (p == null) {
            JOptionPane.showMessageDialog(null, "Veuillez sélectionner une parcelle.");
            return;
        }
        LocalDate debut = dateDebutBilan.getValue();
        LocalDate fin = dateFinBilan.getValue();
        if (debut == null || fin == null) {
            JOptionPane.showMessageDialog(null, "Veuillez choisir la période (du / au).");
            return;
        }
        if (fin.isBefore(debut)) {
            JOptionPane.showMessageDialog(null, "La date « Au » doit être après « Du ».");
            return;
        }
        try {
            BilanHydriqueDto dto = metier.getBilanHydriqueParcelle(p.getId(), debut, fin);
            String msg = String.format(
                    "Parcelle : %s\nSurface : %.2f ha\nPériode : %s → %s\nVolume total eau : %s L\nConsommation : %s m³/ha",
                    dto.getNomParcelle(),
                    dto.getSurface(),
                    dto.getDateDebut(),
                    dto.getDateFin(),
                    dto.getVolumeTotalEau() != null ? dto.getVolumeTotalEau().toPlainString() : "0",
                    dto.getConsommationParHectare() != null ? dto.getConsommationParHectare().toPlainString() : "0"
            );
            labelResultatBilan.setText(msg);
        } catch (SQLException e) {
            labelResultatBilan.setText("Erreur : " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void apiConsommation() {
        Parcelle p = cbParcelleConso.getSelectionModel().getSelectedItem();
        if (p == null) {
            JOptionPane.showMessageDialog(null, "Veuillez sélectionner une parcelle.");
            return;
        }
        LocalDate debut = dateDebutApi.getValue();
        LocalDate fin = dateFinApi.getValue();
        if (debut == null || fin == null) {
            JOptionPane.showMessageDialog(null, "Veuillez choisir la période (du / au).");
            return;
        }
        if (fin.isBefore(debut)) {
            JOptionPane.showMessageDialog(null, "La date « Au » doit être après « Du ».");
            return;
        }
        try {
            ConsommationParcelleDto dto = apiService.getConsommationEauParParcelle(p.getId(), debut, fin);
            String msg = String.format(
                    "Parcelle : %s | Surface : %.2f ha | Période : %s → %s\nVolume total eau : %s L | Nombre d'irrigations : %d",
                    dto.getNomParcelle(),
                    dto.getSurface(),
                    dto.getDateDebut(),
                    dto.getDateFin(),
                    dto.getVolumeTotalEau() != null ? dto.getVolumeTotalEau().toPlainString() : "0",
                    dto.getNombreIrrigations()
            );
            labelResultatConso.setText(msg);
        } catch (SQLException e) {
            labelResultatConso.setText("Erreur : " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void apiRecommandations() {
        try {
            List<RecommandationIrrigationDto> list = apiService.getRecommandationsIrrigation();
            tableRecommandations.getItems().setAll(list);
            labelResultatConso.setText("API 2 : " + list.size() + " recommandation(s) chargée(s).");
        } catch (SQLException e) {
            tableRecommandations.getItems().clear();
            labelResultatConso.setText("Erreur : " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void generer() {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            JOptionPane.showMessageDialog(null, "Veuillez choisir une date.");
            return;
        }
        try {
            List<LigneIrrigationDto> list = metier.getIrrigationsApresDate(date);
            tableApresDate.getItems().setAll(list);

            Stage stage = (Stage) datePicker.getScene().getWindow();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport PDF");
            fileChooser.setInitialFileName("rapport_irrigation_" + date + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                String path = file.getAbsolutePath();
                if (!path.toLowerCase().endsWith(".pdf")) path += ".pdf";
                File dest = new File(path);
                File created = PdfRapportService.genererPdf(dest, list, date);
                if (created != null) {
                    JOptionPane.showMessageDialog(null, "PDF enregistré :\n" + created.getAbsolutePath());
                } else {
                    JOptionPane.showMessageDialog(null, "Erreur lors de la génération du PDF.");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur: " + e.getMessage());
        }
    }
}
