package controller;

import entity.recolte;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.stage.Stage;
import services.recolteCRUD;

import java.sql.Date;
import java.sql.SQLException;

public class RecolteFormController implements Initializable {

    private static final String[] UNITES = { "kg", "tonne", "Quintal", "gram" };

    @FXML private TextField tfNom;
    @FXML private TextField tfQuantite;
    @FXML private ComboBox<String> cbUnite;
    @FXML private DatePicker dpDate;
    @FXML private TextField tfCout;
    @FXML private Button btnEnregistrer;
    @FXML private Label lblTitle;

    private final recolteCRUD service = new recolteCRUD();
    private recolte editingRecolte; // null = add, non-null = edit

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (cbUnite != null) {
            cbUnite.setItems(FXCollections.observableArrayList(UNITES));
            cbUnite.getSelectionModel().selectFirst(); // default "kg"
        }
    }

    public void setRecolteForEdit(recolte r) {
        this.editingRecolte = r;
        if (lblTitle != null) lblTitle.setText("Modifier la récolte");
        if (r == null) return;
        tfNom.setText(r.getNom_produit());
        tfQuantite.setText(String.valueOf(r.getQuantite()));
        if (cbUnite != null) {
            String u = r.getUnite() != null ? r.getUnite() : "kg";
            if (cbUnite.getItems().contains(u)) {
                cbUnite.getSelectionModel().select(u);
            } else {
                cbUnite.getSelectionModel().selectFirst();
            }
        }
        dpDate.setValue(r.getDate_recolte() != null ? r.getDate_recolte().toLocalDate() : null);
        tfCout.setText(String.valueOf(r.getCout_production()));
    }

    @FXML
    private void save() {
        if (!validate()) return;
        try {
            String nom = tfNom.getText().trim();
            double quantite = Double.parseDouble(tfQuantite.getText().trim());
            String unite = (cbUnite.getValue() != null && !cbUnite.getValue().isEmpty()) ? cbUnite.getValue() : "kg";
            Date date = Date.valueOf(dpDate.getValue());
            double cout = Double.parseDouble(tfCout.getText().trim());

            if (editingRecolte != null) {
                recolte r = new recolte(editingRecolte.getId_recolte(), nom, quantite, unite, date, cout, 1);
                service.modifier(r);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Récolte mise à jour.");
            } else {
                recolte r = new recolte(0, nom, quantite, unite, date, cout, 1);
                service.ajouter(r);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Récolte ajoutée.");
            }
            closeStage();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Saisie invalide", "Quantité et coût doivent être des nombres.");
        }
    }

    private boolean validate() {
        StringBuilder msg = new StringBuilder();
        if (tfNom.getText() == null || tfNom.getText().trim().isEmpty())
            msg.append("- Nom du produit requis.\n");
        if (dpDate.getValue() == null)
            msg.append("- Date de récolte requise.\n");
        try {
            double q = Double.parseDouble(tfQuantite.getText().trim());
            if (q <= 0) msg.append("- Quantité > 0.\n");
        } catch (NumberFormatException e) {
            msg.append("- Quantité invalide.\n");
        }
        try {
            Double.parseDouble(tfCout.getText().trim());
        } catch (NumberFormatException e) {
            msg.append("- Coût invalide.\n");
        }
        if (msg.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Saisie invalide", msg.toString());
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(content);
        a.showAndWait();
    }

    private void closeStage() {
        Stage stage = (Stage) (btnEnregistrer != null ? btnEnregistrer.getScene().getWindow() : null);
        if (stage != null) stage.close();
    }
}
