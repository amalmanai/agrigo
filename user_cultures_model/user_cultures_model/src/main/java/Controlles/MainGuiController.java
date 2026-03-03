package Controlles;

import Entites.Alerte;
import Entites.Culture;
import Entites.Parcelle;
import Entites.User;
import Services.AIPredictionEngine;
import Services.AlerteCRUD;
import Services.CultureCRUD;
import Services.ParcelleCRUD;
import Services.ServiceTache;
import Services.WeatherService;
import Services.YieldPredictionService;
import Utils.Session;
import Controllers.ListeTacheController;
import Controllers.ModifierUserController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import java.util.Map;

public class MainGuiController {

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
    @FXML private Tab tabDashboard;
    @FXML private Tab tabCultures;
    @FXML private Tab tabParcelles;
    @FXML private Tab tabIrrigationHistorique;
    @FXML private Tab tabIrrigationSystemes;
    @FXML private Tab tabIrrigationRapports;
    @FXML private StackPane irrigationHistoriquePlaceholder;
    @FXML private StackPane irrigationSystemesPlaceholder;
    @FXML private StackPane irrigationRapportsPlaceholder;
    @FXML private Tab tabAnalytics;

    private IrrigationDashBoardController irrigationController;
    private Parent irrigationRoot;

    @FXML private TableView<Alerte> tableAlertes;
    @FXML private TableColumn<Alerte, Integer> colAId;
    @FXML private TableColumn<Alerte, java.sql.Timestamp> colADate;
    @FXML private TableColumn<Alerte, String> colAType;
    @FXML private TableColumn<Alerte, String> colADesc;
    @FXML private TableColumn<Alerte, String> colACulture;
    @FXML private TextField searchField;
    @FXML private Label brandTitle;
    @FXML private StackPane rootPane;
    @FXML private Button btnThemeToggle;
    @FXML private ImageView themeIconImage;
    @FXML private HBox globalStatsContainer;

    // User-style topbar controls (profile menu + logout)
    @FXML private MenuButton btnProfil;
    @FXML private Button logoutuser;

    // Analytics & weather tab UI
    @FXML private Label lblTotalCultures;
    @FXML private Label lblTotalParcelles;
    @FXML private Label lblTotalAlertes;
    @FXML private StackPane weatherRiskCard;
    @FXML private Label lblWeatherRiskTitle;
    @FXML private Label lblWeatherRiskDetail;
    @FXML private Label lblAdvancedSummary;
    @FXML private ComboBox<Parcelle> comboParcelleSelect;

    @FXML private TableView<YieldPredictionService.PredictionResult> tablePredictions;
    @FXML private TableColumn<YieldPredictionService.PredictionResult, String> colPredParcelle;
    @FXML private TableColumn<YieldPredictionService.PredictionResult, String> colPredCulture;
    @FXML private TableColumn<YieldPredictionService.PredictionResult, Double> colPredBase;
    @FXML private TableColumn<YieldPredictionService.PredictionResult, Double> colPredPrevu;
    @FXML private TableColumn<YieldPredictionService.PredictionResult, Double> colPredImpact;
    @FXML private TableColumn<YieldPredictionService.PredictionResult, Integer> colPredConfidence;
    @FXML private TableColumn<YieldPredictionService.PredictionResult, String> colPredRecommendation;

