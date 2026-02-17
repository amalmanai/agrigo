package Controlles;

import Entites.HistoriqueIrrigation;
import Entites.SystemeIrrigation;
import Services.HistoriqueIrrigationCRUD;
import Services.SystemeIrrigationCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class DashboardController {

    @FXML private Label labelIrrigations, labelIrrigationsTrend;
    @FXML private Label labelVolume, labelVolumeStatus;
    @FXML private Label labelSystemes, labelSystemesDetail;
    @FXML private TextField searchField;
    @FXML private Button tabSystemes, btnAjouter;
    @FXML private TableView<HistoriqueIrrigation> tvHistorique;
    @FXML private TableColumn<HistoriqueIrrigation, Timestamp> colDate;
    @FXML private TableColumn<HistoriqueIrrigation, Long> colSysteme;
    @FXML private TableColumn<HistoriqueIrrigation, Integer> colDuree;
    @FXML private TableColumn<HistoriqueIrrigation, BigDecimal> colVolume, colHumidite;
    @FXML private TableColumn<HistoriqueIrrigation, String> colType;
    @FXML private TableColumn<HistoriqueIrrigation, Void> colActions;

    private final HistoriqueIrrigationCRUD serviceHistorique = new HistoriqueIrrigationCRUD();
    private final SystemeIrrigationCRUD serviceSysteme = new SystemeIrrigationCRUD();
    private final ObservableList<HistoriqueIrrigation> data = FXCollections.observableArrayList();
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateIrrigation"));
        colSysteme.setCellValueFactory(new PropertyValueFactory<>("idSysteme"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeMinutes"));
        colVolume.setCellValueFactory(new PropertyValueFactory<>("volumeEau"));
        colHumidite.setCellValueFactory(new PropertyValueFactory<>("humiditeAvant"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeDeclenchement"));

        colActions.setCellFactory(actionsColumn());
        tvHistorique.setItems(data);

        searchField.textProperty().addListener((o, oldVal, newVal) -> filtrerTableau(newVal));

        charger();
        mettreAJourCartes();
    }

    private Callback<TableColumn<HistoriqueIrrigation, Void>, TableCell<HistoriqueIrrigation, Void>> actionsColumn() {
        return col -> new TableCell<>() {
            private final Button btnEdit = new Button("✎");
            private final Button btnDelete = new Button("🗑");
            private final HBox box = new HBox(8, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("btn-edit-cell");
                btnDelete.getStyleClass().add("btn-delete-cell");
                btnEdit.setOnAction(e -> {
                    HistoriqueIrrigation h = getTableRow().getItem();
                    if (h != null) modifierLigne(h);
                });
                btnDelete.setOnAction(e -> {
                    HistoriqueIrrigation h = getTableRow().getItem();
                    if (h != null) supprimerLigne(h);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        };
    }

    private void filtrerTableau(String texte) {
        if (texte == null || texte.isBlank()) {
            tvHistorique.setItems(data);
            return;
        }
        String q = texte.trim().toLowerCase();
        FilteredList<HistoriqueIrrigation> filtered = data.filtered(h ->
                String.valueOf(h.getIdSysteme()).toLowerCase().contains(q) ||
                (h.getTypeDeclenchement() != null && h.getTypeDeclenchement().toLowerCase().contains(q)) ||
                (h.getDateIrrigation() != null && h.getDateIrrigation().toString().toLowerCase().contains(q))
        );
        tvHistorique.setItems(FXCollections.observableArrayList(filtered));
    }

    private void charger() {
        data.clear();
        try {
            data.addAll(serviceHistorique.afficherTous());
        } catch (SQLException e) {
            // ignore
        }
    }

    private void mettreAJourCartes() {
        try {
            List<HistoriqueIrrigation> tous = serviceHistorique.afficherTous();
            LocalDateTime debutMois = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            long ceMois = tous.stream()
                    .filter(h -> h.getDateIrrigation() != null &&
                            h.getDateIrrigation().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().isAfter(debutMois))
                    .count();
            BigDecimal volumeTotal = tous.stream()
                    .map(HistoriqueIrrigation::getVolumeEau)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            labelIrrigations.setText(String.valueOf(ceMois));
            labelIrrigationsTrend.setText("ce mois");
            labelVolume.setText(volumeTotal.stripTrailingZeros().toPlainString() + " L");
            labelVolumeStatus.setText("✔ Stable");

            List<SystemeIrrigation> systemes = serviceSysteme.afficher();
            long actifs = systemes.stream().filter(s -> "ACTIF".equals(s.getStatut())).count();
            labelSystemes.setText(String.valueOf(actifs));
            labelSystemesDetail.setText(systemes.size() + " système(s) au total");
        } catch (SQLException e) {
            labelIrrigations.setText("0");
            labelVolume.setText("0");
            labelSystemes.setText("0");
        }
    }

    @FXML
    private void onTabHistorique() {
        // déjà sur Historique
    }

    @FXML
    private void onTabSystemes() {
        if (mainController != null) mainController.showSystemeIrrigation();
    }

    @FXML
    private void handleAdd() {
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
        Dialog<HistoriqueIrrigation> dialog = creerFormulaire(null, sysList);
        dialog.showAndWait().ifPresent(h -> {
            try {
                serviceHistorique.ajouter(h);
                charger();
                mettreAJourCartes();
                JOptionPane.showMessageDialog(null, "Irrigation ajoutée.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, "Données invalides: " + ex.getMessage());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Erreur: " + ex.getMessage());
            }
        });
    }

    private void modifierLigne(HistoriqueIrrigation h) {
        List<SystemeIrrigation> sysList = new ArrayList<>();
        try {
            sysList.addAll(serviceSysteme.afficher());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Impossible de charger les systèmes");
            return;
        }
        Dialog<HistoriqueIrrigation> dialog = creerFormulaire(h, sysList);
        dialog.showAndWait().ifPresent(updated -> {
            try {
                serviceHistorique.modifier(updated);
                charger();
                mettreAJourCartes();
                JOptionPane.showMessageDialog(null, "Irrigation modifiée.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, "Données invalides: " + ex.getMessage());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Erreur: " + ex.getMessage());
            }
        });
    }

    private void supprimerLigne(HistoriqueIrrigation h) {
        int ok = JOptionPane.showConfirmDialog(null, "Supprimer cette irrigation ?", "Confirmer", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            serviceHistorique.supprimer(h.getId());
            charger();
            mettreAJourCartes();
            JOptionPane.showMessageDialog(null, "Irrigation supprimée.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erreur: " + ex.getMessage());
        }
    }

    private Dialog<HistoriqueIrrigation> creerFormulaire(HistoriqueIrrigation existant, List<SystemeIrrigation> systemes) {
        Dialog<HistoriqueIrrigation> dialog = new Dialog<>();
        dialog.setTitle(existant == null ? "Ajouter une irrigation" : "Modifier l'irrigation");
        dialog.setHeaderText("Renseignez les champs");

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

        grid.add(new Label("Système *:"), 0, 0);
        grid.add(comboIdSysteme, 1, 0);
        grid.add(new Label("Durée (min) *:"), 0, 1);
        grid.add(tfDuree, 1, 1);
        grid.add(new Label("Volume eau (L):"), 0, 2);
        grid.add(tfVolume, 1, 2);
        grid.add(new Label("Humidité avant (0-100):"), 0, 3);
        grid.add(tfHumidite, 1, 3);
        grid.add(new Label("Type *:"), 0, 4);
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

    public void refresh() {
        charger();
        mettreAJourCartes();
    }
}
