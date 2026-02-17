package Controlles;

import Services.HistoriqueIrrigationCRUD;
import Services.SystemeIrrigationCRUD;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.SQLException;

public class TableauBordController {

    @FXML
    private Label lblSystemesActifs;

    @FXML
    private Label lblHistorique;

    @FXML
    private void initialize() {
        try {
            long nbSystemes = new SystemeIrrigationCRUD().afficher().stream()
                    .filter(s -> "ACTIF".equals(s.getStatut())).count();
            int nbHistorique = new HistoriqueIrrigationCRUD().afficherTous().size();
            lblSystemesActifs.setText(String.valueOf(nbSystemes));
            lblHistorique.setText(String.valueOf(nbHistorique));
        } catch (SQLException e) {
            lblSystemesActifs.setText("-");
            lblHistorique.setText("-");
        }
    }
}
