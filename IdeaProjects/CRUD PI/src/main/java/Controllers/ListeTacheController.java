 package Controllers;

import Api.TacheApiService;
import Entites.Tache;
import Services.ServiceTache;
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
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import Utils.Session;

public class ListeTacheController {

    @FXML private TableView<Tache> ListTache;
    @FXML private Label labelMeteo;
    @FXML private Label labelCitation;
    @FXML private TextField fieldRechercheTache;
    @FXML private ComboBox<String> comboFiltreStatut;
    @FXML private Label labelAucuneTache;

    private final ServiceTache serviceTache = new ServiceTache();
    private ObservableList<Tache> taches;
    private ObservableList<Tache> tachesFull;
    /** Si true, affiche uniquement les tâches de l'utilisateur connecté (Mes tâches). */
    private boolean filterByCurrentUser = false;

    /** À appeler après load() pour n'afficher que les tâches du user connecté. */
    public void setFilterByCurrentUser(boolean filter) {
        this.filterByCurrentUser = filter;
        if (filter && Session.getCurrentUser() != null) {
            Set<Tache> userTaches = serviceTache.getByUser(Session.getCurrentUser().getId_user());
            taches = FXCollections.observableArrayList(userTaches);
            tachesFull = FXCollections.observableArrayList(userTaches);
            if (ListTache != null) {
                ListTache.setItems(taches);
            }
            mettreAJourPlaceholderTache();
        }
    }

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

        // Sélection multiple pour export PDF ciblé
        ListTache.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Utilisateur non-admin : ne voir que ses propres tâches
        if (Session.getCurrentUser() != null && !"admin".equalsIgnoreCase(Session.getCurrentUser().getRole_user())) {
            filterByCurrentUser = true;
        }

        // Charger les données (selon le rôle : toutes les tâches ou uniquement les siennes)
        loadTaches();
        tachesFull = FXCollections.observableArrayList(taches);

        if (comboFiltreStatut != null) {
            comboFiltreStatut.getItems().addAll("Assignée", "En cours", "Terminée", "Annulée");
            comboFiltreStatut.valueProperty().addListener((o, oldVal, newVal) -> appliquerFiltreTache());
        }
        if (fieldRechercheTache != null) {
            fieldRechercheTache.textProperty().addListener((o, oldVal, newVal) -> appliquerFiltreTache());
        }
        mettreAJourPlaceholderTache();

        // API 1 (Tâche) : Météo pour aujourd'hui
        TacheApiService.getWeatherForDate(LocalDate.now()).thenAccept(w -> Platform.runLater(() -> {
            if (labelMeteo != null) labelMeteo.setText("Météo (aujourd'hui) : " + w.getSummary());
        }));

        // API 2 (Tâche) : Citation aléatoire
        TacheApiService.getRandomQuote().thenAccept(q -> Platform.runLater(() -> {
            if (labelCitation != null && q.content != null)
                labelCitation.setText("« " + q.content + " » — " + (q.author != null ? q.author : ""));
        }));
    }

    private void loadTaches() {
        Set<Tache> allTaches = filterByCurrentUser && Session.getCurrentUser() != null
                ? serviceTache.getByUser(Session.getCurrentUser().getId_user())
                : serviceTache.getAll();
        taches = FXCollections.observableArrayList(allTaches);
        ListTache.setItems(taches);
        if (tachesFull != null) tachesFull.setAll(allTaches);
        mettreAJourPlaceholderTache();
    }

    private void appliquerFiltreTache() {
        if (tachesFull == null) return;
        String q = fieldRechercheTache != null ? fieldRechercheTache.getText().trim().toLowerCase() : "";
        String statut = comboFiltreStatut != null && comboFiltreStatut.getValue() != null ? comboFiltreStatut.getValue().trim() : "";
        taches.setAll(tachesFull.stream()
                .filter(t -> (q.isEmpty() || (t.getTitre_tache() != null && t.getTitre_tache().toLowerCase().contains(q)))
                        && (statut.isEmpty() || (t.getStatus_tache() != null && t.getStatus_tache().equals(statut))))
                .collect(Collectors.toList()));
        mettreAJourPlaceholderTache();
    }

    private void mettreAJourPlaceholderTache() {
        if (labelAucuneTache != null) labelAucuneTache.setVisible(taches != null && taches.isEmpty());
    }

    @FXML
    public void rafraichir(ActionEvent event) {
        loadTaches();
        tachesFull = FXCollections.observableArrayList(serviceTache.getAll());
        if (fieldRechercheTache != null) fieldRechercheTache.clear();
        if (comboFiltreStatut != null) comboFiltreStatut.setValue(null);
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

    /** Retour à l'accueil (MainGui ou MainGuiAdmin selon le rôle) */
    @FXML
    public void retourAccueil(ActionEvent event) {
        try {
            String fxml = (Session.getCurrentUser() != null && "admin".equalsIgnoreCase(Session.getCurrentUser().getRole_user()))
                    ? "/MainGuiAdmin.fxml" : "/MainGui.fxml";
            Parent root = new FXMLLoader(getClass().getResource(fxml)).load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AgriGo");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de revenir à l'accueil.").showAndWait();
        }
    }

    /** Export PDF : exporte uniquement les tâches sélectionnées. */
    @FXML
    public void exportPdf(ActionEvent event) {
        java.util.List<Tache> selected = ListTache.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Veuillez sélectionner une ou plusieurs tâches à exporter.\nCliquez sur les lignes (Ctrl+Clic pour plusieurs sélections).").showAndWait();
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fc.setInitialFileName("taches_agrigo.pdf");
        File f = fc.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
        if (f == null) return;
        try {
            ExportPdfService.exportTachesToPdf(new java.util.HashSet<>(selected), f.getAbsolutePath());
            // Ouvrir automatiquement le PDF après export si possible
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(f);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Alert(Alert.AlertType.INFORMATION, selected.size() + " tâche(s) exportée(s) : " + f.getAbsolutePath()).showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible d'exporter le PDF.").showAndWait();
        }
    }
}
