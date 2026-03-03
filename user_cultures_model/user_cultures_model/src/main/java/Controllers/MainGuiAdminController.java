package Controllers;

import Entites.Alerte;
import Entites.Culture;
import Entites.Parcelle;
import Entites.User;
import Services.AlerteCRUD;
import Services.CultureCRUD;
import Services.ParcelleCRUD;
import Services.ServiceUser;
import Services.WeatherService;
import Services.YieldPredictionService;
import Utils.Session;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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

public class MainGuiAdminController {

    // ================= Tables =================
    @FXML
    private TableView<Culture> tableCultures;
    @FXML
    private TableColumn<Culture, Integer> colId;
    @FXML
    private TableColumn<Culture, String> colNom;
    @FXML
    private TableColumn<Culture, String> colEtat;
    @FXML
    private TableColumn<Culture, Double> colRendement;
    @FXML
    private TableColumn<Culture, Void> colActions;

    @FXML
    private TableView<Parcelle> tableParcelles;
    @FXML
    private TableColumn<Parcelle, Integer> colPId;
    @FXML
    private TableColumn<Parcelle, String> colPNom;
    @FXML
    private TableColumn<Parcelle, Double> colPSurface;
    @FXML
    private TableColumn<Parcelle, String> colPType;
    @FXML
    private TableColumn<Parcelle, Void> colPActions;

    @FXML
    private TableView<Alerte> tableAlertes;
    @FXML
    private TableColumn<Alerte, Integer> colAId;
    @FXML
    private TableColumn<Alerte, String> colAType;
    @FXML
    private TableColumn<Alerte, String> colADesc;
    @FXML
    private TableColumn<Alerte, String> colACulture;

    @FXML
    private TableView<User> tableUsers;
    @FXML
    private TableColumn<User, Integer> colUId;
    @FXML
    private TableColumn<User, String> colUNom;
    @FXML
    private TableColumn<User, String> colUEmail;
    @FXML
    private TableColumn<User, String> colURole;
    @FXML
    private TableColumn<User, Void> colUActions;

    // ================= UI =================
    @FXML
    private TabPane mainTabs;
    @FXML
    private Tab tabDashboard;
    @FXML
    private Tab tabCultures;
    @FXML
    private Tab tabParcelles;
    @FXML
    private Tab tabAnalytics;

    @FXML
    private TextField searchField;
    @FXML
    private Label lblTotalCultures;
    @FXML
    private Label lblTotalParcelles;
    @FXML
    private Label lblTotalAlertes;
    @FXML
    private Label lblTotalUsers;

    @FXML
    private StackPane rootPane;

    // ================= Services =================
    private final CultureCRUD cultureService = new CultureCRUD();
    private final ParcelleCRUD parcelleService = new ParcelleCRUD();
    private final AlerteCRUD alerteService = new AlerteCRUD();
    private final ServiceUser userService = new ServiceUser();
    private final WeatherService weatherService = new WeatherService();

    private ObservableList<Culture> masterCultures = FXCollections.observableArrayList();
    private ObservableList<Parcelle> masterParcelles = FXCollections.observableArrayList();
    private ObservableList<Alerte> masterAlertes = FXCollections.observableArrayList();
    private ObservableList<User> masterUsers = FXCollections.observableArrayList();

    public static boolean preferredDarkMode = false;
    private ActionEvent actionEvent;

    // ================= INITIALIZE =================
    @FXML
    public void initialize() {
        setupColumns();
        setupFiltering();
        refreshTable();
        applyTheme();
    }

