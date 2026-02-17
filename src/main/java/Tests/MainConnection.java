package Tests;

import Entites.*;
import Services.*;
import Utils.MyBD;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class MainConnection {
    public static void main(String[] args) {
        System.out.println("--- STEP 1: Connection ---");
        Connection connection = MyBD.getInstance().getConnection();
        if (connection == null) {
            System.out.println("Connection Failed! Vérifiez XAMPP !");
            return;
        }
        System.out.println("Connected to agri_go_db successfully!\n");

        // Initialize Services
        ParcelleCRUD pc = new ParcelleCRUD();
        CultureCRUD cc = new CultureCRUD();
        AlerteCRUD ac = new AlerteCRUD();
        HistoriqueCRUD hc = new HistoriqueCRUD();

        try {
            // --- Parcelle Test ---
            System.out.println("--- STEP 2: Parcelle Operations ---");
            // Uses the constructor we fixed in Parcelle.java
            Parcelle p = new Parcelle("Main Test Plot", 75.0, "36.80, 10.18", "Sableux");

            pc.ajouter(p); // Standardized name

            List<Parcelle> parcelles = pc.afficher(); // Fixed from afficherParcelles()
            int lastParcelleId = parcelles.get(parcelles.size() - 1).getId();
            System.out.println("Created Parcelle ID: " + lastParcelleId);

            // --- Culture Test ---
            System.out.println("\n--- STEP 3: Culture Operations ---");
            Culture cult = new Culture("Blé Premium", Date.valueOf("2026-02-08"), "Croissance", 550.0);

            // If your CultureCRUD handles the parcelle_id inside ajouter:
            cc.ajouter(cult);

            List<Culture> cultures = cc.afficher(); // Fixed from afficherToutes()
            int lastCultureId = cultures.get(cultures.size() - 1).getId();
            System.out.println("Created Culture ID: " + lastCultureId);

            // --- STEP 4: Alerte Test ---
            System.out.println("\n--- STEP 4: Alerte Operations ---");
            Alerte alerte = new Alerte("Parasites", "Détection de pucerons", lastCultureId);
            ac.ajouter(alerte); // Fixed from ajouterAlerte()

            System.out.println("Latest Alerts:");
            ac.afficher().forEach(a -> // Fixed from afficherAlertes()
                    System.out.println("- [" + a.getType() + "] " + a.getDescription())
            );

            // --- STEP 5: Summary ---
            System.out.println("\n--- FINAL SUMMARY ---");
            System.out.println("Total Plots in DB: " + pc.afficher().size());
            System.out.println("Total Active Cultures: " + cc.afficher().size());
            System.out.println("Total Alerts Recorded: " + ac.afficher().size());

        } catch (SQLException e) {
            System.err.println("ERREUR DATABASE: " + e.getMessage());
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Erreur: La liste est vide. L'ajout a probablement échoué.");
        }
    }
}