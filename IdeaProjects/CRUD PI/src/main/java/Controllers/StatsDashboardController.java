package Controllers;

import Services.ServiceTache;
import Services.ServiceUser;
import Entites.Tache;
import Entites.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Fonctionnalité avancée 2 : Tableau de bord statistiques (nombre utilisateurs, tâches, par statut).
 */
public class StatsDashboardController {

    @FXML private Label labelTotalUsers;
    @FXML private Label labelTotalTaches;
    @FXML private Label labelTachesEnCours;
    @FXML private Label labelTachesTerminees;
    @FXML private Label labelDerniereMaj;
    @FXML private PieChart pieTachesParRole;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceTache serviceTache = new ServiceTache();

    @FXML
    public void initialize() {
        int users = serviceUser.getAll().size();
        Set<Tache> all = serviceTache.getAll();
        long enCours = all.stream().filter(t -> t.getStatus_tache() != null && t.getStatus_tache().toLowerCase().contains("cours")).count();
        long terminees = all.stream().filter(t -> t.getStatus_tache() != null && t.getStatus_tache().toLowerCase().contains("termin")).count();

        labelTotalUsers.setText(String.valueOf(users));
        labelTotalTaches.setText(String.valueOf(all.size()));
        labelTachesEnCours.setText(String.valueOf(enCours));
        labelTachesTerminees.setText(String.valueOf(terminees));
        if (labelDerniereMaj != null) {
            labelDerniereMaj.setText("Dernière mise à jour : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }

        buildPieChartTachesParRole(all);
    }

    private void buildPieChartTachesParRole(Set<Tache> allTaches) {
        if (pieTachesParRole == null) return;
        pieTachesParRole.setTitle("Répartition des tâches par type d'utilisateur");

        if (allTaches == null || allTaches.isEmpty()) {
            pieTachesParRole.setData(FXCollections.observableArrayList());
            return;
        }

        // Compter le nombre de tâches par rôle utilisateur
        Map<String, Integer> countByRole = new HashMap<>();
        for (Tache t : allTaches) {
            User u = t.getUser();
            String role = "Inconnu";
            if (u != null) {
                User fullUser = serviceUser.getOneByID(u.getId_user());
                if (fullUser != null && fullUser.getRole_user() != null && !fullUser.getRole_user().isBlank()) {
                    role = fullUser.getRole_user();
                }
            }
            countByRole.merge(role, 1, Integer::sum);
        }

        int totalTaches = allTaches.size();
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> e : countByRole.entrySet()) {
            String role = e.getKey();
            int count = e.getValue();
            double pct = (count * 100.0) / totalTaches;
            String label = String.format("%s (%.1f%%)", role, pct);
            data.add(new PieChart.Data(label, count));
        }

        pieTachesParRole.setData(data);
    }

    @FXML
    public void retour(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MainGuiAdmin.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