    // ================= SETUP =================
    private void setupColumns() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));
        colRendement.setCellValueFactory(new PropertyValueFactory<>("rendement"));

        colPId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPSurface.setCellValueFactory(new PropertyValueFactory<>("surface"));
        colPType.setCellValueFactory(new PropertyValueFactory<>("typeSol"));

        colUId.setCellValueFactory(new PropertyValueFactory<>("id_user"));
        colUNom.setCellValueFactory(new PropertyValueFactory<>("nom_user"));
        colUEmail.setCellValueFactory(new PropertyValueFactory<>("email_user"));
        colURole.setCellValueFactory(new PropertyValueFactory<>("role_user"));

        setupCultureActions();
        setupParcelleActions();
        setupUserActions();
    }

    // ================= FILTER =================
    private void setupFiltering() {
        if (searchField == null)
            return;

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFiltering());
    }

    private void applyFiltering() {
        String filter = searchField.getText() == null ? "" : searchField.getText().toLowerCase();

        FilteredList<Culture> filteredCultures = new FilteredList<>(masterCultures,
                c -> c.getNom().toLowerCase().contains(filter));

        SortedList<Culture> sortedCultures = new SortedList<>(filteredCultures);
        sortedCultures.comparatorProperty().bind(tableCultures.comparatorProperty());
        tableCultures.setItems(sortedCultures);
    }

    // ================= REFRESH =================
    public void refreshTable() {
        try {
            masterCultures = FXCollections.observableArrayList(cultureService.afficher());
            masterParcelles = FXCollections.observableArrayList(parcelleService.afficher());
            masterAlertes = FXCollections.observableArrayList(alerteService.afficher());
            masterUsers = FXCollections.observableArrayList(userService.getAll());

            tableCultures.setItems(masterCultures);
            tableParcelles.setItems(masterParcelles);
            tableUsers.setItems(masterUsers);

            updateStats();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= STATS =================
    private void updateStats() {
        lblTotalCultures.setText(String.valueOf(masterCultures.size()));
        lblTotalParcelles.setText(String.valueOf(masterParcelles.size()));
        lblTotalAlertes.setText(String.valueOf(masterAlertes.size()));
        lblTotalUsers.setText(String.valueOf(masterUsers.size()));
    }

    // ================= ACTION BUTTONS =================
    private void setupCultureActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            final Button deleteBtn = createIconButton();
            final HBox box = new HBox(deleteBtn);

            {
                box.setAlignment(Pos.CENTER);
                deleteBtn.setOnAction(e -> {
                    int index = getIndex();
                    if (index < 0)
                        return;
                    Culture c = getTableView().getItems().get(index);
                    try {
                        cultureService.supprimer(c.getId());
                        refreshTable();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupParcelleActions() {
        colPActions.setCellFactory(param -> new TableCell<>() {
            final Button deleteBtn = createIconButton();
            final HBox box = new HBox(deleteBtn);

            {
                box.setAlignment(Pos.CENTER);
                deleteBtn.setOnAction(e -> {
                    int index = getIndex();
                    if (index < 0)
                        return;
                    Parcelle p = getTableView().getItems().get(index);
                    try {
                        parcelleService.supprimer(p.getId());
                        refreshTable();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupUserActions() {
        colUActions.setCellFactory(param -> new TableCell<>() {
            final Button deleteBtn = createIconButton();
            final HBox box = new HBox(deleteBtn);

            {
                box.setAlignment(Pos.CENTER);
                deleteBtn.setOnAction(e -> {
                    int index = getIndex();
                    if (index < 0)
                        return;
                    User u = getTableView().getItems().get(index);
                    userService.supprimer(u.getId_user());
                    refreshTable();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ================= ICON BUTTON =================
    private Button createIconButton() {
        SVGPath icon = new SVGPath();
        icon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6z");
        Button btn = new Button();
        btn.setGraphic(icon);
        return btn;
    }

    // ================= THEME =================
    private void applyTheme() {
        if (rootPane == null)
            return;

        if (preferredDarkMode) {
            if (!rootPane.getStyleClass().contains("theme-dark")) {
                rootPane.getStyleClass().add("theme-dark");
            }
        } else {
            rootPane.getStyleClass().remove("theme-dark");
        }
    }

    private void applyStylesheet(Scene scene) {
        try {
            var css = getClass().getResource("/app.css");
            if (css != null)
                scene.getStylesheets().add(css.toExternalForm());
        } catch (Exception ignored) {
        }
    }

    public void toggleTheme(ActionEvent event) {
        preferredDarkMode = !preferredDarkMode;
        applyTheme();
    }

    // ================= NAVIGATION =================
    public void logout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logoutuser(ActionEvent event) {
        logout(event);
    }

    public void openAnalytics(ActionEvent event) {
        try {
            List<YieldPredictionService.PredictionResult> predictions = new YieldPredictionService()
                    .predictYields(masterCultures, masterParcelles, null);
            System.out.println("Predictions loaded: " + predictions.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openAgriChatbot(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgriChatbot.fxml"));
            Parent root = loader.load();
            AgriChatbotController controller = loader.getController();
            controller.setReturnTo("admin"); // Indiquer que c'est un admin

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            if (preferredDarkMode) {
                root.getStyleClass().add("theme-dark");
            }
            stage.setScene(scene);
            stage.setTitle("Assistant Agricole AgriGo");
            stage.show();
        } catch (IOException e) {
            System.err.println("openAgriChatbot error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir l'assistant agricole.").showAndWait();
        }
    }

    public void openAgriPuzzle(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgriPuzzle.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            if (preferredDarkMode) {
                root.getStyleClass().add("theme-dark");
            }
            stage.setScene(scene);
            stage.setTitle("Puzzle Agricole");
            stage.show();
        } catch (IOException e) {
            System.err.println("openAgriPuzzle error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le puzzle agricole.").showAndWait();
        }
    }

    public void openAgriGame(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgriGame.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            if (preferredDarkMode) {
                root.getStyleClass().add("theme-dark");
            }
            stage.setScene(scene);
            stage.setTitle("Jeu Agricole");
            stage.show();
        } catch (IOException e) {
            System.err.println("openAgriGame error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le jeu agricole.").showAndWait();
        }
    }

    public void openMesTaches(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Tache/ListTache.fxml"));
            Parent root = loader.load();

            ListeTacheController controller = loader.getController();
            if (controller != null) {
                controller.setFilterByCurrentUser(true);
            }

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            if (preferredDarkMode) {
                root.getStyleClass().add("theme-dark");
            }
            stage.setScene(scene);
            stage.setTitle("Mes tâches");
            stage.show();
        } catch (IOException e) {
            System.err.println("openMesTaches error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir Mes tâches.").showAndWait();
        }
    }

    public void openMonProfil(ActionEvent actionEvent) {
        User current = Session.getCurrentUser();
        if (current == null) {
            new Alert(Alert.AlertType.WARNING, "Session expirée. Reconnectez-vous.").showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierUser.fxml"));
            Parent root = loader.load();

            // Configurer le contrôleur pour l'utilisateur courant
            ModifierUserController controller = loader.getController();
            if (controller != null) {
                controller.setUser(current);
            }

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            if (preferredDarkMode) {
                root.getStyleClass().add("theme-dark");
            }
            stage.setScene(scene);
            stage.setTitle("Mon profil");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("openMonProfil error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible de charger Mon profil.").showAndWait();
        }
    }

    public void showAnalytics(ActionEvent actionEvent) {
        if (mainTabs != null && tabAnalytics != null) {
            mainTabs.getSelectionModel().select(tabAnalytics);
        }
    }

    public void showParcelles(ActionEvent actionEvent) {
        if (mainTabs != null && tabParcelles != null) {
            mainTabs.getSelectionModel().select(tabParcelles);
        }
    }

    public void openGestionTaches(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Tache/ListTache.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            if (preferredDarkMode) {
                root.getStyleClass().add("theme-dark");
            }
            stage.setScene(scene);
            stage.setTitle("Gestion des tâches");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("openGestionTaches error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible de charger la gestion des tâches.").showAndWait();
        }
    }

    public void showCultures(ActionEvent actionEvent) {
        if (mainTabs != null && tabCultures != null) {
            mainTabs.getSelectionModel().select(tabCultures);
        }
    }

    @FXML
    private Tab tabStats;

    public void openStats(ActionEvent actionEvent) {
        if (mainTabs != null && tabStats != null) {
            mainTabs.getSelectionModel().select(tabStats);
        }
    }

    @FXML
    private Tab tabUsers;

    public void openGestionUsers(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            if (preferredDarkMode) {
                root.getStyleClass().add("theme-dark");
            }
            stage.setScene(scene);
            stage.setTitle("Gestion des utilisateurs");
            stage.show();
        } catch (IOException e) {
            System.err.println("openGestionUsers error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible de charger la gestion des utilisateurs.").showAndWait();
        }
    }

    public void showDashboard(ActionEvent actionEvent) {
        if (mainTabs != null && tabDashboard != null) {
            mainTabs.getSelectionModel().select(tabDashboard);
        }
    }

    public void ajouterParcelle(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pageParcelle.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            if (preferredDarkMode) {
                root.getStyleClass().add("theme-dark");
            }
            stage.setScene(scene);
            stage.setTitle("Ajouter une Parcelle");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            System.err.println("ajouterParcelle error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible d'ajouter une parcelle.").showAndWait();
        }
    }

    public void ajouterCulture(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/page1.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            if (preferredDarkMode) {
                root.getStyleClass().add("theme-dark");
            }
            stage.setScene(scene);
            stage.setTitle("Ajouter une Culture");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            System.err.println("ajouterCulture error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible d'ajouter une culture.").showAndWait();
        }
    }

    public void ajouterUser(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterUser.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            if (preferredDarkMode) {
                root.getStyleClass().add("theme-dark");
            }
            stage.setScene(scene);
            stage.setTitle("Ajouter un utilisateur");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            System.err.println("ajouterUser error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible d'ajouter un utilisateur.").showAndWait();
        }
    }

    public void retour(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuadmin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            if (preferredDarkMode) {
                root.getStyleClass().add("theme-dark");
            }
            stage.setScene(scene);
            stage.setTitle("Menu Admin");
            stage.show();
        } catch (IOException e) {
            System.err.println("retour error: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible de revenir au menu admin.").showAndWait();
        }
    }

    public void todashboard(ActionEvent actionEvent) {
        retour(actionEvent);
    }

    // Méthodes manquantes pour FXML
    public void openGestionTache(ActionEvent actionEvent) {
        openGestionTaches(actionEvent);
    }

    public void BtnGestionTache(ActionEvent actionEvent) {
        openGestionTaches(actionEvent);
    }
}