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
import org.example.Entites.Produit;
import org.example.Services.ProduitCRUD;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class MenuController implements Initializable {

    // --- Navigation ---
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavCultures;
    @FXML private Button btnNavParcelles;
    @FXML private Button btnNavStock;
    @FXML private Button btnNavMouvements;
    @FXML private Button btnNavUsers;
    @FXML private Button btnThemeToggle;
    @FXML private Label  themeIconLabel;
    @FXML private StackPane rootPane;

    private boolean isDarkMode = true;

    // --- Recherche ---
    @FXML private TextField searchField;

    // --- Stats ---
    @FXML private Label statsCulturesActives;
    @FXML private Label statsParcelles;
    @FXML private Label statsRendement;
    @FXML private Label statsStock;

    // --- Tabs ---
    @FXML private TabPane mainTabs;
    @FXML private Tab tabCultures;
    @FXML private Tab tabParcelles;
    @FXML private Tab tabStock;
    @FXML private Tab tabUsers;

    // --- Formulaire ---
    @FXML private Label     formTitleLabel;
    @FXML private TextField nomField;
    @FXML private TextField categorieField;
    @FXML private TextField quantiteField;
    @FXML private TextField uniteField;
    @FXML private TextField seuilField;
    @FXML private TextField prixField;
    @FXML private DatePicker datePicker;
    @FXML private Label     messageLabel;
    @FXML private Button    btnAjouter;
    @FXML private Button    btnModifier;
    @FXML private Button    btnSupprimer;
    @FXML private Button    btnAnnuler;

    // --- Tableau Stock ---
    @FXML private TableView<Produit>            tableStock;
    @FXML private TableColumn<Produit, Integer> colStockId;
    @FXML private TableColumn<Produit, String>  colStockNom;
    @FXML private TableColumn<Produit, String>  colStockCategorie;
    @FXML private TableColumn<Produit, Integer> colStockQuantite;
    @FXML private TableColumn<Produit, String>  colStockUnite;
    @FXML private TableColumn<Produit, Integer> colStockSeuil;
    @FXML private TableColumn<Produit, String>  colStockDate;
    @FXML private TableColumn<Produit, Double>  colStockPrix;
    @FXML private TableColumn<Produit, Void>    colStockActions;

    private final ProduitCRUD             produitCRUD = new ProduitCRUD();
    private final ObservableList<Produit>  stockList   = FXCollections.observableArrayList();
    private       Produit                  produitSelectionne = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurerValidation();
        configurerColonnes();
        configurerClicLigne();
        configurerRecherche();

        tableStock.setItems(stockList);
        mainTabs.setVisible(false);
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        messageLabel.setText("");

        chargerProduits();
        setNavActive(btnNavDashboard);
        appliquerTheme();
    }

    private void configurerValidation() {
        quantiteField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) {
                quantiteField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        seuilField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) {
                seuilField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        prixField.textProperty().addListener((obs, old, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*\\.?\\d*")) {
                prixField.setText(old);
            }
        });

        ajoutValidationChamp(nomField, "Le nom est requis");
        ajoutValidationChamp(categorieField, "La catégorie est requise");
        ajoutValidationChamp(quantiteField, "La quantité est requise");
        ajoutValidationChamp(uniteField, "L'unité est requise");
        ajoutValidationChamp(seuilField, "Le seuil est requis");
        ajoutValidationChamp(prixField, "Le prix est requis");
    }

    private void ajoutValidationChamp(TextField champ, String messageErreur) {
        champ.focusedProperty().addListener((obs, ancien, nouveau) -> {
            if (!nouveau) {
                if (champ.getText().trim().isEmpty()) {
                    champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                    champ.setPromptText(messageErreur);
                } else {
                    champ.setStyle("-fx-border-color: #22c55e; -fx-border-width: 1; -fx-border-radius: 8;");
                }
            }
        });
    }

    private void configurerColonnes() {
        colStockId.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
        colStockNom.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colStockCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colStockQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteDisponible"));
        colStockUnite.setCellValueFactory(new PropertyValueFactory<>("unite"));
        colStockSeuil.setCellValueFactory(new PropertyValueFactory<>("seuilAlerte"));
        colStockDate.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
        colStockPrix.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));

        colStockQuantite.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty);
                if (empty || qty == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(String.valueOf(qty));
                Produit p = getTableView().getItems().get(getIndex());
                if (p != null && qty <= p.getSeuilAlerte()) {
                    setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 700;");
                } else {
                    setStyle("-fx-text-fill: #22c55e; -fx-font-weight: 600;");
                }
            }
        });

        colStockActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnDel = new Button("Supprimer");
            private final Button btnEdit = new Button("Modifier");
            private final HBox box = new HBox(5, btnEdit, btnDel);

            {
                btnDel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-weight: 700;");
                btnDel.setOnMouseEntered(e -> btnDel.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-weight: 700;"));
                btnDel.setOnMouseExited(e -> btnDel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-weight: 700;"));

                btnEdit.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-weight: 700;");
                btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-weight: 700;"));
                btnEdit.setOnMouseExited(e -> btnEdit.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-weight: 700;"));

                Tooltip.install(btnDel, new Tooltip("Supprimer ce produit"));
                Tooltip.install(btnEdit, new Tooltip("Modifier ce produit"));
                box.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Produit p = getTableView().getItems().get(getIndex());
                btnDel.setOnAction(e -> supprimerProduit(p));
                btnEdit.setOnAction(e -> remplirFormulaire(p));
                setGraphic(box);
            }
        });
    }

    private void configurerClicLigne() {
        tableStock.getSelectionModel().selectedItemProperty().addListener((obs, ancien, selectionne) -> {
            if (selectionne != null) {
                remplirFormulaire(selectionne);
            }
        });
    }

    private void chargerProduits() {
        try {
            List<Produit> produits = produitCRUD.afficher();
            stockList.setAll(produits);
            statsStock.setText(String.valueOf(produits.size()));
        } catch (SQLException e) {
            afficherErreur("Erreur chargement", e.getMessage());
        }
    }

    @FXML
    public void ajouterProduit(ActionEvent event) {
        if (!validerFormulaire()) return;

        try {
            Produit p = new Produit(
                    0,
                    nomField.getText().trim(),
                    Integer.parseInt(quantiteField.getText().trim()),
                    categorieField.getText().trim(),
                    uniteField.getText().trim(),
                    Integer.parseInt(seuilField.getText().trim()),
                    datePicker.getValue() != null ? datePicker.getValue().toString() : null,
                    Double.parseDouble(prixField.getText().trim())
            );
            produitCRUD.ajouter(p);
            chargerProduits();
            resetFormulaire();
            afficherSucces("Produit ajouté avec succès !");
        } catch (SQLException e) {
            afficherErreur("Erreur ajout", e.getMessage());
        }
    }

    @FXML
    public void modifierProduit(ActionEvent event) {
        if (produitSelectionne == null) {
            afficherErreur("Erreur", "Sélectionnez d'abord un produit");
            return;
        }

        if (!validerFormulaire()) return;

        try {
            produitSelectionne.setNomProduit(nomField.getText().trim());
            produitSelectionne.setCategorie(categorieField.getText().trim());
            produitSelectionne.setQuantiteDisponible(Integer.parseInt(quantiteField.getText().trim()));
            produitSelectionne.setUnite(uniteField.getText().trim());
            produitSelectionne.setSeuilAlerte(Integer.parseInt(seuilField.getText().trim()));
            produitSelectionne.setDateExpiration(datePicker.getValue() != null ? datePicker.getValue().toString() : null);
            produitSelectionne.setPrixUnitaire(Double.parseDouble(prixField.getText().trim()));

            produitCRUD.modifier(produitSelectionne);
            chargerProduits();
            resetFormulaire();
            afficherSucces("Produit modifié avec succès !");
        } catch (SQLException e) {
            afficherErreur("Erreur modification", e.getMessage());
        }
    }

    @FXML
    public void supprimerProduitAction(ActionEvent event) {
        if (produitSelectionne == null) {
            afficherErreur("Erreur", "Sélectionnez d'abord un produit");
            return;
        }
        supprimerProduit(produitSelectionne);
    }

    private void supprimerProduit(Produit p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le produit");
        confirm.setContentText("Voulez-vous vraiment supprimer \"" + p.getNomProduit() + "\" ?");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    produitCRUD.supprimer(p);
                    chargerProduits();
                    if (produitSelectionne != null && produitSelectionne.getIdProduit() == p.getIdProduit()) {
                        resetFormulaire();
                    }
                    afficherSucces("Produit supprimé avec succès !");
                } catch (SQLException e) {
                    afficherErreur("Erreur suppression", e.getMessage());
                }
            }
        });
    }

    private void remplirFormulaire(Produit p) {
        produitSelectionne = p;
        nomField.setText(p.getNomProduit());
        categorieField.setText(p.getCategorie() != null ? p.getCategorie() : "");
        quantiteField.setText(String.valueOf(p.getQuantiteDisponible()));
        uniteField.setText(p.getUnite() != null ? p.getUnite() : "");
        seuilField.setText(String.valueOf(p.getSeuilAlerte()));
        prixField.setText(String.valueOf(p.getPrixUnitaire()));

        if (p.getDateExpiration() != null && !p.getDateExpiration().isEmpty()) {
            try {
                datePicker.setValue(java.time.LocalDate.parse(p.getDateExpiration()));
            } catch (Exception ex) {
                datePicker.setValue(null);
            }
        } else {
            datePicker.setValue(null);
        }

        formTitleLabel.setText("Modifier : " + p.getNomProduit());
        messageLabel.setText("✎ Produit sélectionné");
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
        produitSelectionne = null;
        nomField.clear();
        categorieField.clear();
        quantiteField.clear();
        uniteField.clear();
        seuilField.clear();
        prixField.clear();
        datePicker.setValue(null);
        formTitleLabel.setText("Nouveau produit");
        messageLabel.setText("");
        btnAjouter.setDisable(false);
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        tableStock.getSelectionModel().clearSelection();

        nomField.setStyle("");
        categorieField.setStyle("");
        quantiteField.setStyle("");
        uniteField.setStyle("");
        seuilField.setStyle("");
        prixField.setStyle("");
    }

    private void configurerRecherche() {
        searchField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.trim().isEmpty()) {
                tableStock.setItems(stockList);
            } else {
                ObservableList<Produit> filtre = FXCollections.observableArrayList();
                String q = val.toLowerCase().trim();
                for (Produit p : stockList) {
                    boolean nomMatch = p.getNomProduit() != null && p.getNomProduit().toLowerCase().contains(q);
                    boolean catMatch = p.getCategorie() != null && p.getCategorie().toLowerCase().contains(q);
                    if (nomMatch || catMatch) filtre.add(p);
                }
                tableStock.setItems(filtre);
            }
        });
    }

    private boolean validerFormulaire() {
        if (nomField.getText().trim().isEmpty()) {
            afficherErreur("Validation", "Le nom est obligatoire");
            nomField.requestFocus();
            return false;
        }
        if (categorieField.getText().trim().isEmpty()) {
            afficherErreur("Validation", "La catégorie est obligatoire");
            categorieField.requestFocus();
            return false;
        }
        if (quantiteField.getText().trim().isEmpty()) {
            afficherErreur("Validation", "La quantité est obligatoire");
            quantiteField.requestFocus();
            return false;
        }
        if (uniteField.getText().trim().isEmpty()) {
            afficherErreur("Validation", "L'unité est obligatoire");
            uniteField.requestFocus();
            return false;
        }
        if (seuilField.getText().trim().isEmpty()) {
            afficherErreur("Validation", "Le seuil est obligatoire");
            seuilField.requestFocus();
            return false;
        }
        if (prixField.getText().trim().isEmpty()) {
            afficherErreur("Validation", "Le prix est obligatoire");
            prixField.requestFocus();
            return false;
        }
        return true;
    }

    @FXML
    public void showDashboard(ActionEvent e) {
        setNavActive(btnNavDashboard);
        mainTabs.setVisible(false);
    }

    @FXML
    public void showCultures(ActionEvent e) {
        setNavActive(btnNavCultures);
        mainTabs.setVisible(true);
        mainTabs.getSelectionModel().select(tabCultures);
    }

    @FXML
    public void showParcelles(ActionEvent e) {
        setNavActive(btnNavParcelles);
        mainTabs.setVisible(true);
        mainTabs.getSelectionModel().select(tabParcelles);
    }

    @FXML
    public void showStock(ActionEvent e) {
        try {
            // Vérifier si on est déjà dans le menu
            if (rootPane.getChildren().size() > 1 && rootPane.getChildren().get(1) instanceof HBox) {
                // On est dans le menu, afficher simplement l'onglet Stock
                mainTabs.setVisible(true);
                mainTabs.getSelectionModel().select(tabStock);
                chargerProduits();
                resetFormulaire();
                setNavActive(btnNavStock);
            } else {
                // On est dans une autre vue, recharger le menu
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
                StackPane menuView = loader.load();

                // Remplacer la vue actuelle par le menu
                rootPane.getChildren().clear();
                rootPane.getChildren().add(menuView);
            }
        } catch (Exception ex) {
            afficherErreur("Erreur", "Impossible de charger le stock: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    public void showMouvements(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mouvement.fxml"));
            StackPane mouvementView = loader.load();

            // Remplacer la vue actuelle
            rootPane.getChildren().clear();
            rootPane.getChildren().add(mouvementView);

        } catch (Exception ex) {
            afficherErreur("Erreur", "Impossible de charger la vue des mouvements: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    public void showUsers(ActionEvent e) {
        setNavActive(btnNavUsers);
        mainTabs.setVisible(true);
        mainTabs.getSelectionModel().select(tabUsers);
    }

    private void setNavActive(Button active) {
        Button[] navButtons = {btnNavDashboard, btnNavCultures, btnNavParcelles, btnNavStock, btnNavMouvements, btnNavUsers};
        for (Button b : navButtons) {
            b.getStyleClass().remove("nav-btn-active");
        }
        active.getStyleClass().add("nav-btn-active");
    }

    @FXML
    public void toggleTheme(ActionEvent e) {
        isDarkMode = !isDarkMode;
        appliquerTheme();
    }

    private void appliquerTheme() {
        javafx.scene.Scene scene = rootPane.getScene();
        if (scene == null) return;

        if (isDarkMode) {
            rootPane.setStyle("-fx-background-color: #0f1923;");
            themeIconLabel.setText("☀");
            themeIconLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 18px;");
        } else {
            rootPane.setStyle("-fx-background-color: #f0f4f8;");
            themeIconLabel.setText("☾");
            themeIconLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 18px;");
        }
    }

    @FXML
    public void ajouterCulture(ActionEvent e) {
        montrerInfo("Module Cultures à implémenter");
    }

    @FXML
    public void ajouterParcelle(ActionEvent e) {
        montrerInfo("Module Parcelles à implémenter");
    }

    @FXML
    public void ajouterUtilisateur(ActionEvent e) {
        montrerInfo("Module Utilisateurs à implémenter");
    }

    private void afficherSucces(String msg) {
        messageLabel.setText("✓ " + msg);
        messageLabel.setStyle("-fx-text-fill: #22c55e;");
    }

    private void afficherErreur(String titre, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void montrerInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}