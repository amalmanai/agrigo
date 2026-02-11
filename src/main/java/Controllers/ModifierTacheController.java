package Controllers;

import Entites.Tache;
import Services.ServiceTache;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Date;
import java.sql.Time;

public class ModifierTacheController {

    @FXML private TextField TFtitreModif;
    @FXML private TextField TFdescriptionModif;
    @FXML private ComboBox<String> tftypeModif;
    @FXML private DatePicker tfdateModif;
    @FXML private TextField tfheuredebutModif;
    @FXML private TextField tfheurefinModif;
    @FXML private ComboBox<String> tfstatusModif;
    @FXML private TextField tfremarqueModif;

    private Tache currentTache;
    private final ServiceTache serviceTache = new ServiceTache();

    // Méthode appelée depuis ListeTacheController
    public void setTache(Tache tache) {
        this.currentTache = tache;

        // Pré-remplir les champs
        TFtitreModif.setText(tache.getTitre_tache());
        TFdescriptionModif.setText(tache.getDescription_tache());
        tftypeModif.setValue(tache.getType_tache());
        tfdateModif.setValue(tache.getDate_tache().toLocalDate());
        tfheuredebutModif.setText(tache.getHeure_debut_tache().toString());
        tfheurefinModif.setText(tache.getHeure_fin_tache().toString());
        tfstatusModif.setValue(tache.getStatus_tache());
        tfremarqueModif.setText(tache.getRemarque_tache());
    }

    @FXML
    public void modifiertache(ActionEvent actionEvent) {
        // Contrôle de saisie
        if (TFtitreModif.getText().isEmpty() || TFdescriptionModif.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Titre et description sont obligatoires !");
            alert.show();
            return;
        }

        try {
            // Mettre à jour l'objet
            currentTache.setTitre_tache(TFtitreModif.getText());
            currentTache.setDescription_tache(TFdescriptionModif.getText());
            currentTache.setType_tache(tftypeModif.getValue());
            currentTache.setDate_tache(Date.valueOf(tfdateModif.getValue()));
            currentTache.setHeure_debut_tache(Time.valueOf(tfheuredebutModif.getText()));
            currentTache.setHeure_fin_tache(Time.valueOf(tfheurefinModif.getText()));
            currentTache.setStatus_tache(tfstatusModif.getValue());
            currentTache.setRemarque_tache(tfremarqueModif.getText());

            // Appeler le service
            serviceTache.modifier(currentTache);

            // Fermer la fenêtre
            ((Button) actionEvent.getSource()).getScene().getWindow().hide();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la modification : " + e.getMessage());
            alert.show();
        }
    }
}
