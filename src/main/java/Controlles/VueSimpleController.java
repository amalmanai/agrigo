package Controlles;

import Entites.SystemeIrrigation;
import Services.SystemeIrrigationCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.SQLException;

public class VueSimpleController {

    @FXML private Button btnAjouter, btnModifier, btnSupprimer;
    @FXML private TableView<SystemeIrrigation> tvSystemes;
    @FXML private TableColumn<SystemeIrrigation, Long> colId, colIdParcelle;
    @FXML private TableColumn<SystemeIrrigation, String> colNom, colMode, colStatut;
    @FXML private TableColumn<SystemeIrrigation, BigDecimal> colSeuil;
    @FXML private TableColumn<SystemeIrrigation, java.sql.Timestamp> colDateCreation;

    private final SystemeIrrigationCRUD service = new SystemeIrrigationCRUD();
    private final ObservableList<SystemeIrrigation> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idSysteme"));
        colIdParcelle.setCellValueFactory(new PropertyValueFactory<>("idParcelle"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomSysteme"));
        colSeuil.setCellValueFactory(new PropertyValueFactory<>("seuilHumidite"));
        colMode.setCellValueFactory(new PropertyValueFactory<>("mode"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDateCreation.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        tvSystemes.setItems(data);
        charger();
    }

    private void charger() {
        data.clear();
        try {
            data.addAll(service.afficher());
        } catch (SQLException e) {
            // Ne pas bloquer l'affichage si la BD est inaccessible
        }
    }

    private Dialog<SystemeIrrigation> creerFormulaireAjout(SystemeIrrigation existant) {
        Dialog<SystemeIrrigation> dialog = new Dialog<>();
        dialog.setTitle(existant == null ? "Ajouter un système" : "Modifier le système");
        dialog.setHeaderText("Remplissez tous les champs de la table systeme_irrigation");

        ButtonType btnValider = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField tfIdParcelle = new TextField(existant != null ? String.valueOf(existant.getIdParcelle()) : "1");
        TextField tfNom = new TextField(existant != null ? existant.getNomSysteme() : "");
        TextField tfSeuil = new TextField(existant != null && existant.getSeuilHumidite() != null ? existant.getSeuilHumidite().toString() : "30");
        ComboBox<String> comboMode = new ComboBox<>(FXCollections.observableArrayList("AUTO", "MANUEL"));
        comboMode.getSelectionModel().select(existant != null ? existant.getMode() : "MANUEL");
        comboMode.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> comboStatut = new ComboBox<>(FXCollections.observableArrayList("ACTIF", "INACTIF"));
        comboStatut.getSelectionModel().select(existant != null ? existant.getStatut() : "ACTIF");
        comboStatut.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("id_parcelle *:"), 0, 0);
        grid.add(tfIdParcelle, 1, 0);
        grid.add(new Label("nom_systeme *:"), 0, 1);
        grid.add(tfNom, 1, 1);
        grid.add(new Label("seuil_humidite *:"), 0, 2);
        grid.add(tfSeuil, 1, 2);
        grid.add(new Label("mode *:"), 0, 3);
        grid.add(comboMode, 1, 3);
        grid.add(new Label("statut *:"), 0, 4);
        grid.add(comboStatut, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt == btnValider) {
                SystemeIrrigation s = existant != null ? existant : new SystemeIrrigation();
                try {
                    s.setIdParcelle(Long.parseLong(tfIdParcelle.getText().trim()));
                    s.setNomSysteme(tfNom.getText().trim());
                    s.setSeuilHumidite(new BigDecimal(tfSeuil.getText().trim()));
                    s.setMode(comboMode.getSelectionModel().getSelectedItem());
                    s.setStatut(comboStatut.getSelectionModel().getSelectedItem());
                    return s;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });
        return dialog;
    }

    @FXML private void handleAdd(ActionEvent e) {
        Dialog<SystemeIrrigation> dialog = creerFormulaireAjout(null);
        dialog.showAndWait().ifPresent(s -> {
            try {
                service.ajouter(s);
                charger();
                JOptionPane.showMessageDialog(null, "Système ajouté !");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Erreur: " + ex.getMessage());
            }
        });
    }

    @FXML private void handleUpdate(ActionEvent e) {
        SystemeIrrigation sel = tvSystemes.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(null, "Sélectionnez un système à modifier");
            return;
        }
        Dialog<SystemeIrrigation> dialog = creerFormulaireAjout(sel);
        dialog.showAndWait().ifPresent(s -> {
            try {
                service.modifier(s);
                charger();
                JOptionPane.showMessageDialog(null, "Système modifié");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Erreur: " + ex.getMessage());
            }
        });
    }

    @FXML private void handleDelete(ActionEvent e) {
        SystemeIrrigation sel = tvSystemes.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(null, "Sélectionnez un système à supprimer");
            return;
        }
        try {
            service.supprimer((int) sel.getIdSysteme());
            charger();
            JOptionPane.showMessageDialog(null, "Système supprimé");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erreur: " + ex.getMessage());
        }
    }
}
