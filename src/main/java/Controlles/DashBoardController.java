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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
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
    @FXML private StackPane rootPane;
    @FXML private Button btnThemeToggle;
    @FXML private Label themeIconLabel;

    private final CultureCRUD cc = new CultureCRUD();
    private final ParcelleCRUD pc = new ParcelleCRUD();
    private final Image cultureIcon = loadImage("/image/cultures_icon.png");
    private final Image parcelleIcon = loadImage("/image/parcelles_icon.png");
    private ObservableList<Culture> masterCultures = FXCollections.observableArrayList();
    private ObservableList<Parcelle> masterParcelles = FXCollections.observableArrayList();
    private boolean darkModeEnabled = false;
    private static boolean preferredDarkMode = false;

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

        setupCultureColumns();
        setupParcelleColumns();
        setupCultureActions();
        setupParcelleActions();
        setupFiltering();
        setupTabSync();
        refreshTable();
        darkModeEnabled = preferredDarkMode;
        applyTheme();
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
            applyThemeToRoot(root);
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
            private final Button btnSup = createIconButton(
                    "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zm3.5-9h1v8h-1v-8zm4 0h1v8h-1v-8zM15.5 4l-1-1h-5l-1 1H5v2h14V4z",
                    "table-icon-danger"
            );
            private final Button btnMod = createIconButton(
                    "M3 17.25V21h3.75l11-11.03-3.75-3.75L3 17.25zm14.71-9.04a1.003 1.003 0 0 0 0-1.42L15.21 4.79a1.003 1.003 0 0 0-1.42 0l-1.83 1.83 3.75 3.75 1.5-1.46z",
                    "table-icon-edit"
            );
            private final HBox container = new HBox(btnMod, btnSup);
            {
                container.setSpacing(8);
                container.setAlignment(Pos.CENTER);
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
            applyThemeToRoot(root);
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
            private final Button btnSup = createIconButton(
                    "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zm3.5-9h1v8h-1v-8zm4 0h1v8h-1v-8zM15.5 4l-1-1h-5l-1 1H5v2h14V4z",
                    "table-icon-danger"
            );
            private final Button btnMod = createIconButton(
                    "M3 17.25V21h3.75l11-11.03-3.75-3.75L3 17.25zm14.71-9.04a1.003 1.003 0 0 0 0-1.42L15.21 4.79a1.003 1.003 0 0 0-1.42 0l-1.83 1.83 3.75 3.75 1.5-1.46z",
                    "table-icon-edit"
            );
            private final HBox container = new HBox(btnMod, btnSup);
            {
                container.setSpacing(8);
                container.setAlignment(Pos.CENTER);
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

    private void setupParcelleColumns() {
        colPNom.setCellFactory(param -> new TableCell<>() {
            private final StackPane iconWrap = new StackPane();
            private final ImageView iconImage = createTableIconView(parcelleIcon);
            private final Label nameLabel = new Label();
            private final HBox content = new HBox(iconWrap, nameLabel);
            {
                iconWrap.getStyleClass().addAll("crop-icon-wrap", "parcel-icon-wrap");
                iconImage.getStyleClass().add("row-icon-image");
                iconWrap.getChildren().add(iconImage);

                nameLabel.getStyleClass().add("table-name-label");
                content.setSpacing(10);
                content.setAlignment(Pos.CENTER_LEFT);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(item);
                    setGraphic(content);
                }
            }
        });

        colPType.setCellFactory(param -> new TableCell<>() {
            private final Label badge = new Label();
            {
                badge.getStyleClass().add("status-pill");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                badge.setText("\u2022 " + item);
                badge.getStyleClass().removeAll(
                        "status-pill-done",
                        "status-pill-harvest",
                        "status-pill-growth",
                        "status-pill-default"
                );
                String value = item.toLowerCase(Locale.ROOT);
                if (value.contains("arg") || value.contains("limon")) {
                    badge.getStyleClass().add("status-pill-done");
                } else if (value.contains("sable")) {
                    badge.getStyleClass().add("status-pill-harvest");
                } else if (value.contains("calc") || value.contains("hum")) {
                    badge.getStyleClass().add("status-pill-growth");
                } else {
                    badge.getStyleClass().add("status-pill-default");
                }
                setGraphic(badge);
            }
        });
    }

    private void setupCultureColumns() {
        colNom.setCellFactory(param -> new TableCell<>() {
            private final StackPane iconWrap = new StackPane();
            private final ImageView iconImage = createTableIconView(cultureIcon);
            private final Label nameLabel = new Label();
            private final HBox content = new HBox(iconWrap, nameLabel);
            {
                iconWrap.getStyleClass().add("crop-icon-wrap");
                iconImage.getStyleClass().add("row-icon-image");
                iconWrap.getChildren().add(iconImage);

                nameLabel.getStyleClass().add("table-name-label");
                content.setSpacing(10);
                content.setAlignment(Pos.CENTER_LEFT);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(item);
                    setGraphic(content);
                }
            }
        });

        colEtat.setCellFactory(param -> new TableCell<>() {
            private final Label badge = new Label();
            {
                badge.getStyleClass().add("status-pill");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                badge.setText("\u2022 " + item);
                badge.getStyleClass().removeAll(
                        "status-pill-done",
                        "status-pill-harvest",
                        "status-pill-growth",
                        "status-pill-default"
                );
                String value = item.toLowerCase(Locale.ROOT);
                if (value.contains("terminee") || value.contains("termine")) {
                    badge.getStyleClass().add("status-pill-done");
                } else if (value.contains("recolte")) {
                    badge.getStyleClass().add("status-pill-harvest");
                } else if (value.contains("croissance")) {
                    badge.getStyleClass().add("status-pill-growth");
                } else {
                    badge.getStyleClass().add("status-pill-default");
                }
                setGraphic(badge);
            }
        });
    }

    private Button createIconButton(String path, String styleClass) {
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.getStyleClass().add("table-icon-glyph");
        Button button = new Button();
        button.getStyleClass().addAll("table-icon-button", styleClass);
        button.setGraphic(icon);
        return button;
    }

    private Image loadImage(String resourcePath) {
        var resource = getClass().getResource(resourcePath);
        if (resource == null) {
            return null;
        }
        return new Image(resource.toExternalForm(), true);
    }

    private ImageView createTableIconView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(16);
        imageView.setFitHeight(16);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
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
            applyThemeToRoot(root);
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

    @FXML
    private void toggleTheme() {
        darkModeEnabled = !darkModeEnabled;
        applyTheme();
    }

    private void applyTheme() {
        preferredDarkMode = darkModeEnabled;
        if (rootPane != null) {
            rootPane.getStyleClass().remove("theme-dark");
            if (darkModeEnabled) {
                rootPane.getStyleClass().add("theme-dark");
            }
        }
        if (themeIconLabel != null) {
            if (darkModeEnabled) {
                themeIconLabel.setText("☀");
            } else {
                themeIconLabel.setText("☾");
            }
        }
        if (btnThemeToggle != null) {
            btnThemeToggle.setTooltip(new Tooltip(darkModeEnabled ? "Switch to day mode" : "Switch to night mode"));
        }
    }

    private void applyThemeToRoot(Parent root) {
        if (root == null) {
            return;
        }
        root.getStyleClass().remove("theme-dark");
        if (darkModeEnabled) {
            root.getStyleClass().add("theme-dark");
        }
    }
}
