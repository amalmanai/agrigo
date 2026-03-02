package Controllers;

import Entites.Tache;
import Services.ServiceTache;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.util.ResourceBundle;

public class ModifierTacheController implements Initializable {

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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tftypeModif.getItems().addAll("Irrigation", "Récolte", "Semis", "Maintenance");
        tfstatusModif.getItems().addAll("Assignée", "En cours", "Terminée", "Annulée");
    }

    // Méthode appelée depuis ListeTacheController
    public void setTache(Tache tache) {
        this.currentTache = tache;

        TFtitreModif.setText(tache.getTitre_tache());
        TFdescriptionModif.setText(tache.getDescription_tache());
        tftypeModif.setValue(tache.getType_tache());
        tfdateModif.setValue(tache.getDate_tache().toLocalDate());
        String hd = tache.getHeure_debut_tache() != null ? tache.getHeure_debut_tache().toString() : "";
        String hf = tache.getHeure_fin_tache() != null ? tache.getHeure_fin_tache().toString() : "";
        tfheuredebutModif.setText(hd.length() >= 5 ? hd.substring(0, 5) : hd);
        tfheurefinModif.setText(hf.length() >= 5 ? hf.substring(0, 5) : hf);
        tfstatusModif.setValue(tache.getStatus_tache());
        tfremarqueModif.setText(tache.getRemarque_tache());
    }

    @FXML
    public void modifiertache(ActionEvent actionEvent) {
        // Contrôle de saisie
        if (TFtitreModif.getText().isEmpty() || TFdescriptionModif.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "le Titre et la description sont obligatoires !");
            alert.show();
            return;
        }

        try {
            // Mettre à jour l'objet
            currentTache.setTitre_tache(TFtitreModif.getText());
            currentTache.setDescription_tache(TFdescriptionModif.getText());
            currentTache.setType_tache(tftypeModif.getValue());
            currentTache.setDate_tache(Date.valueOf(tfdateModif.getValue()));
            String hd = tfheuredebutModif.getText().trim();
            String hf = tfheurefinModif.getText().trim();
            if (!hd.contains(":")) hd += ":00";
            if (!hf.contains(":")) hf += ":00";
            if (hd.length() == 5) hd += ":00";
            if (hf.length() == 5) hf += ":00";
            currentTache.setHeure_debut_tache(Time.valueOf(hd));
            currentTache.setHeure_fin_tache(Time.valueOf(hf));
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
