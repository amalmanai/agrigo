package Controllers;

import Api.UserApiService;
import Entites.User;
import Services.ServiceUser;
import Utils.ExportPdfService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/** Gestion des ouvriers agricoles (main d'œuvre). */
public class GestionMainOeuvreController {

    private static final String ROLE_OUVRIER = "ouvrier agricole";

    @FXML private TableView<User> tableViewUserRegister;
    @FXML private TextField fieldRechercheUser;
    @FXML private Label labelAucunUser;

    private ServiceUser serviceUser = new ServiceUser();
    private ObservableList<User> userList;
    private ObservableList<User> userListFull;

    @FXML
    public void initialize() {
        TableColumn<User, String> colAvatar = new TableColumn<>("Avatar");
        colAvatar.setCellValueFactory(new PropertyValueFactory<>("email_user"));
        colAvatar.setCellFactory(tc -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            { imageView.setFitWidth(28); imageView.setFitHeight(28); imageView.setPreserveRatio(true); }
            @Override
            protected void updateItem(String email, boolean empty) {
                super.updateItem(email, empty);
                if (empty || email == null) { setGraphic(null); }
                else {
                    String url = UserApiService.getGravatarUrl(email, 56);
                    if (url != null) try { imageView.setImage(new Image(url, true)); } catch (Exception e) { imageView.setImage(null); }
                    setGraphic(imageView);
                }
            }
        });

        TableColumn<User, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_user"));
        TableColumn<User, String> colPrenom = new TableColumn<>("Prénom");
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom_user"));
        TableColumn<User, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email_user"));
        TableColumn<User, String> colRole = new TableColumn<>("Rôle");
        colRole.setCellValueFactory(new PropertyValueFactory<>("role_user"));
        TableColumn<User, Integer> colTel = new TableColumn<>("Téléphone");
        colTel.setCellValueFactory(new PropertyValueFactory<>("num_user"));
        TableColumn<User, String> colAdresse = new TableColumn<>("Adresse");
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse_user"));

        TableColumn<User, Void> colAction = new TableColumn<>("Action");
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            {
                btnEdit.setStyle("-fx-background-color: #228B22; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                btnEdit.setOnAction(e -> { User u = getTableView().getItems().get(getIndex()); editUser(u); });
                btnDelete.setOnAction(e -> { User u = getTableView().getItems().get(getIndex()); deleteUser(u); });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, btnEdit, btnDelete));
            }
        });

        tableViewUserRegister.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableViewUserRegister.getColumns().addAll(colAvatar, colNom, colPrenom, colEmail, colRole, colTel, colAdresse, colAction);

        loadUsers();
        userListFull = FXCollections.observableArrayList(serviceUser.getByRole(ROLE_OUVRIER));
        if (fieldRechercheUser != null) fieldRechercheUser.textProperty().addListener((o, oldVal, newVal) -> appliquerFiltreUser());
        mettreAJourPlaceholderUser();
    }

    private void loadUsers() {
        Set<User> users = serviceUser.getByRole(ROLE_OUVRIER);
        userList = FXCollections.observableArrayList(users);
        tableViewUserRegister.setItems(userList);
        if (userListFull != null) userListFull.setAll(users);
        mettreAJourPlaceholderUser();
    }

    private void appliquerFiltreUser() {
        if (userListFull == null) return;
        String q = fieldRechercheUser != null ? fieldRechercheUser.getText().trim().toLowerCase() : "";
        if (q.isEmpty()) {
            userList.setAll(userListFull);
        } else {
            userList.setAll(userListFull.stream()
                    .filter(u -> (u.getNom_user() != null && u.getNom_user().toLowerCase().contains(q))
                            || (u.getPrenom_user() != null && u.getPrenom_user().toLowerCase().contains(q))
                            || (u.getEmail_user() != null && u.getEmail_user().toLowerCase().contains(q))
                            || (String.valueOf(u.getNum_user()).contains(q)))
                    .collect(Collectors.toList()));
        }
        mettreAJourPlaceholderUser();
    }

    private void mettreAJourPlaceholderUser() {
        if (labelAucunUser != null) labelAucunUser.setVisible(userList != null && userList.isEmpty());
    }

    @FXML
    public void rafraichir(ActionEvent event) {
        loadUsers();
        userListFull = FXCollections.observableArrayList(serviceUser.getByRole(ROLE_OUVRIER));
        if (fieldRechercheUser != null) fieldRechercheUser.clear();
    }

    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer cet ouvrier ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            serviceUser.supprimer(user.getId_user());
            userList.remove(user);
        }
    }

    private void editUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierUser.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            ModifierUserController controller = loader.getController();
            controller.setUser(user);
            stage.showAndWait();
            loadUsers();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void retourAccueil(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MainGuiAdmin.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AgriGo");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de revenir à l'accueil.");
        }
    }

    @FXML
    public void exportPdf(ActionEvent event) {
        ObservableList<User> selected = tableViewUserRegister.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            showAlert("Export PDF", "Veuillez sélectionner un ou plusieurs ouvriers à exporter.\nCliquez sur les lignes (Ctrl+Clic pour plusieurs sélections).");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        // Si un seul ouvrier est sélectionné, utiliser Nom_Prenom comme nom de fichier
        if (selected.size() == 1) {
            User u = selected.get(0);
            String nom = u.getNom_user() != null ? u.getNom_user().replaceAll("\\s+", "_") : "ouvrier";
            String prenom = u.getPrenom_user() != null ? u.getPrenom_user().replaceAll("\\s+", "_") : "profil";
            fc.setInitialFileName(nom + "_" + prenom + ".pdf");
        } else {
            fc.setInitialFileName("main_oeuvre_agrigo.pdf");
        }
        File f = fc.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
        if (f == null) return;
        try {
            ExportPdfService.exportUsersToPdf(new java.util.HashSet<>(selected), f.getAbsolutePath());
            // Ouvrir automatiquement le PDF après export si possible
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(f);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            showAlert("Export réussi", selected.size() + " ouvrier(s) exporté(s) — " + f.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'exporter le PDF.");
        }
    }

    @FXML
    public void importDemoUser(ActionEvent event) {
        UserApiService.fetchRandomUser().thenAccept(r -> Platform.runLater(() -> {
            if (!r.isValid()) {
                showAlert("API", "Impossible de récupérer un ouvrier démo (réseau ou API).");
                return;
            }
            try {
                int phone = 0;
                try { phone = Integer.parseInt(r.phone); } catch (NumberFormatException e) { }
                User u = new User(r.last, r.first, r.email, ROLE_OUVRIER, phone, "demo123", r.address != null ? r.address : "");
                serviceUser.ajouter(u);
                userList.add(u);
                userListFull.add(u);
                showAlert("Ouvrier démo ajouté", r.first + " " + r.last + " (" + r.email + ")");
            } catch (Exception e) { showAlert("Erreur", e.getMessage()); }
        }));
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
