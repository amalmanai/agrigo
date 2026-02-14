package Controlles;

import Entites.Culture;
import Entites.Parcelle;
import Services.CultureCRUD;
import Services.ParcelleCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class DashBoardController {

    @FXML private TableView<Culture> tableCultures;
    @FXML private TableColumn<Culture, Integer> colId;
    @FXML private TableColumn<Culture, String> colNom;
    @FXML private TableColumn<Culture, String> colEtat;
    @FXML private TableColumn<Culture, Double> colRendement;
    @FXML private TableColumn<Culture, Void> colActions;

    @FXML private TableView<Parcelle> tableParcelles;
    @FXML private TableColumn<Parcelle, Integer> colPId;
    @FXML private TableColumn<Parcelle, String> colPNom;
    @FXML private TableColumn<Parcelle, Double> colPSurface;
    @FXML private TableColumn<Parcelle, String> colPType;
    @FXML private TableColumn<Parcelle, Void> colPActions;
    @FXML private TabPane mainTabs;
    @FXML private Tab tabCultures;
    @FXML private Tab tabParcelles;
    @FXML private TextField searchField;
    @FXML private Label brandTitle;
    @FXML private Button btnAddItem;
    @FXML private Label btnAddItemLabel;
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavCultures;
    @FXML private Button btnNavParcelles;
    @FXML private Button btnNavAnalytics;

    private final CultureCRUD cc = new CultureCRUD();
    private final ParcelleCRUD pc = new ParcelleCRUD();
    private ObservableList<Culture> masterCultures = FXCollections.observableArrayList();
    private ObservableList<Parcelle> masterParcelles = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));
        colRendement.setCellValueFactory(new PropertyValueFactory<>("rendement"));

        colPId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPSurface.setCellValueFactory(new PropertyValueFactory<>("surface"));
        colPType.setCellValueFactory(new PropertyValueFactory<>("typeSol"));

        setupCultureActions();
        setupParcelleActions();
        setupFiltering();
        setupTabSync();
        refreshTable();
    }

    @FXML
    void ajouterCulture() {
        // Corrected: Path starts from the root of resources, no "Resources" folder name
        openWindow("/page1.fxml", "Ajouter une Culture");
    }

    @FXML
    void ajouterParcelle() {
        // Corrected: Path starts from the root of resources
        openWindow("/pageParcelle.fxml", "Ajouter une Parcelle");
    }

    @FXML
    void showDashboard() {
        selectTab(tabCultures);
        setActiveNav(btnNavDashboard);
    }

    @FXML
    void showCultures() {
        selectTab(tabCultures);
        setActiveNav(btnNavCultures);
    }

    @FXML
    void showParcelles() {
        selectTab(tabParcelles);
        setActiveNav(btnNavParcelles);
    }

    @FXML
    void showAnalytics() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Analytique");
        alert.setHeaderText("Bientot disponible");
        alert.setContentText("Cette section sera ajoutee dans une prochaine version.");
        alert.showAndWait();
        setActiveNav(btnNavAnalytics);
    }

    /**
     * Generic method to open a popup window
     */
    private void openWindow(String fxmlPath, String title) {
        try {
            // getClass().getResource() looks into the target/classes folder (the root of resources)
            var resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("ERROR: FXML file not found at " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            System.err.println("Could not load FXML at: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void setupCultureActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnSup = new Button("Supprimer");
            private final Button btnMod = new Button("Modifier");
            private final HBox container = new HBox(btnMod, btnSup);
            {
                container.setSpacing(10);
                btnSup.getStyleClass().addAll("table-action", "table-action-danger");
                btnMod.getStyleClass().addAll("table-action", "table-action-primary");
                btnSup.setOnAction(e -> {
                    Culture selected = getTableView().getItems().get(getIndex());
                    if (confirmDelete(selected.getNom())) {
                        try {
                            cc.supprimer(selected.getId());
                            refreshTable();
                        } catch (SQLException ex) { ex.printStackTrace(); }
                    }
                });
                btnMod.setOnAction(e -> {
                    int index = getIndex();
                    if (index < 0 || index >= getTableView().getItems().size()) {
                        return;
                    }
                    Culture selected = getTableView().getItems().get(index);
                    if (selected == null) {
                        JOptionPane.showMessageDialog(null, "Veuillez selectionner une culture.");
                        return;
                    }
                    openCultureEditor(selected);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void openCultureEditor(Culture culture) {
        if (culture == null) {
            return;
        }
        try {
            var resource = getClass().getResource("/page1.fxml");
            if (resource == null) {
                System.err.println("ERROR: FXML file not found at /page1.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Page1Controller controller = loader.getController();
            controller.setCultureForEdit(culture);

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            stage.setTitle("Modifier une Culture");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            System.err.println("Could not load FXML at: /page1.fxml");
            e.printStackTrace();
        }
    }

    private void setupParcelleActions() {
        colPActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnSup = new Button("Supprimer");
            private final Button btnMod = new Button("Modifier");
            private final HBox container = new HBox(btnMod, btnSup);
            {
                container.setSpacing(10);
                btnSup.getStyleClass().addAll("table-action", "table-action-danger");
                btnMod.getStyleClass().addAll("table-action", "table-action-primary");
                btnSup.setOnAction(e -> {
                    Parcelle selected = getTableView().getItems().get(getIndex());
                    if (confirmDelete(selected.getNom())) {
                        try {
                            pc.supprimer(selected.getId());
                            refreshTable();
                        } catch (SQLException ex) { ex.printStackTrace(); }
                    }
                });
                btnMod.setOnAction(e -> {
                    int index = getIndex();
                    if (index < 0 || index >= getTableView().getItems().size()) {
                        return;
                    }
                    Parcelle selected = getTableView().getItems().get(index);
                    if (selected == null) {
                        JOptionPane.showMessageDialog(null, "Veuillez selectionner une parcelle.");
                        return;
                    }
                    openParcelleEditor(selected);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void openParcelleEditor(Parcelle parcelle) {
        if (parcelle == null) {
            return;
        }
        try {
            var resource = getClass().getResource("/pageParcelle.fxml");
            if (resource == null) {
                System.err.println("ERROR: FXML file not found at /pageParcelle.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            ParcelleController controller = loader.getController();
            controller.setParcelleForEdit(parcelle);

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            stage.setTitle("Modifier une Parcelle");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            System.err.println("Could not load FXML at: /pageParcelle.fxml");
            e.printStackTrace();
        }
    }

    private boolean confirmDelete(String name) {
        return JOptionPane.showConfirmDialog(null, "Supprimer " + name + "?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public void refreshTable() {
        try {
            masterCultures = FXCollections.observableArrayList(cc.afficher());
            masterParcelles = FXCollections.observableArrayList(pc.afficher());
            applyFiltering();
            System.out.println("DEBUG: UI Refreshed.");
        } catch (SQLException e) {
            System.err.println("DB Load Error: " + e.getMessage());
        }
    }

    private void applyStylesheet(Scene scene) {
        var css = getClass().getResource("/app.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }

    private void setupFiltering() {
        if (searchField == null) {
            return;
        }
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltering());
    }

    private void applyFiltering() {
        String query = searchField == null ? "" : searchField.getText();
        String filter = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        boolean filterParcelles = isParcellesSelected();

        FilteredList<Culture> filteredCultures = new FilteredList<>(masterCultures, culture -> {
            if (filterParcelles) {
                return true;
            }
            if (filter.isEmpty()) {
                return true;
            }
            return containsIgnoreCase(culture.getNom(), filter)
                    || containsIgnoreCase(culture.getEtat(), filter)
                    || containsIgnoreCase(String.valueOf(culture.getRendement()), filter);
        });

        FilteredList<Parcelle> filteredParcelles = new FilteredList<>(masterParcelles, parcelle -> {
            if (!filterParcelles) {
                return true;
            }
            if (filter.isEmpty()) {
                return true;
            }
            return containsIgnoreCase(parcelle.getNom(), filter)
                    || containsIgnoreCase(parcelle.getTypeSol(), filter)
                    || containsIgnoreCase(String.valueOf(parcelle.getSurface()), filter);
        });

        SortedList<Culture> sortedCultures = new SortedList<>(filteredCultures);
        sortedCultures.comparatorProperty().bind(tableCultures.comparatorProperty());
        tableCultures.setItems(sortedCultures);

        SortedList<Parcelle> sortedParcelles = new SortedList<>(filteredParcelles);
        sortedParcelles.comparatorProperty().bind(tableParcelles.comparatorProperty());
        tableParcelles.setItems(sortedParcelles);
    }

    @FXML
    void handleAddAction() {
        if (isParcellesSelected()) {
            ajouterParcelle();
        } else {
            ajouterCulture();
        }
    }

    private void setupTabSync() {
        if (mainTabs == null) {
            return;
        }
        mainTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            updateHeaderForTab(newTab);
            applyFiltering();
        });
        updateHeaderForTab(mainTabs.getSelectionModel().getSelectedItem());
    }

    private void updateHeaderForTab(Tab selectedTab) {
        boolean parcelles = selectedTab == tabParcelles;
        if (brandTitle != null) {
            brandTitle.setText(parcelles ? "Parcelles" : "Cultures");
        }
        if (searchField != null) {
            searchField.setPromptText(parcelles ? "Rechercher une parcelle..." : "Rechercher une culture...");
        }
        if (btnAddItemLabel != null) {
            btnAddItemLabel.setText(parcelles ? "Nouvelle parcelle" : "Nouvelle culture");
        }
    }

    private boolean isParcellesSelected() {
        if (mainTabs == null) {
            return false;
        }
        return mainTabs.getSelectionModel().getSelectedItem() == tabParcelles;
    }

    private boolean containsIgnoreCase(String value, String filter) {
        if (value == null) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(filter);
    }

    private void selectTab(Tab tab) {
        if (mainTabs != null && tab != null) {
            mainTabs.getSelectionModel().select(tab);
        }
    }

    private void setActiveNav(Button active) {
        Button[] buttons = { btnNavDashboard, btnNavCultures, btnNavParcelles, btnNavAnalytics };
        for (Button button : buttons) {
            if (button == null) {
                continue;
            }
            button.getStyleClass().remove("nav-button-active");
            if (button == active) {
                button.getStyleClass().add("nav-button-active");
            }
        }
    }
}
