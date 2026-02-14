package Services;

import Entites.Culture;
import Utils.MyBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CultureCRUD implements IntrefaceCRUD<Culture> {
    private Connection cn;

    public CultureCRUD() {
        // Fetch connection from Singleton
        this.cn = MyBD.getInstance().getConnection();

        if (this.cn == null) {
            System.err.println("ERREUR CRITIQUE: La connexion à la base de données a échoué. Vérifiez XAMPP !");
        }
    }

    /**
     * Adds a culture linked to a specific parcel ID.
     * Essential for foreign key constraints.
     */
    public void ajouterCulture(Culture c, int idParcelle) {
        if (cn == null) return;

        String req = "INSERT INTO cultures (nom_culture, date_semis, etat_croissance, rendement_prevu, id_parcelle) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cn.prepareStatement(req)) {
            pst.setString(1, c.getNom());
            // Safe date handling for MySQL
            pst.setDate(2, (c.getDateSemis() != null) ? (java.sql.Date) c.getDateSemis() : new java.sql.Date(System.currentTimeMillis()));
            pst.setString(3, c.getEtat());
            pst.setDouble(4, c.getRendement());
            if (idParcelle <= 0) {
                pst.setNull(5, Types.INTEGER);
            } else {
                pst.setInt(5, idParcelle);
            }
            pst.executeUpdate();
            System.out.println("Culture ajoutée avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur Insertion: " + e.getMessage());
        }
    }

    @Override
    public void ajouter(Culture c) throws SQLException {
        // Default parcel ID as used in your current logic
        ajouterCulture(c, 5);
    }

    @Override
    public void modifier(Culture c) throws SQLException {
        if (cn == null) return;
        // Updates based on id_culture column
        String req = "UPDATE cultures SET nom_culture=?, date_semis=?, etat_croissance=?, rendement_prevu=?, id_parcelle=? WHERE id_culture=?";
        try (PreparedStatement pst = cn.prepareStatement(req)) {
            pst.setString(1, c.getNom());
            pst.setDate(2, (c.getDateSemis() != null) ? (java.sql.Date) c.getDateSemis() : new java.sql.Date(System.currentTimeMillis()));
            pst.setString(3, c.getEtat());
            pst.setDouble(4, c.getRendement());
            if (c.getIdParcelle() <= 0) {
                pst.setNull(5, Types.INTEGER);
            } else {
                pst.setInt(5, c.getIdParcelle());
            }
            pst.setInt(6, c.getId());
            pst.executeUpdate();
            System.out.println("Culture modifiée avec succès dans la base de données !");
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        if (cn == null) return;
        // Matches the logic required for the dashboard delete button
        String req = "DELETE FROM cultures WHERE id_culture = ?";
        try (PreparedStatement pst = cn.prepareStatement(req)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Culture supprimée !");
        }
    }

    @Override
    public List<Culture> afficher() throws SQLException {
        return afficherToutes();
    }

    /**
     * Retrieves all cultures for the Dashboard list.
     * Fixes the 'st' vs 'rs' error and constructor mismatch.
     */
    public List<Culture> afficherToutes() throws SQLException {
        List<Culture> list = new ArrayList<>();
        if (cn == null) return list;

        String req = "SELECT * FROM cultures";
        try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                // Fix: Using empty constructor + setters to avoid "cannot be applied to" errors
                Culture c = new Culture();
                c.setId(rs.getInt("id_culture"));
                c.setNom(rs.getString("nom_culture"));
                c.setEtat(rs.getString("etat_croissance"));
                c.setRendement(rs.getDouble("rendement_prevu"));
                c.setDateSemis(rs.getDate("date_semis"));
                c.setIdParcelle(rs.getInt("id_parcelle"));

                list.add(c);
            }
        } catch (SQLException ex) {
            System.err.println("Erreur Affichage: " + ex.getMessage());
            throw ex;
        }
        return list;
    }
}
