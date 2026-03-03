package Controlles;

import Entites.Parcelle;
import Entites.SystemeIrrigation;
import Services.ParcelleCRUD;
import Services.SystemeIrrigationCRUD;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class FormSystemeController {

    @FXML private Label lblTitle;
    @FXML private ComboBox<Parcelle> cbParcelle;
    @FXML private TextField tfNom, tfSeuil;
    @FXML private ComboBox<String> cbMode, cbStatut;

    private final SystemeIrrigationCRUD service = new SystemeIrrigationCRUD();
    private final ParcelleCRUD parcelleCRUD = new ParcelleCRUD();
    private SystemeIrrigation existing;
    private Runnable onSaved;

    @FXML
    private void initialize() {
        cbMode.setItems(javafx.collections.FXCollections.observableArrayList("AUTO", "MANUEL"));
        cbStatut.setItems(javafx.collections.FXCollections.observableArrayList("ACTIF", "INACTIF"));
        cbMode.getSelectionModel().select("MANUEL");
        cbStatut.getSelectionModel().select("ACTIF");

        cbParcelle.setConverter(new StringConverter<Parcelle>() {
            @Override
            public String toString(Parcelle p) {
                return p == null ? "" : p.getNom() + " (ID: " + p.getId() + ")";
            }
            @Override
            public Parcelle fromString(String string) {
                return null;
            }
        });
        chargerParcelles();
    }

    private void chargerParcelles() {
        try {
            List<Parcelle> parcelles = parcelleCRUD.afficherToutes();
            cbParcelle.setItems(javafx.collections.FXCollections.observableArrayList(parcelles));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur chargement parcelles: " + e.getMessage());
        }
    }

    public void setSystemeForEdit(SystemeIrrigation s) {
        this.existing = s;
        if (lblTitle != null) lblTitle.setText(s == null ? "Nouveau système" : "Modifier le système");

        if (s != null) {
            selectParcelleById(s.getIdParcelle());
            tfNom.setText(s.getNomSysteme());
            tfSeuil.setText(s.getSeuilHumidite() != null ? s.getSeuilHumidite().toString() : "");
            cbMode.getSelectionModel().select(s.getMode());
            cbStatut.getSelectionModel().select(s.getStatut());
        } else {
            cbParcelle.getSelectionModel().clearSelection();
            tfNom.clear();
            tfSeuil.clear();
            cbMode.getSelectionModel().select("MANUEL");
            cbStatut.getSelectionModel().select("ACTIF");
        }
    }

    private void selectParcelleById(long id) {
        if (cbParcelle.getItems() == null) return;
        for (Parcelle p : cbParcelle.getItems()) {
            if (p.getId() == (int) id) {
                cbParcelle.getSelectionModel().select(p);
                return;
            }
        }
        cbParcelle.getSelectionModel().clearSelection();
    }

    public void setOnSaved(Runnable callback) {
        this.onSaved = callback;
    }

    @FXML
    private void enregistrer() {
        Parcelle parcelle = cbParcelle.getSelectionModel().getSelectedItem();
        if (parcelle == null) {
            JOptionPane.showMessageDialog(null, "Veuillez sélectionner une parcelle.");
            return;
        }
        long idParcelle = parcelle.getId();

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
            ((Stage) cbParcelle.getScene().getWindow()).close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur: " + e.getMessage());
        }
    }
}
