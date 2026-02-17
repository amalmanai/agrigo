package Controlles;

import Entites.HistoriqueIrrigation;
import Entites.SystemeIrrigation;
import Services.HistoriqueIrrigationCRUD;
import Services.SystemeIrrigationCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VueAvanceeController {

    @FXML private ComboBox<SystemeIrrigation> comboSysteme;
    @FXML private TableView<HistoriqueIrrigation> tvHistorique;
    @FXML private TableColumn<HistoriqueIrrigation, Long> colId, colIdSysteme;
    @FXML private TableColumn<HistoriqueIrrigation, Timestamp> colDate;
    @FXML private TableColumn<HistoriqueIrrigation, Integer> colDuree;
    @FXML private TableColumn<HistoriqueIrrigation, BigDecimal> colVolume, colHumidite;
    @FXML private TableColumn<HistoriqueIrrigation, String> colType;

    private final HistoriqueIrrigationCRUD serviceHistorique = new HistoriqueIrrigationCRUD();
    private final SystemeIrrigationCRUD serviceSysteme = new SystemeIrrigationCRUD();
    private final ObservableList<HistoriqueIrrigation> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colIdSysteme.setCellValueFactory(new PropertyValueFactory<>("idSysteme"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateIrrigation"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeMinutes"));
        colVolume.setCellValueFactory(new PropertyValueFactory<>("volumeEau"));
        colHumidite.setCellValueFactory(new PropertyValueFactory<>("humiditeAvant"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeDeclenchement"));
        tvHistorique.setItems(data);

        comboSysteme.setConverter(new javafx.util.StringConverter<SystemeIrrigation>() {
            @Override public String toString(SystemeIrrigation s) {
                return s == null ? "Tous" : s.getIdSysteme() <= 0 ? "Tous" : s.getNomSysteme();
            }
            @Override public SystemeIrrigation fromString(String t) { return null; }
        });

        try {
            SystemeIrrigation tous = new SystemeIrrigation();
            tous.setIdSysteme(-1);
            tous.setNomSysteme("Tous");
            comboSysteme.getItems().add(tous);
            comboSysteme.getItems().addAll(serviceSysteme.afficher());
            comboSysteme.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            // BD inaccessible - combo vide
        }
        charger();
    }

    @FXML private void onFilterChange() {
        charger();
    }

    private void charger() {
        data.clear();
        try {
            SystemeIrrigation sel = comboSysteme.getSelectionModel().getSelectedItem();
            List<HistoriqueIrrigation> list;
            if (sel != null && sel.getIdSysteme() > 0) {
                list = serviceHistorique.afficherParSysteme(sel.getIdSysteme());
            } else {
                list = serviceHistorique.afficherTous();
            }
            data.addAll(list);
        } catch (SQLException e) {
            // Ne pas bloquer
        }
    }

    private Dialog<HistoriqueIrrigation> creerFormulaireAjout(HistoriqueIrrigation existant, List<SystemeIrrigation> systemes) {
        Dialog<HistoriqueIrrigation> dialog = new Dialog<>();
        dialog.setTitle(existant == null ? "Ajouter une entrée" : "Modifier l'entrée");
        dialog.setHeaderText("Tous les champs de la table historique_irrigation");

        ButtonType btnValider = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        ComboBox<SystemeIrrigation> comboIdSysteme = new ComboBox<>(FXCollections.observableArrayList(systemes));
        comboIdSysteme.setConverter(new javafx.util.StringConverter<SystemeIrrigation>() {
            @Override public String toString(SystemeIrrigation s) {
                return s == null ? "" : s.getIdSysteme() + " - " + s.getNomSysteme();
            }
            @Override public SystemeIrrigation fromString(String t) { return null; }
        });
        if (!systemes.isEmpty()) comboIdSysteme.getSelectionModel().selectFirst();

        TextField tfDuree = new TextField(existant != null ? String.valueOf(existant.getDureeMinutes()) : "30");
        TextField tfVolume = new TextField(existant != null && existant.getVolumeEau() != null ? existant.getVolumeEau().toString() : "100");
        TextField tfHumidite = new TextField(existant != null && existant.getHumiditeAvant() != null ? existant.getHumiditeAvant().toString() : "30");
        ComboBox<String> comboType = new ComboBox<>(FXCollections.observableArrayList("AUTO", "MANUEL"));
        comboType.getSelectionModel().select(existant != null ? existant.getTypeDeclenchement() : "MANUEL");
        comboType.setMaxWidth(Double.MAX_VALUE);

        if (existant != null) {
            for (SystemeIrrigation s : systemes) {
                if (s.getIdSysteme() == existant.getIdSysteme()) {
                    comboIdSysteme.getSelectionModel().select(s);
                    break;
                }
            }
        }

        grid.add(new Label("id_systeme *:"), 0, 0);
        grid.add(comboIdSysteme, 1, 0);
        grid.add(new Label("duree_minutes *:"), 0, 1);
        grid.add(tfDuree, 1, 1);
        grid.add(new Label("volume_eau:"), 0, 2);
        grid.add(tfVolume, 1, 2);
        grid.add(new Label("humidite_avant:"), 0, 3);
        grid.add(tfHumidite, 1, 3);
        grid.add(new Label("type_declenchement *:"), 0, 4);
        grid.add(comboType, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt == btnValider) {
                SystemeIrrigation sys = comboIdSysteme.getSelectionModel().getSelectedItem();
                if (sys == null) return null;
                try {
                    HistoriqueIrrigation h = existant != null ? existant : new HistoriqueIrrigation();
                    h.setIdSysteme(sys.getIdSysteme());
                    h.setDureeMinutes(Integer.parseInt(tfDuree.getText().trim()));
                    h.setVolumeEau(new BigDecimal(tfVolume.getText().trim()));
                    h.setHumiditeAvant(new BigDecimal(tfHumidite.getText().trim()));
                    h.setTypeDeclenchement(comboType.getSelectionModel().getSelectedItem());
                    return h;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });
        return dialog;
    }

    @FXML private void handleAdd(ActionEvent e) {
        List<SystemeIrrigation> sysList = new ArrayList<>();
        try {
            sysList.addAll(serviceSysteme.afficher());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Impossible de charger les systèmes");
            return;
        }
        if (sysList.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Aucun système d'irrigation. Créez-en un d'abord.");
            return;
        }
        Dialog<HistoriqueIrrigation> dialog = creerFormulaireAjout(null, sysList);
        dialog.showAndWait().ifPresent(h -> {
            try {
                serviceHistorique.ajouter(h);
                charger();
                JOptionPane.showMessageDialog(null, "Entrée ajoutée !");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Erreur: " + ex.getMessage());
            }
        });
    }

    @FXML private void handleUpdate(ActionEvent e) {
        HistoriqueIrrigation sel = tvHistorique.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(null, "Sélectionnez une entrée");
            return;
        }
        List<SystemeIrrigation> sysList = new ArrayList<>();
        try {
            sysList.addAll(serviceSysteme.afficher());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Impossible de charger les systèmes");
            return;
        }
        Dialog<HistoriqueIrrigation> dialog = creerFormulaireAjout(sel, sysList);
        dialog.showAndWait().ifPresent(h -> {
            try {
                serviceHistorique.modifier(h);
                charger();
                JOptionPane.showMessageDialog(null, "Entrée modifiée");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Erreur: " + ex.getMessage());
            }
        });
    }

    @FXML private void handleDelete(ActionEvent e) {
        HistoriqueIrrigation sel = tvHistorique.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(null, "Sélectionnez une entrée");
            return;
        }
        try {
            serviceHistorique.supprimer(sel.getId());
            charger();
            JOptionPane.showMessageDialog(null, "Entrée supprimée");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erreur: " + ex.getMessage());
        }
    }
}
