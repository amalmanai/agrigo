package Controlles;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class DetailsController {
    @FXML private TextField resNom;
    @FXML private TextField resEtat;
    @FXML private TextField resRendement;

    // These setters are called by Page1Controller to pass the data
    public void setResNom(String nom) {
        if (resNom != null) this.resNom.setText(nom);
    }

    public void setResEtat(String etat) {
        if (resEtat != null) this.resEtat.setText(etat);
    }

    public void setResRendement(String rend) {
        if (resRendement != null) this.resRendement.setText(rend);
    }
}