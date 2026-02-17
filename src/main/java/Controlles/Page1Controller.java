package Controlles;

import Entites.SystemeIrrigation;
import Services.SystemeIrrigationCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import javax.swing.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

public class Page1Controller {

    @FXML
    private Button btnAjouter;

    @FXML
    private Button btnModifier;

    @FXML
    private Button btnSupprimer;

    @FXML
    private Button btnHistorique;

    @FXML
    private TextField tfNom;

    @FXML
    private TextField tfMode;

    @FXML
    private TextField tfSeuil;

    @FXML
    private TableView<SystemeIrrigation> tvSystemes;

    @FXML
    private TableColumn<SystemeIrrigation, Long> colId;

    @FXML
    private TableColumn<SystemeIrrigation, String> colNom;

    @FXML
    private TableColumn<SystemeIrrigation, String> colMode;

    @FXML
    private TableColumn<SystemeIrrigation, BigDecimal> colSeuil;

    @FXML
    private TableColumn<SystemeIrrigation, String> colStatut;

    private final SystemeIrrigationCRUD service = new SystemeIrrigationCRUD();
    private final ObservableList<SystemeIrrigation> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idSysteme"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomSysteme"));
        colMode.setCellValueFactory(new PropertyValueFactory<>("mode"));
        colSeuil.setCellValueFactory(new PropertyValueFactory<>("seuilHumidite"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        tvSystemes.setItems(data);
        chargerSystemes();

        tvSystemes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                tfNom.setText(newSel.getNomSysteme());
                tfMode.setText(newSel.getMode());
                tfSeuil.setText(newSel.getSeuilHumidite() != null ? newSel.getSeuilHumidite().toString() : "");
            }
        });
    }

    private void chargerSystemes() {
        data.clear();
        try {
            data.addAll(service.afficher());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void handleAddSystem(ActionEvent event) {
        try {
            long idParcelle = 1L; // pour simplifier
            String nomSysteme = tfNom.getText();
            String mode = tfMode.getText();
            BigDecimal seuilHumidite = new BigDecimal(tfSeuil.getText());

            SystemeIrrigation s = new SystemeIrrigation(
                    idParcelle,
                    nomSysteme,
                    seuilHumidite,
                    mode,
                    "ACTIF"
            );

            service.ajouter(s);
            chargerSystemes();
            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(), "Système d'irrigation ajouté !");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(), "Seuil d'humidité invalide");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(), "Erreur BD : " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateSystem(ActionEvent event) {
        SystemeIrrigation selected = tvSystemes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(), "Veuillez sélectionner un système à modifier");
            return;
        }
        try {
            selected.setNomSysteme(tfNom.getText());
            selected.setMode(tfMode.getText());
            selected.setSeuilHumidite(new BigDecimal(tfSeuil.getText()));

            service.modifier(selected);
            chargerSystemes();
            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(), "Système d'irrigation modifié");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(), "Seuil d'humidité invalide");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(), "Erreur BD : " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteSystem(ActionEvent event) {
        SystemeIrrigation selected = tvSystemes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(), "Veuillez sélectionner un système à supprimer");
            return;
        }
        try {
            service.supprimer((int) selected.getIdSysteme());
            chargerSystemes();
            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(), "Système d'irrigation supprimé");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(), "Erreur BD : " + e.getMessage());
        }
    }

    @FXML
    private void handleShowHistory(ActionEvent event) {
        SystemeIrrigation selected = tvSystemes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(), "Veuillez sélectionner un système");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/details.fxml"));
            Parent root = loader.load();
            DetailsController dc = loader.getController();
            dc.setSysteme(selected);
            btnHistorique.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
