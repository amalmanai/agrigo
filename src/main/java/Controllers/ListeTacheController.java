package Controllers;

import Entites.Tache;
import Services.ServiceTache;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.Set;

public class ListeTacheController {

    @FXML private TableView<Tache> ListTache;

    private final ServiceTache serviceTache = new ServiceTache();
    private ObservableList<Tache> taches;

    @FXML
    public void initialize() {
        // Colonnes visibles
        TableColumn<Tache, String> colTitre = new TableColumn<>("Titre");
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre_tache"));

        TableColumn<Tache, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description_tache"));

        TableColumn<Tache, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type_tache"));

        TableColumn<Tache, Date> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_tache"));

        TableColumn<Tache, Time> colDebut = new TableColumn<>("Heure Début");
        colDebut.setCellValueFactory(new PropertyValueFactory<>("heure_debut_tache"));

        TableColumn<Tache, Time> colFin = new TableColumn<>("Heure Fin");
        colFin.setCellValueFactory(new PropertyValueFactory<>("heure_fin_tache"));

        TableColumn<Tache, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status_tache"));

        TableColumn<Tache, String> colRemarque = new TableColumn<>("Remarque");
        colRemarque.setCellValueFactory(new PropertyValueFactory<>("remarque_tache"));

        // Colonne Action
        TableColumn<Tache, Void> colAction = new TableColumn<>("Action");
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");

            {
                btnEdit.setStyle("-fx-background-color: #228B22; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: red; -fx-text-fill: white;");

                btnEdit.setOnAction(event -> {
                    Tache tache = getTableView().getItems().get(getIndex());
                    editTache(tache);
                });

                btnDelete.setOnAction(event -> {
                    Tache tache = getTableView().getItems().get(getIndex());
                    deleteTache(tache);
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

        // Ajouter toutes les colonnes
        ListTache.getColumns().addAll(colTitre, colDesc, colType, colDate, colDebut, colFin, colStatus, colRemarque, colAction);

        // Charger les données
        loadTaches();
    }

    private void loadTaches() {
        Set<Tache> allTaches = serviceTache.getAll(); // récupère toutes les tâches
        taches = FXCollections.observableArrayList(allTaches);
        ListTache.setItems(taches);
        System.out.println("✅ Nombre de tâches chargées : " + taches.size());
    }

    private void deleteTache(Tache tache) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer cette tâche ?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            serviceTache.supprimer(tache.getId());
            taches.remove(tache);
        }
    }

    private void editTache(Tache tache) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Tache/ModifierTache.fxml"));
            Parent root = loader.load();

            ModifierTacheController controller = loader.getController();
            controller.setTache(tache); // passer la tâche sélectionnée

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Tâche");
            stage.showAndWait();

            // Rafraîchir la liste après modification
            loadTaches();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void Ajouter_Tache(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Tache/AjouterTache.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Tâche");
            stage.showAndWait();

            loadTaches();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
