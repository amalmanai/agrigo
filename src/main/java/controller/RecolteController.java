package controller;

import entity.recolte;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.recolteCRUD;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class RecolteController {

    @FXML private TextField nomProduitField, quantiteField, uniteField, coutField;
    @FXML private DatePicker datePicker;
    @FXML private TableView<recolte> recolteTable;
    @FXML private TableColumn<recolte, String> colNom;
    @FXML private TableColumn<recolte, Double> colQte;

    private final recolteCRUD service = new recolteCRUD();
    private int selectedId = -1; // Pour savoir quelle ligne est sélectionnée

    @FXML
    public void initialize() {
        // Configuration des colonnes du tableau
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_produit"));
        colQte.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        rafraichir();

        // Détecter le clic sur une ligne
        recolteTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedId = newVal.getId_recolte();
                remplirChamps(newVal);
            }
        });
    }

    @FXML
    private void ajouterRecolte() {
        if (estValide()) { // Contrôle de saisie avant ajout
            try {
                recolte r = extraireInfos();
                service.ajouter(r);
                rafraichir();
                viderChamps();
                montrerAlerte(Alert.AlertType.INFORMATION, "Succès", "Récolte ajoutée avec succès !");
            } catch (SQLException e) {
                montrerAlerte(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
            }
        }
    }

    @FXML
    private void modifierRecolte() {
        if (selectedId != -1 && estValide()) {
            try {
                recolte r = extraireInfos();
                // On crée un objet avec l'ID sélectionné pour la mise à jour
                recolte aModifier = new recolte(selectedId, r.getNom_produit(), r.getQuantite(),
                        r.getUnite(), r.getDate_recolte(), r.getCout_production(), 1);
                service.modifier(aModifier);
                rafraichir();
                montrerAlerte(Alert.AlertType.INFORMATION, "Succès", "Récolte mise à jour !");
            } catch (SQLException e) {
                montrerAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        } else if (selectedId == -1) {
            montrerAlerte(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner une récolte.");
        }
    }

    @FXML
    private void supprimerRecolte() {
        if (selectedId != -1) {
            try {
                service.supprimer(selectedId);
                rafraichir();
                viderChamps();
                selectedId = -1;
            } catch (SQLException e) {
                montrerAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    // --- Contrôles de Saisie ---
    private boolean estValide() {
        String msg = "";
        if (nomProduitField.getText().isEmpty()) msg += "- Le nom du produit est requis.\n";
        if (datePicker.getValue() == null) msg += "- La date est requise.\n";

        try {
            double qte = Double.parseDouble(quantiteField.getText());
            if (qte <= 0) msg += "- La quantité doit être supérieure à 0.\n";
            Double.parseDouble(coutField.getText());
        } catch (NumberFormatException e) {
            msg += "- La quantité et le coût doivent être des nombres.\n";
        }

        if (!msg.isEmpty()) {
            montrerAlerte(Alert.AlertType.WARNING, "Erreur de saisie", msg);
            return false;
        }
        return true;
    }

    // --- Utilitaires ---
    private recolte extraireInfos() {
        return new recolte(0, nomProduitField.getText(),
                Double.parseDouble(quantiteField.getText()), uniteField.getText(),
                Date.valueOf(datePicker.getValue()), Double.parseDouble(coutField.getText()), 1);
    }

    private void rafraichir() {
        try {
            recolteTable.setItems(FXCollections.observableArrayList(service.afficher()));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void remplirChamps(recolte r) {
        nomProduitField.setText(r.getNom_produit());
        quantiteField.setText(String.valueOf(r.getQuantite()));
        uniteField.setText(r.getUnite());
        coutField.setText(String.valueOf(r.getCout_production()));
        datePicker.setValue(r.getDate_recolte().toLocalDate());
    }

    private void viderChamps() {
        nomProduitField.clear(); quantiteField.clear(); uniteField.clear();
        coutField.clear(); datePicker.setValue(null);
    }

    private void montrerAlerte(Alert.AlertType type, String titre, String texte) {
        Alert a = new Alert(type);
        a.setTitle(titre);
        a.setContentText(texte);
        a.show();
    }
}