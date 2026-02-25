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

public class ShowUserController {

    @FXML
    private TableView<User> tableViewUserRegister;
    @FXML
    private TextField fieldRechercheUser;
    @FXML
    private Label labelAucunUser;

    private ServiceUser serviceUser = new ServiceUser();
    private ObservableList<User> userList;
    private ObservableList<User> userListFull;

    @FXML
    public void initialize() {
        // =================== COLUMNS ===================

        // API 1 (User) : Gravatar - avatar par email
        TableColumn<User, String> colAvatar = new TableColumn<>("Avatar");
        colAvatar.setCellValueFactory(new PropertyValueFactory<>("email_user"));
        colAvatar.setCellFactory(tc -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            { imageView.setFitWidth(28); imageView.setFitHeight(28); imageView.setPreserveRatio(true); }
            @Override
            protected void updateItem(String email, boolean empty) {
                super.updateItem(email, empty);
                if (empty || email == null) {
                    setGraphic(null);
                } else {
                    String url = UserApiService.getGravatarUrl(email, 56);
                    if (url != null) {
                        try {
                            Image img = new Image(url, true);
                            imageView.setImage(img);
                        } catch (Exception e) { imageView.setImage(null); }
                    }
                    setGraphic(imageView);
                }
            }
        });

        TableColumn<User, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_user"));

        TableColumn<User, String> colPrenom = new TableColumn<>("Prenom");
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom_user"));

        TableColumn<User, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email_user"));

        TableColumn<User, String> colRole = new TableColumn<>("Role");
        colRole.setCellValueFactory(new PropertyValueFactory<>("role_user"));

        TableColumn<User, Integer> colTel = new TableColumn<>("Telephone");
        colTel.setCellValueFactory(new PropertyValueFactory<>("num_user"));

        TableColumn<User, String> colAdresse = new TableColumn<>("Adresse");
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse_user"));

        // =================== ACTION COLUMN ===================
        TableColumn<User, Void> colAction = new TableColumn<>("Action");

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");

            {
                btnEdit.setStyle("-fx-background-color: #228B22; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: red; -fx-text-fill: white;");

                btnEdit.setOnAction((ActionEvent event) -> {
                    User user = getTableView().getItems().get(getIndex());
                    editUser(user);
                });

                btnDelete.setOnAction((ActionEvent event) -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox(5, btnEdit, btnDelete);
                    setGraphic(hBox);
                }
            }
        });

        // Sélection multiple pour export PDF ciblé
        tableViewUserRegister.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // =================== ADD COLUMNS TO TABLE ===================
        tableViewUserRegister.getColumns().addAll(
                colAvatar, colNom, colPrenom, colEmail, colRole, colTel, colAdresse, colAction
        );

        // =================== LOAD DATA ===================
        loadUsers();
        userListFull = FXCollections.observableArrayList(serviceUser.getAll());

        if (fieldRechercheUser != null) {
            fieldRechercheUser.textProperty().addListener((o, oldVal, newVal) -> appliquerFiltreUser());
        }
        mettreAJourPlaceholderUser();
    }

    private void loadUsers() {
        Set<User> users = serviceUser.getAll();
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
                            || (u.getRole_user() != null && u.getRole_user().toLowerCase().contains(q)))
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
        userListFull = FXCollections.observableArrayList(serviceUser.getAll());
        if (fieldRechercheUser != null) fieldRechercheUser.clear();
    }

    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer cet utilisateur ?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            serviceUser.supprimer(user.getId_user());
            userList.remove(user);
        }
    }

    private void editUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierUser.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            // Pass the selected user to the modifier controller
            ModifierUserController controller = loader.getController();
            controller.setUser(user);

            stage.showAndWait(); // Wait until window closes

            // Refresh the table after modification
            loadUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Retour à l'accueil admin */
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
    public void logoutshow(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page.");
        }
    }

    /** Export PDF : exporte uniquement les profils sélectionnés (aucune sélection = avertissement). */
    @FXML
    public void exportPdf(ActionEvent event) {
        ObservableList<User> selected = tableViewUserRegister.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            showAlert("Export PDF", "Veuillez sélectionner un ou plusieurs utilisateurs à exporter.\nCliquez sur les lignes (Ctrl+Clic pour plusieurs sélections).");
            return;
        }
        ObservableList<User> toExport = selected;
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        // Si un seul utilisateur sélectionné, utiliser "Nom_Prenom.pdf"
        if (toExport.size() == 1) {
            User u = toExport.get(0);
            String nom = u.getNom_user() != null ? u.getNom_user().replaceAll("\\s+", "_") : "user";
            String prenom = u.getPrenom_user() != null ? u.getPrenom_user().replaceAll("\\s+", "_") : "profil";
            fc.setInitialFileName(nom + "_" + prenom + ".pdf");
        } else {
            fc.setInitialFileName("utilisateurs_agrigo.pdf");
        }
        File f = fc.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
        if (f == null) return;
        try {
            ExportPdfService.exportUsersToPdf(new java.util.HashSet<>(toExport), f.getAbsolutePath());
            // Ouvrir automatiquement le PDF après export si possible
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(f);
                }
            } catch (Exception e) {
                // Si ouverture impossible, on se contente de l'alerte
                e.printStackTrace();
            }
            showAlert("Export réussi", "PDF enregistré : " + toExport.size() + " utilisateur(s) — " + f.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur export PDF", "Impossible d'exporter le PDF : " +
                    (e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    /** API 2 (module User) : Importer un utilisateur de démo depuis Random User API */
    @FXML
    public void importDemoUser(ActionEvent event) {
        UserApiService.fetchRandomUser().thenAccept(r -> Platform.runLater(() -> {
            if (!r.isValid()) {
                showAlert("API", "Impossible de récupérer un utilisateur démo (réseau ou API).");
                return;
            }
            try {
                int phone = 0;
                try { phone = Integer.parseInt(r.phone); } catch (NumberFormatException e) { }
                User u = new User(r.last, r.first, r.email, "agriculteur client", phone, "demo123", r.address != null ? r.address : "");
                serviceUser.ajouter(u);
                userList.add(u);
                showAlert("Utilisateur démo ajouté", r.first + " " + r.last + " (" + r.email + ")");
            } catch (Exception e) {
                showAlert("Erreur", e.getMessage());
            }
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
