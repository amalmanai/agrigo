package Controlles;

import Entites.Culture;
import Entites.Parcelle;
import Services.CultureCRUD;
import Services.ParcelleCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.swing.JOptionPane;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Page1Controller {

    @FXML private Label lblTitle;
    @FXML private Button btnAjouter;
    @FXML private TextField tfNom;
    @FXML private TextField tfRendement;
    @FXML private ComboBox<String> cbEtat;
    @FXML private DatePicker dpDateSemis;
    @FXML private ComboBox<Parcelle> cbParcelle;

    private Culture cultureToEdit;
    private static final double MAX_RENDEMENT = 1_000_000d;
    private static final int MAX_NOM_LENGTH = 80;
    private static final String NOM_REGEX = "^[\\p{L}][\\p{L}\\s'\\-]{1,79}$";

    @FXML
    public void initialize() {
        cbEtat.getItems().setAll(
                "Semis",
                "Croissance",
                "Floraison",
                "Recolte",
                "Recolte termine"
        );
        cbEtat.setEditable(true);

        dpDateSemis.setValue(LocalDate.now());

        try {
            ParcelleCRUD pc = new ParcelleCRUD();
            List<Parcelle> parcelles = pc.afficher();
            Parcelle none = new Parcelle();
            none.setId(0);
            none.setNom("Aucune");
            cbParcelle.getItems().add(none);
            cbParcelle.getItems().addAll(parcelles);
            cbParcelle.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur chargement parcelles.");
        }
    }

    @FXML
    void backToMenu(ActionEvent event) {
        closeWindow();
    }

    @FXML
    void viderChamps(ActionEvent event) {
        tfNom.clear();
        tfRendement.clear();
        cbEtat.getSelectionModel().clearSelection();
        dpDateSemis.setValue(LocalDate.now());
        if (!cbParcelle.getItems().isEmpty()) {
            cbParcelle.getSelectionModel().selectFirst();
        }
    }

    @FXML
    void saveCulture(ActionEvent event) {
        String nom = safeTrim(tfNom.getText());
        String etat = safeTrim(cbEtat.getValue());
        String rendementText = safeTrim(tfRendement.getText());
        LocalDate dateSemis = dpDateSemis.getValue();
        Parcelle parcelleSelectionnee = cbParcelle.getValue();

        String validationError = validateCultureInputs(nom, etat, rendementText, dateSemis, parcelleSelectionnee);
        if (validationError != null) {
            showError(validationError);
            return;
        }

        try {
            double rendement = parseFrenchNumber(rendementText);

            Culture c = new Culture();
            c.setNom(nom);
            c.setEtat(etat);
            c.setRendement(rendement);
            c.setDateSemis(Date.valueOf(dateSemis));
            c.setIdParcelle(parcelleSelectionnee.getId());

            CultureCRUD cc = new CultureCRUD();

            if (cultureToEdit != null) {
                c.setId(cultureToEdit.getId());
                try {
                    cc.modifier(c);
                    JOptionPane.showMessageDialog(null, "Culture modifiee !");
                    closeWindow();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Erreur : Impossible d'enregistrer la culture.");
                }
                return;
            }

            cc.ajouterCulture(c, parcelleSelectionnee.getId());
            JOptionPane.showMessageDialog(null, "Culture ajoutee ! Retour au menu...");
            closeWindow();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Erreur : Le rendement doit etre un nombre !");
        }
    }

    private String validateCultureInputs(String nom, String etat, String rendementText, LocalDate dateSemis, Parcelle parcelleSelectionnee) {
        if (nom.isEmpty()) {
            return "Le nom de la culture est obligatoire.";
        }
        if (nom.length() > MAX_NOM_LENGTH) {
            return "Le nom de la culture ne doit pas depasser 80 caracteres.";
        }
        if (!nom.matches(NOM_REGEX)) {
            return "Nom invalide: utilisez des lettres, espaces, tirets ou apostrophes.";
        }

        if (etat.isEmpty()) {
            return "L'etat de croissance est obligatoire.";
        }
        if (!cbEtat.getItems().contains(etat)) {
            return "Etat invalide. Choisissez un etat propose dans la liste.";
        }

        if (rendementText.isEmpty()) {
            return "Le rendement est obligatoire.";
        }

        double rendement;
        try {
            rendement = parseFrenchNumber(rendementText);
        } catch (NumberFormatException ex) {
            return "Le rendement doit etre un nombre valide (ex: 540 ou 540.5).";
        }
        if (rendement <= 0 || rendement > MAX_RENDEMENT) {
            return "Le rendement doit etre superieur a 0 et inferieur ou egal a 1000000.";
        }

        if (dateSemis == null) {
            return "La date de semis est obligatoire.";
        }
        if (dateSemis.isAfter(LocalDate.now())) {
            return "La date de semis ne peut pas etre dans le futur.";
        }

        if (parcelleSelectionnee == null) {
            return "La parcelle est obligatoire.";
        }

        return null;
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

    public void setCultureForEdit(Culture culture) {
        this.cultureToEdit = culture;
        tfNom.setText(culture.getNom());
        cbEtat.setValue(culture.getEtat());
        tfRendement.setText(String.valueOf(culture.getRendement()));
        if (culture.getDateSemis() != null) {
            dpDateSemis.setValue(culture.getDateSemis().toLocalDate());
        }
        selectParcelleById(culture.getIdParcelle());
        if (lblTitle != null) {
            lblTitle.setText("Modifier Culture");
        }
        btnAjouter.setText("Mettre a jour");
    }

    private void selectParcelleById(int idParcelle) {
        for (Parcelle parcelle : cbParcelle.getItems()) {
            if (parcelle.getId() == idParcelle) {
                cbParcelle.getSelectionModel().select(parcelle);
                return;
            }
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnAjouter.getScene().getWindow();
        stage.close();
    }
}
