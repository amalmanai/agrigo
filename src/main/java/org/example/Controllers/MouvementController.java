package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.Entites.Mouvement;
import org.example.Entites.Produit;
import org.example.Services.MouvementCRUD;
import org.example.Services.ProduitCRUD;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class MouvementController implements Initializable {

    // --- Formulaire ---
    @FXML private Label formTitleLabel;
    @FXML private ComboBox<String> typeMouvementCombo;
    @FXML private ComboBox<Produit> produitCombo;
    @FXML private TextField quantiteField;
    @FXML private TextField motifField;
    @FXML private Label messageLabel;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnAnnuler;

    // --- Tableau Mouvements ---
    @FXML private TableView<Mouvement> tableMouvements;
    @FXML private TableColumn<Mouvement, Integer> colMvtId;
    @FXML private TableColumn<Mouvement, String> colMvtType;
    @FXML private TableColumn<Mouvement, String> colMvtProduit;
    @FXML private TableColumn<Mouvement, Integer> colMvtQuantite;
    @FXML private TableColumn<Mouvement, String> colMvtDate;
    @FXML private TableColumn<Mouvement, String> colMvtMotif;
    @FXML private TableColumn<Mouvement, Void> colMvtActions;

    // --- Stats ---
    @FXML private Label statsTotalMouvements;
    @FXML private Label statsEntrees;
    @FXML private Label statsSorties;
    @FXML private Label statsProduitsConcernes;

    // --- Services et données ---
    private final MouvementCRUD mouvementCRUD = new MouvementCRUD();
    private final ProduitCRUD produitCRUD = new ProduitCRUD();
    private final ObservableList<Mouvement> mouvementList = FXCollections.observableArrayList();
    private final ObservableList<Produit> produitList = FXCollections.observableArrayList();
    private Mouvement mouvementSelectionne = null;

    // User ID static pour le moment
    private final int STATIC_USER_ID = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurerComboBox();
        configurerColonnes();
        configurerValidation();
        configurerClicLigne();

        tableMouvements.setItems(mouvementList);
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        messageLabel.setText("");

        chargerProduits();
        chargerMouvements();
    }

    private void configurerComboBox() {
        // Type de mouvement
        typeMouvementCombo.getItems().addAll("ENTREE", "SORTIE");
        typeMouvementCombo.setValue("ENTREE");

        // Affichage personnalisé pour le ComboBox de produits
        produitCombo.setCellFactory(param -> new ListCell<Produit>() {
            @Override
            protected void updateItem(Produit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNomProduit() + " (Stock: " + item.getQuantiteDisponible() + " " + item.getUnite() + ")");
                }
            }
        });

        produitCombo.setButtonCell(new ListCell<Produit>() {
            @Override
            protected void updateItem(Produit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNomProduit() + " (Stock: " + item.getQuantiteDisponible() + " " + item.getUnite() + ")");
                }
            }
        });
    }

    private void configurerValidation() {
        // Validation de la quantité (chiffres uniquement)
        quantiteField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) {
                quantiteField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Validation au perte de focus
        quantiteField.focusedProperty().addListener((obs, ancien, nouveau) -> {
            if (!nouveau && quantiteField.getText().trim().isEmpty()) {
                quantiteField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            } else {
                quantiteField.setStyle("");
            }
        });

        motifField.focusedProperty().addListener((obs, ancien, nouveau) -> {
            if (!nouveau && motifField.getText().trim().isEmpty()) {
                motifField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            } else {
                motifField.setStyle("");
            }
        });
    }

    private void configurerColonnes() {
        colMvtId.setCellValueFactory(new PropertyValueFactory<>("idMouvement"));
        colMvtType.setCellValueFactory(new PropertyValueFactory<>("typeMouvement"));
        colMvtProduit.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colMvtQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colMvtDate.setCellValueFactory(new PropertyValueFactory<>("dateMouvement"));
        colMvtMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));

        // Formatage de la date
        colMvtDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    try {
                        LocalDateTime dt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        setText(dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    } catch (Exception e) {
                        setText(date);
                    }
                }
            }
        });

        // Colorer le type de mouvement
        colMvtType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(type);
                    if ("ENTREE".equals(type)) {
                        setStyle("-fx-text-fill: #22c55e; -fx-font-weight: 700;");
                    } else {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 700;");
                    }
                }
            }
        });

        // Colonne Actions
        colMvtActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox box = new HBox(5, btnEdit, btnDelete);

            {
                // Style bouton Modifier
                btnEdit.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-weight: 700;");
                btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-weight: 700;"));
                btnEdit.setOnMouseExited(e -> btnEdit.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-weight: 700;"));

                // Style bouton Supprimer
                btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-weight: 700;");
                btnDelete.setOnMouseEntered(e -> btnDelete.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-weight: 700;"));
                btnDelete.setOnMouseExited(e -> btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-weight: 700;"));

                box.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Mouvement m = getTableView().getItems().get(getIndex());
                btnEdit.setOnAction(e -> remplirFormulaire(m));
                btnDelete.setOnAction(e -> supprimerMouvement(m));
                setGraphic(box);
            }
        });
    }

    private void configurerClicLigne() {
        tableMouvements.getSelectionModel().selectedItemProperty().addListener((obs, ancien, selectionne) -> {
            if (selectionne != null) {
                remplirFormulaire(selectionne);
            }
        });
    }

    private void chargerProduits() {
        try {
            List<Produit> produits = produitCRUD.afficher();
            produitList.setAll(produits);
            produitCombo.setItems(produitList);
        } catch (SQLException e) {
            afficherErreur("Erreur", "Impossible de charger les produits: " + e.getMessage());
        }
    }

    private void chargerMouvements() {
        try {
            List<Mouvement> mouvements = mouvementCRUD.afficher();
            mouvementList.setAll(mouvements);
            mettreAJourStats();
        } catch (SQLException e) {
            afficherErreur("Erreur", "Impossible de charger les mouvements: " + e.getMessage());
        }
    }

    private void mettreAJourStats() {
        statsTotalMouvements.setText(String.valueOf(mouvementList.size()));

        long entrees = mouvementList.stream().filter(m -> "ENTREE".equals(m.getTypeMouvement())).count();
        long sorties = mouvementList.stream().filter(m -> "SORTIE".equals(m.getTypeMouvement())).count();
        long produitsDistincts = mouvementList.stream().map(Mouvement::getIdProduit).distinct().count();

        statsEntrees.setText(String.valueOf(entrees));
        statsSorties.setText(String.valueOf(sorties));
        statsProduitsConcernes.setText(String.valueOf(produitsDistincts));
    }

    @FXML
    public void ajouterMouvement(ActionEvent event) {
        if (!validerFormulaire()) return;

        try {
            Mouvement m = new Mouvement();
            m.setTypeMouvement(typeMouvementCombo.getValue());
            m.setDateMouvement(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            m.setQuantite(Integer.parseInt(quantiteField.getText().trim()));
            m.setMotif(motifField.getText().trim());
            m.setIdProduit(produitCombo.getValue().getIdProduit());
            m.setIdUser(STATIC_USER_ID);

            // Vérifier le stock pour les sorties
            if ("SORTIE".equals(m.getTypeMouvement())) {
                Produit p = produitCombo.getValue();
                if (p.getQuantiteDisponible() < m.getQuantite()) {
                    afficherErreur("Stock insuffisant",
                            "Stock disponible: " + p.getQuantiteDisponible() + " " + p.getUnite());
                    return;
                }
            }

            mouvementCRUD.ajouter(m);
            chargerMouvements();
            chargerProduits(); // Recharger les produits pour mettre à jour les stocks
            resetFormulaire();
            afficherSuccès("Mouvement ajouté avec succès !");
        } catch (SQLException e) {
            afficherErreur("Erreur", e.getMessage());
        }
    }

    @FXML
    public void modifierMouvement(ActionEvent event) {
        if (mouvementSelectionne == null) {
            afficherErreur("Erreur", "Sélectionnez d'abord un mouvement");
            return;
        }

        if (!validerFormulaire()) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Modifier le mouvement");
        confirm.setContentText("Voulez-vous vraiment modifier ce mouvement ?");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    mouvementSelectionne.setTypeMouvement(typeMouvementCombo.getValue());
                    mouvementSelectionne.setQuantite(Integer.parseInt(quantiteField.getText().trim()));
                    mouvementSelectionne.setMotif(motifField.getText().trim());
                    mouvementSelectionne.setIdProduit(produitCombo.getValue().getIdProduit());

                    mouvementCRUD.modifier(mouvementSelectionne);
                    chargerMouvements();
                    chargerProduits();
                    resetFormulaire();
                    afficherSuccès("Mouvement modifié avec succès !");
                } catch (SQLException e) {
                    afficherErreur("Erreur", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void supprimerMouvementAction(ActionEvent event) {
        if (mouvementSelectionne == null) {
            afficherErreur("Erreur", "Sélectionnez d'abord un mouvement");
            return;
        }
        supprimerMouvement(mouvementSelectionne);
    }

    private void supprimerMouvement(Mouvement m) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le mouvement");
        confirm.setContentText("Voulez-vous vraiment supprimer ce mouvement ?");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    mouvementCRUD.supprimer(m);
                    chargerMouvements();
                    chargerProduits();
                    if (mouvementSelectionne != null && mouvementSelectionne.getIdMouvement() == m.getIdMouvement()) {
                        resetFormulaire();
                    }
                    afficherSuccès("Mouvement supprimé avec succès !");
                } catch (SQLException e) {
                    afficherErreur("Erreur", e.getMessage());
                }
            }
        });
    }

    private void remplirFormulaire(Mouvement m) {
        mouvementSelectionne = m;

        typeMouvementCombo.setValue(m.getTypeMouvement());

        // Sélectionner le produit correspondant
        for (Produit p : produitList) {
            if (p.getIdProduit() == m.getIdProduit()) {
                produitCombo.setValue(p);
                break;
            }
        }

        quantiteField.setText(String.valueOf(m.getQuantite()));
        motifField.setText(m.getMotif());

        formTitleLabel.setText("Modifier mouvement #" + m.getIdMouvement());
        messageLabel.setText("✎ Modification en cours");
        messageLabel.setStyle("-fx-text-fill: #f59e0b;");

        btnAjouter.setDisable(true);
        btnModifier.setDisable(false);
        btnSupprimer.setDisable(false);
    }

    @FXML
    public void annulerFormulaire(ActionEvent event) {
        resetFormulaire();
    }

    private void resetFormulaire() {
        mouvementSelectionne = null;
        typeMouvementCombo.setValue("ENTREE");
        produitCombo.setValue(null);
        quantiteField.clear();
        motifField.clear();
        formTitleLabel.setText("Nouveau mouvement");
        messageLabel.setText("");

        btnAjouter.setDisable(false);
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);

        tableMouvements.getSelectionModel().clearSelection();

        // Réinitialiser les styles
        quantiteField.setStyle("");
        motifField.setStyle("");
    }

    private boolean validerFormulaire() {
        if (produitCombo.getValue() == null) {
            afficherErreur("Validation", "Veuillez sélectionner un produit");
            return false;
        }
        if (quantiteField.getText().trim().isEmpty()) {
            afficherErreur("Validation", "La quantité est obligatoire");
            quantiteField.requestFocus();
            return false;
        }
        if (motifField.getText().trim().isEmpty()) {
            afficherErreur("Validation", "Le motif est obligatoire");
            motifField.requestFocus();
            return false;
        }
        return true;
    }

    @FXML
    public void retourMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
            StackPane menuView = loader.load();

            // Remplacer la vue actuelle par le menu
            StackPane root = (StackPane) ((Button) event.getSource()).getScene().getRoot();
            root.getChildren().clear();
            root.getChildren().add(menuView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void afficherSuccès(String msg) {
        messageLabel.setText("✓ " + msg);
        messageLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 12px; -fx-font-weight: 600;");
    }

    private void afficherErreur(String titre, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}