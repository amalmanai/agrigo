package Controllers;

import Entites.Tache;
import Entites.User;
import Services.ServiceTache;
import Utils.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;

public class AjouterTacheController {

    @FXML private TextField TFtitre;
    @FXML private TextField TFdescription;
    @FXML private ComboBox<String> tftype;
    @FXML private DatePicker tfdate;
    @FXML private TextField tfheuredebut;
    @FXML private TextField tfheurefin;
    @FXML private ComboBox<String> tfstatus;
    @FXML private TextField tfremarque;

    private final ServiceTache serviceTache = new ServiceTache();

    // ⚠️ Ici tu dois récupérer l’utilisateur connecté depuis ton système de login
    // Par exemple : ServiceUser.currentUser (variable statique que tu définis après login)
    User currentUser = Session.getCurrentUser();


    @FXML
    public void initialize() {
        // Remplir combobox type
        tftype.getItems().addAll("Irrigation", "Récolte", "Semis", "Maintenance");

        // Remplir combobox status
        tfstatus.getItems().addAll("Assignée", "En cours", "Terminée", "Annulée");
    }

    @FXML
    public void ajoutertache(ActionEvent actionEvent) {
        try {
            // =================== CONTROLE DE SAISIE ===================
            if (TFtitre.getText().isEmpty() ||
                    TFdescription.getText().isEmpty() ||
                    tftype.getValue() == null ||
                    tfdate.getValue() == null ||
                    tfheuredebut.getText().isEmpty() ||
                    tfheurefin.getText().isEmpty() ||
                    tfstatus.getValue() == null) {

                showAlert("⚠ Tous les champs sont obligatoires !");
                return;
            }

            // Vérification longueur description
            if (TFdescription.getText().trim().length() < 7) {
                showAlert("⚠ La description doit contenir au moins 7 caractères !");
                return;
            }

            // Validation heure format HH:mm
            String heureDebutStr = tfheuredebut.getText().trim();
            String heureFinStr = tfheurefin.getText().trim();

            if (!heureDebutStr.matches("^([01]\\d|2[0-3]):[0-5]\\d$") ||
                    !heureFinStr.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
                showAlert("⚠ Format heure invalide (HH:mm) !");
                return;
            }

            Time heureDebut = Time.valueOf(heureDebutStr + ":00");
            Time heureFin = Time.valueOf(heureFinStr + ":00");

            if (heureFin.before(heureDebut)) {
                showAlert("⚠ L'heure de fin doit être après l'heure de début !");
                return;
            }

            // =================== CREATION TACHE ===================
            Tache tache = new Tache(
                    TFtitre.getText().trim(),
                    TFdescription.getText().trim(),
                    tftype.getValue(),
                    currentUser, // utilisateur connecté
                    Date.valueOf(tfdate.getValue()),
                    heureDebut,
                    heureFin,
                    tfstatus.getValue(),
                    tfremarque.getText().trim()
            );

            serviceTache.ajouter(tache);
            showAlert("✅ Tâche ajoutée avec succès !");
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("⚠ Erreur lors de l'ajout de la tâche !");
        }
    }


    @FXML
    public void voirtaches(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Tache/ListTache.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("voir Taches");
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void toregister(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("");
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =================== UTILS ===================
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.show();
    }

    private void clearFields() {
        TFtitre.clear();
        TFdescription.clear();
        tftype.setValue(null);
        tfdate.setValue(null);
        tfheuredebut.clear();
        tfheurefin.clear();
        tfstatus.setValue(null);
        tfremarque.clear();
    }



}
