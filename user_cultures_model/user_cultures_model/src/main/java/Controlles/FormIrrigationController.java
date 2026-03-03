package Controlles;

import Entites.HistoriqueIrrigation;
import Entites.SystemeIrrigation;
import Services.HistoriqueIrrigationCRUD;
import Services.SystemeIrrigationCRUD;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FormIrrigationController {

    @FXML private Label lblTitle;
    @FXML private ComboBox<SystemeIrrigation> cbSysteme;
    @FXML private TextField tfDuree, tfVolume, tfHumidite;
    @FXML private ComboBox<String> cbType;

    private final HistoriqueIrrigationCRUD service = new HistoriqueIrrigationCRUD();
    private final SystemeIrrigationCRUD serviceSysteme = new SystemeIrrigationCRUD();
    private HistoriqueIrrigation existing;
    private Runnable onSaved;

    @FXML
    private void initialize() {
        cbType.setItems(FXCollections.observableArrayList("AUTO", "MANUEL"));
        cbType.getSelectionModel().select("MANUEL");

        cbSysteme.setConverter(new StringConverter<SystemeIrrigation>() {
            @Override
            public String toString(SystemeIrrigation s) {
                return s == null ? "" : s.getIdSysteme() + " - " + s.getNomSysteme();
            }
            @Override
            public SystemeIrrigation fromString(String t) { return null; }
        });
    }

    public void setHistoriqueForEdit(HistoriqueIrrigation h) {
        this.existing = h;
        if (lblTitle != null) lblTitle.setText(h == null ? "Nouvelle irrigation" : "Modifier l'irrigation");

        List<SystemeIrrigation> systems = new ArrayList<>();
        try {
            systems.addAll(serviceSysteme.afficher());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Impossible de charger les systèmes.");
            return;
        }
        cbSysteme.setItems(FXCollections.observableArrayList(systems));
        if (!systems.isEmpty()) cbSysteme.getSelectionModel().selectFirst();

        if (h != null) {
            tfDuree.setText(String.valueOf(h.getDureeMinutes()));
            tfVolume.setText(h.getVolumeEau() != null ? h.getVolumeEau().toString() : "");
            tfHumidite.setText(h.getHumiditeAvant() != null ? h.getHumiditeAvant().toString() : "");
            cbType.getSelectionModel().select(h.getTypeDeclenchement());
            for (SystemeIrrigation s : systems) {
                if (s.getIdSysteme() == h.getIdSysteme()) {
                    cbSysteme.getSelectionModel().select(s);
                    break;
                }
            }
        } else {
            tfDuree.setText("30");
            tfVolume.setText("100");
            tfHumidite.setText("30");
        }
    }

    public void setOnSaved(Runnable callback) {
        this.onSaved = callback;
    }

    @FXML
    private void enregistrer() {
        SystemeIrrigation sys = cbSysteme.getSelectionModel().getSelectedItem();
        if (sys == null) {
            JOptionPane.showMessageDialog(null, "Veuillez sélectionner un système.");
            return;
        }
        int duree;
        try {
            duree = Integer.parseInt(tfDuree.getText().trim());
            if (duree <= 0) throw new NumberFormatException("Durée invalide");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Durée invalide (nombre positif requis).");
            return;
        }
        BigDecimal volume, humidite;
        try {
            volume = new BigDecimal(tfVolume.getText().trim());
            if (volume.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Volume négatif");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Volume invalide.");
            return;
        }
        try {
            humidite = new BigDecimal(tfHumidite.getText().trim());
            if (humidite.compareTo(BigDecimal.ZERO) < 0 || humidite.compareTo(new BigDecimal("100")) > 0)
                throw new IllegalArgumentException("Humidité 0-100");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Humidité invalide (0-100).");
            return;
        }
        String type = cbType.getSelectionModel().getSelectedItem();
        if (type == null || type.isBlank()) {
            JOptionPane.showMessageDialog(null, "Veuillez sélectionner le type (AUTO/MANUEL).");
            return;
        }

        HistoriqueIrrigation h = existing != null ? existing : new HistoriqueIrrigation();
        h.setIdSysteme(sys.getIdSysteme());
        h.setDureeMinutes(duree);
        h.setVolumeEau(volume);
        h.setHumiditeAvant(humidite);
        h.setTypeDeclenchement(type);

        try {
            if (existing == null) {
                service.ajouter(h);
                JOptionPane.showMessageDialog(null, "Irrigation ajoutée.");
            } else {
                service.modifier(h);
                JOptionPane.showMessageDialog(null, "Irrigation modifiée.");
            }
            if (onSaved != null) onSaved.run();
            ((Stage) tfDuree.getScene().getWindow()).close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur: " + e.getMessage());
        }
    }
}