    private final CultureCRUD cc = new CultureCRUD();
    private final ParcelleCRUD pc = new ParcelleCRUD();
    private final AlerteCRUD ac = new AlerteCRUD();
    private final WeatherService weatherService = new WeatherService();
    private final Image cultureIcon = loadImage("/image/cultures_icon.png");
    private final Image parcelleIcon = loadImage("/image/parcelles_icon.png");
    private ObservableList<Culture> masterCultures = FXCollections.observableArrayList();
    private ObservableList<Parcelle> masterParcelles = FXCollections.observableArrayList();
    private ObservableList<Alerte> masterAlertes = FXCollections.observableArrayList();
    private boolean darkModeEnabled = false;
    public static boolean preferredDarkMode = false;

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
        setupAlerteColumns();
        setupCultureActions();
        setupParcelleActions();
        setupPredictionColumns();
        setupFiltering();
        setupTabSync();
        refreshTable();
        darkModeEnabled = preferredDarkMode;
        applyTheme();
    }

    @FXML
    void ajouterCulture() {
        openWindow("/page1.fxml", "Ajouter une Culture");
    }

    @FXML
    void ajouterParcelle() {
        openWindow("/pageParcelle.fxml", "Ajouter une Parcelle");
    }

    @FXML
    void ajouterTache() {
        openWindow("/Tache/AjouterTache.fxml", "Ajouter une Tâche");
    }

    @FXML
    void showDashboard() {
        selectTab(tabDashboard);
    }

    @FXML
    void showCultures() {
        selectTab(tabCultures);
    }

    @FXML
    void showParcelles() {
        selectTab(tabParcelles);
    }

    @FXML
    void showHistoriqueIrrigation() {
        loadAndAttachIrrigationIfNeeded(irrigationHistoriquePlaceholder);
        selectTab(tabIrrigationHistorique);
        if (irrigationController != null) irrigationController.showHistorique();
    }

    @FXML
    void showSystemesIrrigation() {
        loadAndAttachIrrigationIfNeeded(irrigationSystemesPlaceholder);
        selectTab(tabIrrigationSystemes);
        if (irrigationController != null) irrigationController.showSystemes();
    }

    @FXML
    void showRapportsIrrigation() {
        loadAndAttachIrrigationIfNeeded(irrigationRapportsPlaceholder);
        selectTab(tabIrrigationRapports);
        if (irrigationController != null) irrigationController.showRapports();
    }

    private void loadAndAttachIrrigationIfNeeded(StackPane target) {
        if (target == null) return;
        try {
            if (irrigationController == null || irrigationRoot == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/irrigationContent.fxml"));
                irrigationRoot = loader.load();
                irrigationController = loader.getController();
            }

            if (irrigationRoot != null) {
                if (irrigationRoot.getParent() instanceof javafx.scene.layout.Pane pane) {
                    pane.getChildren().remove(irrigationRoot);
                }
                target.getChildren().setAll(irrigationRoot);
            }
        } catch (IOException e) {
            System.err.println("Erreur chargement irrigation: " + e.getMessage());
        }
    }

    @FXML
    void showAnalytics() {
        selectTab(tabAnalytics);
        refreshAnalytics();
    }

    private void openWindow(String fxmlPath, String title) {
        try {
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

    private void setupAlerteColumns() {
        colAId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colADate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colAType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colADesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colACulture.setCellValueFactory(param -> {
            int cId = param.getValue().getIdCulture();
            String cName = "Inconnue (" + cId + ")";
            for (Culture c : masterCultures) {
                if (c.getId() == cId) {
                    cName = c.getNom();
                    break;
                }
            }
            return new SimpleStringProperty(cName);
        });

        colAType.setCellFactory(param -> new TableCell<>() {
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
                        "status-pill-done", "status-pill-harvest",
                        "status-pill-growth", "status-pill-default",
                        "risk-pill-red", "risk-pill-yellow", "risk-pill-green"
                );

                String lower = item.toLowerCase(Locale.ROOT);
                if (lower.contains("red") || lower.contains("rouge") || lower.contains("critique")) {
                    badge.getStyleClass().add("risk-pill-red");
                } else if (lower.contains("yellow") || lower.contains("jaune") || lower.contains("surveillance")) {
                    badge.getStyleClass().add("risk-pill-yellow");
                } else if (lower.contains("green") || lower.contains("vert") || lower.contains("faible")) {
                    badge.getStyleClass().add("risk-pill-green");
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
            masterAlertes = FXCollections.observableArrayList(ac.afficher());
            applyFiltering();

            if (tableAlertes != null) {
                SortedList<Alerte> sortedAlertes = new SortedList<>(masterAlertes);
                sortedAlertes.comparatorProperty().bind(tableAlertes.comparatorProperty());
                tableAlertes.setItems(sortedAlertes);
            }

            System.out.println("DEBUG: UI Refreshed.");
        } catch (SQLException e) {
            System.err.println("DB Load Error: " + e.getMessage());
        }
    }

    private void setupPredictionColumns() {
        if (colPredParcelle == null) return;

        colPredParcelle.setCellValueFactory(data -> {
            Parcelle p = data.getValue().getParcelle();
            return new SimpleStringProperty(p != null ? p.getNom() : "N/A");
        });

        colPredCulture.setCellValueFactory(data -> {
            Culture c = data.getValue().getCulture();
            return new SimpleStringProperty(c != null ? c.getNom() : "N/A");
        });

        colPredBase.setCellValueFactory(new PropertyValueFactory<>("rendementBase"));
        colPredPrevu.setCellValueFactory(new PropertyValueFactory<>("rendementPrevu"));

        colPredImpact.setCellValueFactory(new PropertyValueFactory<>("impactPercent"));
        colPredImpact.setCellFactory(column -> new TableCell<YieldPredictionService.PredictionResult, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label();
                    badge.getStyleClass().add("status-pill");
                    if (item > 0) {
                        badge.setText("+" + item + "% ↗");
                        badge.getStyleClass().add("impact-positive");
                    } else if (item < 0) {
                        badge.setText(item + "% ↘");
                        badge.getStyleClass().add("impact-negative");
                    } else {
                        badge.setText("0% ➔");
                        badge.getStyleClass().add("status-pill-default");
                    }
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });

        if (colPredConfidence != null) {
            colPredConfidence.setCellValueFactory(new PropertyValueFactory<>("confidenceScore"));
            colPredConfidence.setCellFactory(column -> new TableCell<YieldPredictionService.PredictionResult, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Label badge = new Label();
                        badge.getStyleClass().add("ai-confidence-badge");
                        badge.setText("CIA: " + item + "%");
                        setGraphic(badge);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    }
                }
            });
        }

        if (colPredRecommendation != null) {
            colPredRecommendation.setCellValueFactory(new PropertyValueFactory<>("recommendation"));
            colPredRecommendation.setCellFactory(column -> new TableCell<YieldPredictionService.PredictionResult, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        getStyleClass().add("ai-recommendation-text");
                    }
                }
            });
        }
    }

    private void refreshAnalytics() {
        try {
            if (masterCultures == null || masterCultures.isEmpty()) {
                masterCultures = FXCollections.observableArrayList(cc.afficher());
            }
            if (masterParcelles == null || masterParcelles.isEmpty()) {
                masterParcelles = FXCollections.observableArrayList(pc.afficher());
            }

            List<Alerte> alertes = ac.afficher();
            WeatherService.RiskSummary weatherSummary = weatherService.computeRiskForParcelles(masterParcelles);

            updateBasicAnalytics(alertes);

            YieldPredictionService predictionService = new YieldPredictionService();
            List<YieldPredictionService.PredictionResult> predictions =
                    predictionService.predictYields(masterCultures, masterParcelles, weatherSummary);

            if (tablePredictions != null) {
                tablePredictions.setItems(FXCollections.observableArrayList(predictions));
            }

            if (comboParcelleSelect != null) {
                Parcelle currentSelection = comboParcelleSelect.getValue();
                comboParcelleSelect.setItems(masterParcelles);

                comboParcelleSelect.setOnAction(e -> {
                    updateWeatherAnalyticsForSelection(weatherSummary);
                });

                if (currentSelection != null && masterParcelles.contains(currentSelection)) {
                    comboParcelleSelect.setValue(currentSelection);
                } else if (!masterParcelles.isEmpty() && comboParcelleSelect.getValue() == null) {
                    comboParcelleSelect.setValue(masterParcelles.get(0));
                }
            }

            updateWeatherAnalyticsForSelection(weatherSummary);
            updateAdvancedAnalytics(alertes, weatherSummary);
        } catch (SQLException e) {
            System.err.println("Erreur lors du rafraichissement des analytiques: " + e.getMessage());
        }
    }

    private void updateBasicAnalytics(List<Alerte> alertes) {
        if (lblTotalCultures != null) {
            lblTotalCultures.setText(String.valueOf(masterCultures != null ? masterCultures.size() : 0));
        }
        if (lblTotalParcelles != null) {
            lblTotalParcelles.setText(String.valueOf(masterParcelles != null ? masterParcelles.size() : 0));
        }
        if (lblTotalAlertes != null) {
            int count = alertes != null ? alertes.size() : 0;
            lblTotalAlertes.setText(String.valueOf(count));
        }
    }

    private void updateWeatherAnalyticsForSelection(WeatherService.RiskSummary summary) {
        if (weatherRiskCard == null || summary == null) {
            return;
        }

        Parcelle selected = comboParcelleSelect != null ? comboParcelleSelect.getValue() : null;

        WeatherService.RiskLevel displayLevel = summary.getLevel();
        String displayTitle = "Niveau global : " + displayLevel.getColorName() + " \u2014 " + displayLevel.getLabel();
        String displayMsg = summary.getMessage();

        if (selected != null && summary.getParcelleLevels() != null) {
            Map<Integer, WeatherService.RiskLevel> levels = summary.getParcelleLevels();
            WeatherService.RiskLevel pLevel = levels.get(selected.getId());
            if (pLevel != null) {
                displayLevel = pLevel;
                displayTitle = "Niveau pour la parcelle \"" + selected.getNom() + "\" : " + displayLevel.getColorName() + " \u2014 " + displayLevel.getLabel();
                displayMsg = switch (pLevel) {
                    case GREEN -> "Conditions météo optimales prévues pour la parcelle \"" + selected.getNom() + "\".";
                    case YELLOW -> "De légers risques météo sont prévus pour la parcelle \"" + selected.getNom() + "\" (pluie ou vent modéré).";
                    case RED -> "Risques météo importants sur la parcelle \"" + selected.getNom() + "\". Des mesures préventives sont recommandées.";
                    case DARK_RED -> "ALERTE INTEMPÉRIES MAJEURES sur la parcelle \"" + selected.getNom() + "\". Dégâts potentiels imminents.";
                };
            }
        }

        weatherRiskCard.getStyleClass().removeAll(
                "risk-level-green",
                "risk-level-yellow",
                "risk-level-red",
                "risk-level-dark-red"
        );
        switch (displayLevel) {
            case GREEN -> weatherRiskCard.getStyleClass().add("risk-level-green");
            case YELLOW -> weatherRiskCard.getStyleClass().add("risk-level-yellow");
            case RED -> weatherRiskCard.getStyleClass().add("risk-level-red");
            case DARK_RED -> weatherRiskCard.getStyleClass().add("risk-level-dark-red");
        }

        if (lblWeatherRiskTitle != null) {
            lblWeatherRiskTitle.setText(displayTitle);
        }
        if (lblWeatherRiskDetail != null) {
            lblWeatherRiskDetail.setText(displayMsg);
        }
    }

    private void updateAdvancedAnalytics(List<Alerte> alertes, WeatherService.RiskSummary weatherSummary) {
        if (lblAdvancedSummary == null) {
            return;
        }
        if (alertes == null || alertes.isEmpty()) {
            String weatherPart;
            if (weatherSummary == null) {
                weatherPart = "Analyse météo indisponible (vérifiez votre connexion Internet).";
            } else {
                weatherPart = "Niveau météo actuel : "
                        + weatherSummary.getLevel().getColorName()
                        + " \u2014 " + weatherSummary.getLevel().getLabel() + ".";
            }
            lblAdvancedSummary.setText(
                    "Aucune alerte historique enregistrée pour le moment.\n"
                            + weatherPart + "\n"
                            + "Le Système Intelligent d'Alerte des Risques Agricoles déclenchera automatiquement des alertes "
                            + "dès qu'un risque météo élevé ou critique sera détecté sur une parcelle."
            );
            return;
        }

        int totalAlertes = alertes.size();

        String dominantType = null;
        int dominantCount = 0;
        java.util.Map<String, Integer> typeCounts = new java.util.HashMap<>();
        for (Alerte al : alertes) {
            if (al == null || al.getType() == null) {
                continue;
            }
            String key = al.getType();
            int newCount = typeCounts.getOrDefault(key, 0) + 1;
            typeCounts.put(key, newCount);
            if (newCount > dominantCount) {
                dominantCount = newCount;
                dominantType = key;
            }
        }

        java.util.Map<Integer, Integer> parcelleAlertCounts = new java.util.HashMap<>();
        if (masterCultures != null) {
            java.util.Map<Integer, Integer> cultureToParcelle = new java.util.HashMap<>();
            for (Culture culture : masterCultures) {
                cultureToParcelle.put(culture.getId(), culture.getIdParcelle());
            }

            for (Alerte al : alertes) {
                Integer parcelleId = cultureToParcelle.get(al.getIdCulture());
                if (parcelleId == null) {
                    continue;
                }
                int newCount = parcelleAlertCounts.getOrDefault(parcelleId, 0) + 1;
                parcelleAlertCounts.put(parcelleId, newCount);
            }
        }

        String hotspotLabel = "aucune zone critique identifiée";
        if (!parcelleAlertCounts.isEmpty() && masterParcelles != null) {
            int bestParcelleId = -1;
            int bestCount = 0;
            for (java.util.Map.Entry<Integer, Integer> entry : parcelleAlertCounts.entrySet()) {
                if (entry.getValue() > bestCount) {
                    bestCount = entry.getValue();
                    bestParcelleId = entry.getKey();
                }
            }

            String parcelleName = null;
            for (Parcelle parcelle : masterParcelles) {
                if (parcelle.getId() == bestParcelleId) {
                    parcelleName = parcelle.getNom();
                    break;
                }
            }

            if (parcelleName != null) {
                hotspotLabel = "zone la plus sensible : parcelle \"" + parcelleName + "\" (" + bestCount + " alertes)";
            }
        }

        String typePart = dominantType == null
                ? "typologie principale d'alerte inconnue"
                : "type dominant : " + dominantType + " (" + dominantCount + " alertes)";

        int stableCount = 0;
        int watchCount = 0;
        int criticalCount = 0;
        if (weatherSummary != null && weatherSummary.getParcelleLevels() != null) {
            for (WeatherService.RiskLevel level : weatherSummary.getParcelleLevels().values()) {
                if (level == null) continue;
                switch (level) {
                    case GREEN -> stableCount++;
                    case YELLOW -> watchCount++;
                    case RED, DARK_RED -> criticalCount++;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Système Intelligent d'Alerte des Risques Agricoles\n\n");
        sb.append("• Historique : ").append(totalAlertes).append(" alerte(s) enregistrée(s).\n");
        sb.append("• Profil de risque : ").append(typePart).append(".\n");
        sb.append("• Cartographie : ").append(hotspotLabel).append(".\n");

        if (weatherSummary != null) {
            sb.append("• Prévision 24–48h : niveau global ")
                    .append(weatherSummary.getLevel().getColorName())
                    .append(" — ")
                    .append(weatherSummary.getLevel().getLabel())
                    .append(".");
        }

        lblAdvancedSummary.setText(sb.toString());
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
            if (newTab == tabAnalytics) {
                refreshAnalytics();
            }
            updateGlobalStatsVisibilityForTab(newTab);
            if (newTab == tabIrrigationHistorique) {
                loadAndAttachIrrigationIfNeeded(irrigationHistoriquePlaceholder);
                if (irrigationController != null) irrigationController.showHistorique();
            } else if (newTab == tabIrrigationSystemes) {
                loadAndAttachIrrigationIfNeeded(irrigationSystemesPlaceholder);
                if (irrigationController != null) irrigationController.showSystemes();
            } else if (newTab == tabIrrigationRapports) {
                loadAndAttachIrrigationIfNeeded(irrigationRapportsPlaceholder);
                if (irrigationController != null) irrigationController.showRapports();
            }
        });
        updateHeaderForTab(mainTabs.getSelectionModel().getSelectedItem());
        updateGlobalStatsVisibilityForTab(mainTabs.getSelectionModel().getSelectedItem());
        if (mainTabs.getSelectionModel().getSelectedItem() == tabAnalytics) {
            refreshAnalytics();
        }
    }

    private void updateGlobalStatsVisibilityForTab(Tab selectedTab) {
        if (globalStatsContainer == null) return;
        boolean irrigation = selectedTab == tabIrrigationHistorique
                || selectedTab == tabIrrigationSystemes
                || selectedTab == tabIrrigationRapports;
        globalStatsContainer.setVisible(!irrigation);
        globalStatsContainer.setManaged(!irrigation);
    }

    private void updateHeaderForTab(Tab selectedTab) {
        boolean parcelles = selectedTab == tabParcelles;
        boolean analytics = selectedTab == tabAnalytics;
        boolean irrigation = selectedTab == tabIrrigationHistorique
                || selectedTab == tabIrrigationSystemes
                || selectedTab == tabIrrigationRapports;

        if (brandTitle != null) {
            if (analytics) {
                brandTitle.setText("Analytique");
            } else if (parcelles) {
                brandTitle.setText("Parcelles");
            } else if (irrigation) {
                brandTitle.setText("Irrigation");
            } else {
                brandTitle.setText("Cultures");
            }
        }
        if (searchField != null) {
            if (analytics) {
                searchField.setPromptText("Filtrer les données (désactivé en mode analytique)");
                searchField.clear();
                searchField.setDisable(true);
            } else if (irrigation) {
                searchField.setDisable(false);
                searchField.setPromptText("Rechercher une irrigation...");
            } else {
                searchField.setDisable(false);
                searchField.setPromptText(parcelles ? "Rechercher une parcelle..." : "Rechercher une culture...");
            }
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

    // Theme images
    private final Image dayImage = loadImage("/image/day_toggle.png");
    private final Image nightImage = loadImage("/image/night_toggle.png");

    @FXML
    private void toggleTheme() {
        if (themeIconImage == null) return;

        // 3D Flip animation
        javafx.animation.RotateTransition rotateOut = new javafx.animation.RotateTransition(javafx.util.Duration.millis(150), themeIconImage);
        rotateOut.setAxis(javafx.scene.transform.Rotate.Y_AXIS);
        rotateOut.setFromAngle(0);
        rotateOut.setToAngle(90);
        rotateOut.setInterpolator(javafx.animation.Interpolator.EASE_IN);

        javafx.animation.RotateTransition rotateIn = new javafx.animation.RotateTransition(javafx.util.Duration.millis(150), themeIconImage);
        rotateIn.setAxis(javafx.scene.transform.Rotate.Y_AXIS);
        rotateIn.setFromAngle(-90);
        rotateIn.setToAngle(0);
        rotateIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        rotateOut.setOnFinished(e -> {
            darkModeEnabled = !darkModeEnabled;
            applyTheme();
            themeIconImage.setImage(darkModeEnabled ? dayImage : nightImage); // Day toggle = switch to night text logic reversed
            rotateIn.play();
        });

        rotateOut.play();
    }

    private void applyTheme() {
        preferredDarkMode = darkModeEnabled;
        if (rootPane != null) {
            rootPane.getStyleClass().remove("theme-dark");
            if (darkModeEnabled) {
                rootPane.getStyleClass().add("theme-dark");
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

    // ============================
    // Navigation vers module USER
    // ============================

    private Stage getCurrentStage() {
        if (rootPane != null && rootPane.getScene() != null && rootPane.getScene().getWindow() instanceof Stage) {
            return (Stage) rootPane.getScene().getWindow();
        }
        return null;
    }

    private void loadFXMLInCurrentStage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = getCurrentStage();
            if (stage != null) {
                Scene scene = new Scene(root);
                applyStylesheet(scene);
                if (preferredDarkMode) {
                    root.getStyleClass().add("theme-dark");
                }
                stage.setScene(scene);
                stage.setTitle(title);
                stage.show();
            } else {
                new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir la page " + title).showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger la page " + title).showAndWait();
        }
    }

    @FXML
    public void openMesTaches(ActionEvent actionEvent) {
        User current = Session.getCurrentUser();
        if (current == null) {
            new Alert(Alert.AlertType.WARNING, "Session expirée. Reconnectez-vous.").showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Tache/ListTache.fxml"));
            Parent root = loader.load();

            ListeTacheController ctrl = loader.getController();
            ctrl.setFilterByCurrentUser(true);

            Stage stage = getCurrentStage();
            if (stage != null) {
                Scene scene = new Scene(root);
                applyStylesheet(scene);
                if (preferredDarkMode) {
                    root.getStyleClass().add("theme-dark");
                }
                stage.setScene(scene);
                stage.setTitle("Mes tâches");
                stage.show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger Mes tâches.").showAndWait();
        }
    }

    @FXML
    public void openMonProfil(ActionEvent actionEvent) {
        User current = Session.getCurrentUser();
        if (current == null) {
            new Alert(Alert.AlertType.WARNING, "Session expirée. Reconnectez-vous.").showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierUser.fxml"));
            Parent root = loader.load();

            ModifierUserController ctrl = loader.getController();
            ctrl.setUser(current);
            ctrl.setSelfEdit(true);

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            if (preferredDarkMode) {
                root.getStyleClass().add("theme-dark");
            }
            stage.setScene(scene);
            stage.setTitle("Mon profil");
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger Mon profil.").showAndWait();
        }
    }

    @FXML
    public void openAgriGame(ActionEvent actionEvent) {
        loadFXMLInCurrentStage("/AgriGame.fxml", "Jeu Agricole");
    }

    @FXML
    public void openAgriPuzzle(ActionEvent actionEvent) {
        loadFXMLInCurrentStage("/AgriPuzzle.fxml", "Puzzle Agricole");
    }

    public void openGestionTaches(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Tache/ListTache.fxml"));
            Parent root = loader.load();
            
            Controllers.ListeTacheController ctrl = loader.getController();
            ctrl.setFilterByCurrentUser(false); // Admin voit toutes les tâches
            
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            if (preferredDarkMode) {
                root.getStyleClass().add("theme-dark");
            }
            stage.setScene(scene);
            stage.setTitle("Gestion des tâches");
            stage.show();
        } catch (java.io.IOException e) {
            System.err.println("openGestionTaches error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible de charger la page de gestion des tâches.").showAndWait();
        }
    }

    public void openStats(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StatsDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            stage.setScene(scene);
            stage.setTitle("Statistiques");
            stage.show();
        } catch (java.io.IOException e) {
            System.err.println("openStats error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible de charger les statistiques.").showAndWait();
        }
    }

    // Méthodes ajoutées depuis MainGuiAdminController

    /** Déconnexion et retour à l'écran de connexion */
    public void logout(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            stage.setScene(scene);
            stage.setTitle("Connexion");
            stage.show();
        } catch (java.io.IOException e) {
            System.err.println("logout error: " + e.getMessage());
        }
    }

    /** Ouvre le chatbot agricole OpenAI */
    public void openAgriChatbot(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgriChatbot.fxml"));
            Parent root = loader.load();
            Controllers.AgriChatbotController controller = loader.getController();
            controller.setReturnTo("user"); // Indiquer que c'est un utilisateur normal
            
            Stage stage = getCurrentStage();
            if (stage == null) {
                new Alert(Alert.AlertType.ERROR, "Impossible de trouver la fenêtre principale.").showAndWait();
                return;
            }
            
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            stage.setScene(scene);
            stage.setTitle("Assistant Agricole AgriGo");
            stage.show();
        } catch (IOException e) {
            System.err.println("openAgriChatbot error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir l'assistant agricole.").showAndWait();
        }
    }

    /** Déconnexion depuis le bouton logoutuser */
    public void logoutuser(ActionEvent actionEvent) {
        logout(actionEvent);
    }

    /** Ouvre la gestion des utilisateurs (redirection vers admin) */
    public void openGestionUsers(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/maingui.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            stage.setScene(scene);
            stage.setTitle("Gestion des utilisateurs");
            stage.show();
        } catch (IOException e) {
            System.err.println("openGestionUsers error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir la gestion des utilisateurs.").showAndWait();
        }
    }
}
