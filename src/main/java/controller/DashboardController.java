package controller;

import entity.recolte;
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
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.recolteCRUD;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    @FXML private TableView<recolte> tableRecoltes;
    @FXML private TableColumn<recolte, Integer> colId;
    @FXML private TableColumn<recolte, String> colNom;
    @FXML private TableColumn<recolte, Double> colQuantite;
    @FXML private TableColumn<recolte, String> colUnite;
    @FXML private TableColumn<recolte, String> colDate;
    @FXML private TableColumn<recolte, Double> colCout;
    @FXML private TableColumn<recolte, Void> colActions;
    @FXML private TabPane mainTabs;
    @FXML private Tab tabRecoltes;
    @FXML private TextField searchField;
    @FXML private Label brandTitle;
    @FXML private Label statTotalRecoltes;
    @FXML private Label statQuantiteTotale;
    @FXML private Label statCoutTotal;
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavRecoltes;
    @FXML private StackPane rootPane;
    @FXML private Button btnThemeToggle;
    @FXML private Label themeIconLabel;

    private final recolteCRUD service = new recolteCRUD();
    private ObservableList<recolte> masterRecoltes = FXCollections.observableArrayList();
    private boolean darkModeEnabled = false;
    private static boolean preferredDarkMode = false;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id_recolte"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_produit"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colUnite.setCellValueFactory(new PropertyValueFactory<>("unite"));
        colDate.setCellValueFactory(cellData -> {
            java.sql.Date d = cellData.getValue().getDate_recolte();
            return new ReadOnlyObjectWrapper<>(d != null ? d.toLocalDate().toString() : "");
        });
        colCout.setCellValueFactory(new PropertyValueFactory<>("cout_production"));

        setupNomColumn();
        setupActions();
        setupFiltering();
        darkModeEnabled = preferredDarkMode;
        applyTheme();
        refreshTable();
    }

    private void setupNomColumn() {
        colNom.setCellFactory(param -> new javafx.scene.control.TableCell<>() {
            private final Label nameLabel = new Label();
            {
                nameLabel.getStyleClass().add("table-name-label");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(item);
                    setGraphic(nameLabel);
                }
            }
        });
    }

    private void setupActions() {
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
                    recolte selected = getTableView().getItems().get(getIndex());
                    if (confirmDelete(selected.getNom_produit())) {
                        try {
                            service.supprimer(selected.getId_recolte());
                            refreshTable();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                btnMod.setOnAction(e -> {
                    int index = getIndex();
                    if (index < 0 || index >= getTableView().getItems().size()) return;
                    recolte selected = getTableView().getItems().get(index);
                    if (selected != null) openRecolteEditor(selected);
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
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.getStyleClass().add("table-icon-glyph");
        Button button = new Button();
        button.getStyleClass().addAll("table-icon-button", styleClass);
        button.setGraphic(icon);
        return button;
    }

    private boolean confirmDelete(String name) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la récolte");
        alert.setContentText("Supprimer \"" + name + "\" ?");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    @FXML
    void ajouterRecolte() {
        openRecolteForm("/RecolteForm.fxml", "Ajouter une récolte", null);
    }

    private void openRecolteEditor(recolte r) {
        openRecolteForm("/RecolteForm.fxml", "Modifier une récolte", r);
    }

    private void openRecolteForm(String fxmlPath, String title, recolte toEdit) {
        try {
            var resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("ERROR: FXML not found at " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            applyThemeToRoot(root);
            RecolteFormController formController = loader.getController();
            if (toEdit != null) formController.setRecolteForEdit(toEdit);

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            System.err.println("Could not load FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public void refreshTable() {
        try {
            List<recolte> list = service.afficher();
            masterRecoltes = FXCollections.observableArrayList(list);
            applyFiltering();
            updateStats(list);
        } catch (SQLException e) {
            System.err.println("DB Load Error: " + e.getMessage());
        }
    }

    private void updateStats(List<recolte> list) {
        if (statTotalRecoltes != null) statTotalRecoltes.setText(String.valueOf(list.size()));
        double qte = list.stream().mapToDouble(recolte::getQuantite).sum();
        double cout = list.stream().mapToDouble(recolte::getCout_production).sum();
        if (statQuantiteTotale != null) statQuantiteTotale.setText(String.format(Locale.US, "%.1f", qte));
        if (statCoutTotal != null) statCoutTotal.setText(String.format(Locale.US, "%.0f", cout));
    }

    private void applyStylesheet(Scene scene) {
        var css = getClass().getResource("/app.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
    }

    private void setupFiltering() {
        if (searchField == null) return;
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltering());
    }

    private void applyFiltering() {
        String query = searchField == null ? "" : searchField.getText();
        String filter = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        FilteredList<recolte> filtered = new FilteredList<>(masterRecoltes, r -> {
            if (filter.isEmpty()) return true;
            return containsIgnoreCase(r.getNom_produit(), filter)
                || containsIgnoreCase(r.getUnite(), filter)
                || containsIgnoreCase(String.valueOf(r.getQuantite()), filter)
                || (r.getDate_recolte() != null && containsIgnoreCase(r.getDate_recolte().toString(), filter));
        });
        SortedList<recolte> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tableRecoltes.comparatorProperty());
        tableRecoltes.setItems(sorted);
    }

    private boolean containsIgnoreCase(String value, String filter) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(filter);
    }

    @FXML
    void showDashboard() {
        selectTab(tabRecoltes);
        setActiveNav(btnNavDashboard);
    }

    @FXML
    void showRecoltes() {
        selectTab(tabRecoltes);
        setActiveNav(btnNavRecoltes);
    }

    private void selectTab(Tab tab) {
        if (mainTabs != null && tab != null) mainTabs.getSelectionModel().select(tab);
    }

    private void setActiveNav(Button active) {
        Button[] buttons = { btnNavDashboard, btnNavRecoltes };
        for (Button button : buttons) {
            if (button == null) continue;
            button.getStyleClass().remove("nav-button-active");
            if (button == active) button.getStyleClass().add("nav-button-active");
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
            if (darkModeEnabled) rootPane.getStyleClass().add("theme-dark");
        }
        if (themeIconLabel != null) {
            themeIconLabel.setText(darkModeEnabled ? "☀" : "☾");
        }
        if (btnThemeToggle != null) {
            btnThemeToggle.setTooltip(new Tooltip(darkModeEnabled ? "Mode clair" : "Mode sombre"));
        }
    }

    private void applyThemeToRoot(Parent root) {
        if (root == null) return;
        root.getStyleClass().remove("theme-dark");
        if (darkModeEnabled) root.getStyleClass().add("theme-dark");
    }
}
