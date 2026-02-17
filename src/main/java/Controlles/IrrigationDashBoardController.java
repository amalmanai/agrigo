package Controlles;

import Entites.HistoriqueIrrigation;
import Entites.SystemeIrrigation;
import Services.HistoriqueIrrigationCRUD;
import Services.SystemeIrrigationCRUD;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class IrrigationDashBoardController {

    @FXML private TableView<HistoriqueIrrigation> tableHistorique;
    @FXML private TableColumn<HistoriqueIrrigation, Long> colHId;
    @FXML private TableColumn<HistoriqueIrrigation, String> colHDate;
    @FXML private TableColumn<HistoriqueIrrigation, Long> colHSysteme;
    @FXML private TableColumn<HistoriqueIrrigation, Integer> colHDuree;
    @FXML private TableColumn<HistoriqueIrrigation, BigDecimal> colHVolume;
    @FXML private TableColumn<HistoriqueIrrigation, BigDecimal> colHHumidite;
    @FXML private TableColumn<HistoriqueIrrigation, String> colHType;
    @FXML private TableColumn<HistoriqueIrrigation, Void> colHActions;

    @FXML private TableView<SystemeIrrigation> tableSystemes;
    @FXML private TableColumn<SystemeIrrigation, Long> colSId;
    @FXML private TableColumn<SystemeIrrigation, String> colSNom;
    @FXML private TableColumn<SystemeIrrigation, BigDecimal> colSSeuil;
    @FXML private TableColumn<SystemeIrrigation, String> colSMode;
    @FXML private TableColumn<SystemeIrrigation, String> colSStatut;
    @FXML private TableColumn<SystemeIrrigation, Void> colSActions;

    @FXML private TabPane mainTabs;
    @FXML private Tab tabHistorique;
    @FXML private Tab tabSystemes;
    @FXML private TextField searchField;
    @FXML private Label brandTitle;
    @FXML private Label labelIrrigations, labelIrrigationsTrend;
    @FXML private Label labelVolume, labelVolumeStatus;
    @FXML private Label labelSystemes, labelSystemesDetail;
    @FXML private StackPane rootPane;
    @FXML private Button btnNavHistorique, btnNavSystemes;

    private final HistoriqueIrrigationCRUD serviceHistorique = new HistoriqueIrrigationCRUD();
    private final SystemeIrrigationCRUD serviceSysteme = new SystemeIrrigationCRUD();
    private ObservableList<HistoriqueIrrigation> masterHistorique = FXCollections.observableArrayList();
    private ObservableList<SystemeIrrigation> masterSystemes = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupHistoriqueColumns();
        setupSystemeColumns();
        setupHistoriqueActions();
        setupSystemeActions();
        setupFiltering();
        setupTabSync();
        refreshData();
    }

    private void setupHistoriqueColumns() {
        colHId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colHSysteme.setCellValueFactory(new PropertyValueFactory<>("idSysteme"));
        colHDuree.setCellValueFactory(new PropertyValueFactory<>("dureeMinutes"));
        colHVolume.setCellValueFactory(new PropertyValueFactory<>("volumeEau"));
        colHHumidite.setCellValueFactory(new PropertyValueFactory<>("humiditeAvant"));
        colHType.setCellValueFactory(new PropertyValueFactory<>("typeDeclenchement"));
        colHDate.setCellValueFactory(cb -> {
            HistoriqueIrrigation h = cb.getValue();
            if (h == null || h.getDateIrrigation() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(h.getDateIrrigation().toInstant().atZone(ZoneId.systemDefault()).format(DATE_FORMAT));
        });
    }

    private void setupSystemeColumns() {
        colSId.setCellValueFactory(new PropertyValueFactory<>("idSysteme"));
        colSNom.setCellValueFactory(new PropertyValueFactory<>("nomSysteme"));
        colSSeuil.setCellValueFactory(new PropertyValueFactory<>("seuilHumidite"));
        colSMode.setCellValueFactory(new PropertyValueFactory<>("mode"));
        colSStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    private void setupHistoriqueActions() {
        colHActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnMod = createIconButton(
                    "M3 17.25V21h3.75l11-11.03-3.75-3.75L3 17.25zm14.71-9.04a1.003 1.003 0 0 0 0-1.42L15.21 4.79a1.003 1.003 0 0 0-1.42 0l-1.83 1.83 3.75 3.75 1.5-1.46z",
                    "table-icon-edit"
            );
            private final Button btnSup = createIconButton(
                    "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zm3.5-9h1v8h-1v-8zm4 0h1v8h-1v-8zM15.5 4l-1-1h-5l-1 1H5v2h14V4z",
                    "table-icon-danger"
            );
            private final HBox container = new HBox(btnMod, btnSup);

            {
                container.setSpacing(8);
                container.setAlignment(Pos.CENTER);
                btnSup.setOnAction(e -> {
                    HistoriqueIrrigation h = getTableView().getItems().get(getIndex());
                    if (h != null && confirmDelete("cette irrigation")) {
                        try {
                            serviceHistorique.supprimer(h.getId());
                            refreshData();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Erreur: " + ex.getMessage());
                        }
                    }
                });
                btnMod.setOnAction(e -> {
                    HistoriqueIrrigation h = getTableView().getItems().get(getIndex());
                    if (h != null) openIrrigationForm(h);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void setupSystemeActions() {
        colSActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnMod = createIconButton(
                    "M3 17.25V21h3.75l11-11.03-3.75-3.75L3 17.25zm14.71-9.04a1.003 1.003 0 0 0 0-1.42L15.21 4.79a1.003 1.003 0 0 0-1.42 0l-1.83 1.83 3.75 3.75 1.5-1.46z",
                    "table-icon-edit"
            );
            private final Button btnSup = createIconButton(
                    "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zm3.5-9h1v8h-1v-8zm4 0h1v8h-1v-8zM15.5 4l-1-1h-5l-1 1H5v2h14V4z",
                    "table-icon-danger"
            );
            private final HBox container = new HBox(btnMod, btnSup);

            {
                container.setSpacing(8);
                container.setAlignment(Pos.CENTER);
                btnSup.setOnAction(e -> {
                    SystemeIrrigation s = getTableView().getItems().get(getIndex());
                    if (s != null && confirmDelete(s.getNomSysteme())) {
                        try {
                            serviceSysteme.supprimer((int) s.getIdSysteme());
                            refreshData();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Erreur: " + ex.getMessage());
                        }
                    }
                });
                btnMod.setOnAction(e -> {
                    SystemeIrrigation s = getTableView().getItems().get(getIndex());
                    if (s != null) openSystemeForm(s);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private Button createIconButton(String path, String styleClass) {
        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        icon.setContent(path);
        icon.getStyleClass().add("table-icon-glyph");
        Button button = new Button();
        button.getStyleClass().addAll("table-icon-button", styleClass);
        button.setGraphic(icon);
        return button;
    }

    private boolean confirmDelete(String name) {
        return JOptionPane.showConfirmDialog(null, "Supprimer " + name + " ?", "Confirmation",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    @FXML
    void ajouterIrrigation() {
        openIrrigationForm(null);
    }

    @FXML
    void ajouterSysteme() {
        openSystemeForm(null);
    }

    private void openIrrigationForm(HistoriqueIrrigation existant) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/formIrrigation.fxml"));
            Parent root = loader.load();
            FormIrrigationController ctrl = loader.getController();
            ctrl.setHistoriqueForEdit(existant);
            ctrl.setOnSaved(this::refreshData);

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            stage.setTitle(existant == null ? "Ajouter une irrigation" : "Modifier l'irrigation");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            refreshData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openSystemeForm(SystemeIrrigation existant) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/formSysteme.fxml"));
            Parent root = loader.load();
            FormSystemeController ctrl = loader.getController();
            ctrl.setSystemeForEdit(existant);
            ctrl.setOnSaved(this::refreshData);

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            stage.setTitle(existant == null ? "Ajouter un système" : "Modifier le système");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            refreshData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyStylesheet(Scene scene) {
        var css = getClass().getResource("/app.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
    }

    @FXML
    void showHistorique() {
        selectTab(tabHistorique);
        setActiveNav(btnNavHistorique);
    }

    @FXML
    void showSystemes() {
        selectTab(tabSystemes);
        setActiveNav(btnNavSystemes);
    }

    private void selectTab(Tab tab) {
        if (mainTabs != null && tab != null) mainTabs.getSelectionModel().select(tab);
    }

    private void setActiveNav(Button active) {
        Button[] buttons = { btnNavHistorique, btnNavSystemes };
        for (Button b : buttons) {
            if (b == null) continue;
            b.getStyleClass().remove("nav-button-active");
            if (b == active) b.getStyleClass().add("nav-button-active");
        }
    }

    private void setupFiltering() {
        if (searchField == null) return;
        searchField.textProperty().addListener((obs, o, n) -> applyFiltering());
    }

    private void applyFiltering() {
        String q = searchField == null ? "" : searchField.getText();
        String filter = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        boolean onSystemes = mainTabs != null && mainTabs.getSelectionModel().getSelectedItem() == tabSystemes;

        FilteredList<HistoriqueIrrigation> flH = new FilteredList<>(masterHistorique, h -> {
            if (onSystemes) return true;
            if (filter.isEmpty()) return true;
            return String.valueOf(h.getIdSysteme()).toLowerCase().contains(filter)
                    || (h.getTypeDeclenchement() != null && h.getTypeDeclenchement().toLowerCase().contains(filter))
                    || (h.getDateIrrigation() != null && h.getDateIrrigation().toString().toLowerCase().contains(filter));
        });
        SortedList<HistoriqueIrrigation> slH = new SortedList<>(flH);
        slH.comparatorProperty().bind(tableHistorique.comparatorProperty());
        tableHistorique.setItems(slH);

        FilteredList<SystemeIrrigation> flS = new FilteredList<>(masterSystemes, s -> {
            if (!onSystemes) return true;
            if (filter.isEmpty()) return true;
            return (s.getNomSysteme() != null && s.getNomSysteme().toLowerCase().contains(filter))
                    || (s.getMode() != null && s.getMode().toLowerCase().contains(filter))
                    || (s.getStatut() != null && s.getStatut().toLowerCase().contains(filter));
        });
        SortedList<SystemeIrrigation> slS = new SortedList<>(flS);
        slS.comparatorProperty().bind(tableSystemes.comparatorProperty());
        tableSystemes.setItems(slS);
    }

    private void setupTabSync() {
        if (mainTabs == null) return;
        mainTabs.getSelectionModel().selectedItemProperty().addListener((o, old, n) -> {
            if (brandTitle != null) brandTitle.setText(n == tabSystemes ? "Systèmes" : "Irrigation");
            if (searchField != null) searchField.setPromptText(n == tabSystemes ? "Rechercher un système..." : "Rechercher une irrigation...");
            applyFiltering();
        });
    }

    public void refreshData() {
        try {
            masterHistorique.setAll(serviceHistorique.afficherTous());
            masterSystemes.setAll(serviceSysteme.afficher());
            applyFiltering();
            updateStats();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur chargement: " + e.getMessage());
        }
    }

    private void updateStats() {
        try {
            List<HistoriqueIrrigation> tous = serviceHistorique.afficherTous();
            LocalDateTime debutMois = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            long ceMois = tous.stream()
                    .filter(h -> h.getDateIrrigation() != null &&
                            h.getDateIrrigation().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().isAfter(debutMois))
                    .count();

            BigDecimal volumeTotal = tous.stream()
                    .map(HistoriqueIrrigation::getVolumeEau)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<SystemeIrrigation> systemes = serviceSysteme.afficher();
            long actifs = systemes.stream().filter(s -> "ACTIF".equals(s.getStatut())).count();

            if (labelIrrigations != null) labelIrrigations.setText(String.valueOf(ceMois));
            if (labelIrrigationsTrend != null) labelIrrigationsTrend.setText("ce mois");
            if (labelVolume != null) labelVolume.setText(volumeTotal.stripTrailingZeros().toPlainString());
            if (labelVolumeStatus != null) labelVolumeStatus.setText("Stable");
            if (labelSystemes != null) labelSystemes.setText(String.valueOf(actifs));
            if (labelSystemesDetail != null) labelSystemesDetail.setText(systemes.size() + " système(s) au total");
        } catch (SQLException e) {
            if (labelIrrigations != null) labelIrrigations.setText("0");
            if (labelVolume != null) labelVolume.setText("0");
            if (labelSystemes != null) labelSystemes.setText("0");
        }
    }
}
