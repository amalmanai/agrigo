package Controllers;

import Entites.User;
import Services.ServiceUser;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Set;

public class ShowUserController {

    @FXML
    private TableView<User> tableViewUserRegister;

    private ServiceUser serviceUser = new ServiceUser();
    private ObservableList<User> userList;

    @FXML
    public void initialize() {
        // =================== COLUMNS ===================


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

        // =================== ADD COLUMNS TO TABLE ===================
        tableViewUserRegister.getColumns().addAll(
                colNom, colPrenom, colEmail, colRole, colTel, colAdresse, colAction
        );

        // =================== LOAD DATA ===================
        loadUsers();
    }

    private void loadUsers() {
        Set<User> users = serviceUser.getAll();
        userList = FXCollections.observableArrayList(users);
        tableViewUserRegister.setItems(userList);
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

    @FXML
    public void logoutshow(ActionEvent actionEvent) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RegisterUser.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            // Set the new scene
            stage.setScene(new Scene(root));
            stage.setTitle("Register User"); // Optional: set title
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erreur");
            alert.setContentText("Impossible de charger la page RegisterUser.fxml");
            alert.showAndWait();
        }
    }

}
