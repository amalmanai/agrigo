package Controlles;

import Entites.Parcelle;
import Services.ParcelleCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.swing.JOptionPane;
import java.sql.SQLException;

public class ParcelleController {

    @FXML private Label lblTitle;
    @FXML private Button btnSave;
    @FXML private TextField tfNomP;
    @FXML private TextField tfSurface;
    @FXML private TextField tfGps;
    @FXML private ComboBox<String> cbTypeSol;

    private Parcelle parcelleToEdit;
    private static final int MAX_NOM_LENGTH = 80;
    private static final double MAX_SURFACE = 1_000_000d;
    private static final String NOM_REGEX = "^[\\p{L}][\\p{L}\\s'\\-]{1,79}$";

    @FXML
    public void initialize() {
        cbTypeSol.getItems().setAll(
                "Argileux",
                "Sableux",
                "Limoneux",
                "Calcaire",
                "Humifere"
        );
        cbTypeSol.setEditable(true);
    }

    @FXML
    void saveParcelle(ActionEvent event) {
        String nom = safeTrim(tfNomP.getText());
        String surfaceText = safeTrim(tfSurface.getText());
        String gpsText = safeTrim(tfGps.getText());
        String typeSol = safeTrim(cbTypeSol.getValue());

        String validationError = validateParcelleInputs(nom, surfaceText, gpsText, typeSol);
        if (validationError != null) {
            showError(validationError);
            return;
        }

        try {
            Parcelle p = new Parcelle();
            p.setNom(nom);
            p.setSurface(parseFrenchNumber(surfaceText));
            p.setGps(gpsText.isEmpty() ? null : gpsText);
            p.setTypeSol(typeSol);

            ParcelleCRUD pc = new ParcelleCRUD();
            if (parcelleToEdit != null) {
                p.setId(parcelleToEdit.getId());
                pc.modifier(p);
                JOptionPane.showMessageDialog(null, "Parcelle modifiee !");
                closeWindow();
                return;
            }

            pc.ajouter(p);
            JOptionPane.showMessageDialog(null, "Parcelle ajoutee avec succes !");
            viderChamps(null);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "La surface doit etre un nombre !");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur Connexion : Verifiez XAMPP !");
            System.err.println("Erreur SQL: " + e.getMessage());
        }
    }

    private String validateParcelleInputs(String nom, String surfaceText, String gpsText, String typeSol) {
        if (nom.isEmpty()) {
            return "Le nom de la parcelle est obligatoire.";
        }
        if (nom.length() > MAX_NOM_LENGTH) {
            return "Le nom de la parcelle ne doit pas depasser 80 caracteres.";
        }
        if (!nom.matches(NOM_REGEX)) {
            return "Nom invalide: utilisez des lettres, espaces, tirets ou apostrophes.";
        }

        if (surfaceText.isEmpty()) {
            return "La surface est obligatoire.";
        }

        double surface;
        try {
            surface = parseFrenchNumber(surfaceText);
        } catch (NumberFormatException ex) {
            return "La surface doit etre un nombre valide (ex: 12.5).";
        }
        if (surface <= 0 || surface > MAX_SURFACE) {
            return "La surface doit etre superieure a 0 et inferieure ou egale a 1000000.";
        }

        if (typeSol.isEmpty()) {
            return "Le type de sol est obligatoire.";
        }
        if (!cbTypeSol.getItems().contains(typeSol)) {
            return "Type de sol invalide. Choisissez une valeur de la liste.";
        }

        if (!gpsText.isEmpty()) {
            if (!isValidGps(gpsText)) {
                return "GPS invalide. Format attendu: latitude, longitude (ex: 36.8065, 10.1815).";
            }
        }

        return null;
    }

    private boolean isValidGps(String gpsText) {
        String[] parts = gpsText.split(",");
        if (parts.length != 2) {
            return false;
        }

        try {
            double latitude = parseFrenchNumber(parts[0].trim());
            double longitude = parseFrenchNumber(parts[1].trim());
            return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private double parseFrenchNumber(String value) {
        return Double.parseDouble(value.replace(",", "."));
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    @FXML
    void viderChamps(ActionEvent event) {
        tfNomP.clear();
        tfSurface.clear();
        tfGps.clear();
        cbTypeSol.getSelectionModel().clearSelection();
    }

    @FXML
    void backToMenu(ActionEvent event) {
        closeWindow();
    }

    public void setParcelleForEdit(Parcelle parcelle) {
        this.parcelleToEdit = parcelle;
        tfNomP.setText(parcelle.getNom());
        tfSurface.setText(String.valueOf(parcelle.getSurface()));
        tfGps.setText(parcelle.getGps());
        cbTypeSol.setValue(parcelle.getTypeSol());
        if (lblTitle != null) {
            lblTitle.setText("Modifier Parcelle");
        }
        if (btnSave != null) {
            btnSave.setText("Mettre a jour");
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) tfNomP.getScene().getWindow();
        stage.close();
    }
}
