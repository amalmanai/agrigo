package Controlles;

import Entites.HistoriqueIrrigation;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DetailsIrrigationController {

    @FXML private TextField resDate, resSysteme, resDuree, resVolume, resHumidite, resType;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setHistorique(HistoriqueIrrigation h) {
        if (h == null) return;
        if (resDate != null && h.getDateIrrigation() != null) {
            resDate.setText(h.getDateIrrigation().toInstant().atZone(ZoneId.systemDefault()).format(DATE_FORMAT));
        }
        if (resSysteme != null) resSysteme.setText(String.valueOf(h.getIdSysteme()));
        if (resDuree != null) resDuree.setText(String.valueOf(h.getDureeMinutes()));
        if (resVolume != null && h.getVolumeEau() != null) resVolume.setText(h.getVolumeEau().toString());
        if (resHumidite != null && h.getHumiditeAvant() != null) resHumidite.setText(h.getHumiditeAvant().toString());
        if (resType != null) resType.setText(h.getTypeDeclenchement());
    }
}
