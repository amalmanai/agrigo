package Controlles;

import Entites.SystemeIrrigation;
import Services.SystemeIrrigationCRUD;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.SQLException;

public class FormSystemeController {

    @FXML private Label lblTitle;
    @FXML private TextField tfIdParcelle, tfNom, tfSeuil;
    @FXML private ComboBox<String> cbMode, cbStatut;

    private final SystemeIrrigationCRUD service = new SystemeIrrigationCRUD();
    private SystemeIrrigation existing;
    private Runnable onSaved;

    @FXML
    private void initialize() {
        cbMode.setItems(javafx.collections.FXCollections.observableArrayList("AUTO", "MANUEL"));
        cbStatut.setItems(javafx.collections.FXCollections.observableArrayList("ACTIF", "INACTIF"));
        cbMode.getSelectionModel().select("MANUEL");
        cbStatut.getSelectionModel().select("ACTIF");
    }

    public void setSystemeForEdit(SystemeIrrigation s) {
        this.existing = s;
        if (lblTitle != null) lblTitle.setText(s == null ? "Nouveau système" : "Modifier le système");

        if (s != null) {
            tfIdParcelle.setText(String.valueOf(s.getIdParcelle()));
            tfNom.setText(s.getNomSysteme());
            tfSeuil.setText(s.getSeuilHumidite() != null ? s.getSeuilHumidite().toString() : "");
            cbMode.getSelectionModel().select(s.getMode());
            cbStatut.getSelectionModel().select(s.getStatut());
        } else {
            tfIdParcelle.clear();
            tfNom.clear();
            tfSeuil.clear();
            cbMode.getSelectionModel().select("MANUEL");
            cbStatut.getSelectionModel().select("ACTIF");
        }
    }

    public void setOnSaved(Runnable callback) {
        this.onSaved = callback;
    }

    @FXML
    private void enregistrer() {
        long idParcelle;
        try {
            idParcelle = Long.parseLong(tfIdParcelle.getText().trim());
            if (idParcelle <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID Parcelle invalide (nombre positif requis).");
            return;
        }

        String nom = tfNom.getText().trim();
        if (nom.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Le nom du système est obligatoire.");
            return;
        }

        BigDecimal seuil;
        try {
            seuil = new BigDecimal(tfSeuil.getText().trim());
            if (seuil.compareTo(BigDecimal.ZERO) < 0 || seuil.compareTo(new BigDecimal("100")) > 0)
                throw new IllegalArgumentException("Seuil 0-100");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Seuil humidité invalide (0-100).");
            return;
        }

        String mode = cbMode.getSelectionModel().getSelectedItem();
        String statut = cbStatut.getSelectionModel().getSelectedItem();
        if (mode == null || statut == null) {
            JOptionPane.showMessageDialog(null, "Veuillez sélectionner mode et statut.");
            return;
        }

        SystemeIrrigation s = existing != null ? existing : new SystemeIrrigation();
        s.setIdParcelle(idParcelle);
        s.setNomSysteme(nom);
        s.setSeuilHumidite(seuil);
        s.setMode(mode);
        s.setStatut(statut);

        try {
            if (existing == null) {
                service.ajouter(s);
                JOptionPane.showMessageDialog(null, "Système ajouté.");
            } else {
                service.modifier(s);
                JOptionPane.showMessageDialog(null, "Système modifié.");
            }
            if (onSaved != null) onSaved.run();
            ((Stage) tfNom.getScene().getWindow()).close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur: " + e.getMessage());
        }
    }
}
