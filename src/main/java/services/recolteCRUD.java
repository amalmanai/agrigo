package services;

import entity.recolte;
import utils.MyBd;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class recolteCRUD {

    // Récupération de la connexion via le Singleton MyBd
    private Connection conn = MyBd.getInstance().getConnection();

    /**
     * AJOUTER UNE RÉCOLTE
     * Note : Les noms des colonnes ici correspondent exactement à ton image phpMyAdmin
     */
    public void ajouter(recolte r) throws SQLException {
        // La requête SQL avec les 6 paramètres (id_recolte est en AUTO_INCREMENT)
        String req = "INSERT INTO recolte (nom_produit, quantite, unite, date_recolte, cout_production, id_user) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, r.getNom_produit());
            ps.setDouble(2, r.getQuantite());
            ps.setString(3, r.getUnite());
            ps.setDate(4, r.getDate_recolte());
            ps.setDouble(5, r.getCout_production());
            ps.setInt(6, 1); // On utilise l'ID utilisateur 1 par défaut pour le test

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ [Base de données] Insertion réussie pour : " + r.getNom_produit());
            } else {
                System.out.println("⚠️ [Base de données] L'insertion a été tentée mais aucune ligne n'a été ajoutée.");
            }
        } catch (SQLException e) {
            System.err.println("❌ [Erreur SQL] Impossible d'ajouter la récolte : " + e.getMessage());
            throw e; // On renvoie l'erreur au contrôleur pour qu'il sache que ça a échoué
        }
    }

    /**
     * AFFICHER TOUTES LES RÉCOLTES
     */
    public List<recolte> afficher() throws SQLException {
        List<recolte> liste = new ArrayList<>();
        String req = "SELECT * FROM recolte";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                // Création de l'objet recolte à partir des données de la ligne actuelle
                recolte r = new recolte(
                        rs.getInt("id_recolte"),
                        rs.getString("nom_produit"),
                        rs.getDouble("quantite"),
                        rs.getString("unite"),
                        rs.getDate("date_recolte"),
                        rs.getDouble("cout_production"),
                        rs.getInt("id_user")
                );
                liste.add(r);
            }
        } catch (SQLException e) {
            System.err.println("❌ [Erreur SQL] Erreur de lecture : " + e.getMessage());
            throw e;
        }
        return liste;
    }

    /**
     * SUPPRIMER UNE RÉCOLTE
     */
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM recolte WHERE id_recolte = ?";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ [Base de données] Récolte ID " + id + " supprimée avec succès.");
            } else {
                System.out.println("⚠️ [Base de données] Aucune récolte trouvée avec l'ID : " + id);
            }
        } catch (SQLException e) {
            System.err.println("❌ [Erreur SQL] Suppression impossible : " + e.getMessage());
            throw e;
        }
    }

    /**
     * MODIFIER UNE RÉCOLTE (Optionnel mais recommandé)
     */
    public void modifier(recolte r) throws SQLException {
        String req = "UPDATE recolte SET nom_produit=?, quantite=?, unite=?, date_recolte=?, cout_production=? WHERE id_recolte=?";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, r.getNom_produit());
            ps.setDouble(2, r.getQuantite());
            ps.setString(3, r.getUnite());
            ps.setDate(4, r.getDate_recolte());
            ps.setDouble(5, r.getCout_production());
            ps.setInt(6, r.getId_recolte());

            ps.executeUpdate();
            System.out.println("✅ [Base de données] Récolte mise à jour !");
        } catch (SQLException e) {
            System.err.println("❌ [Erreur SQL] Mise à jour échouée : " + e.getMessage());
            throw e;
        }
    }
}