package test;

import utils.MyBd;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ConnexionTest {
    public static void main(String[] args) {
        System.out.println("🔄 Tentative de connexion à la base de données...");

        try {
            // 1. Récupération de la connexion via votre Singleton
            Connection c = MyBd.getInstance().getConnection();

            if (c != null && !c.isClosed()) {
                System.out.println("✅ SUCCÈS : Connexion établie avec MySQL !");

                // 2. Vérification si les tables existent
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery("SHOW TABLES");

                System.out.println("📊 Liste des tables trouvées dans 'agri_go_db' :");
                boolean hasTables = false;
                while (rs.next()) {
                    System.out.println("   - " + rs.getString(1));
                    hasTables = true;
                }

                if (!hasTables) {
                    System.out.println("⚠️ Attention : La base est connectée mais elle est VIDE. " +
                            "Exécutez le script SQL pour créer les tables 'recolte' et 'vente'.");
                }
            } else {
                System.err.println("❌ ÉCHEC : La connexion est nulle. Vérifiez si XAMPP/WAMP est lancé.");
            }
        } catch (Exception e) {
            System.err.println("❌ ERREUR CRITIQUE : " + e.getMessage());
            e.printStackTrace();
        }
    }
}