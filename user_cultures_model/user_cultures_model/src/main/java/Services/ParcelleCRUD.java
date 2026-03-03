package Services;

import Entites.Parcelle;
import Utils.MyBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Added "implements IntrefaceCRUD<Parcelle>" to fix the controller error
public class ParcelleCRUD implements IntrefaceCRUD<Parcelle> {

    // Initialize connection from Singleton
    private Connection conn = MyBD.getInstance().getConnection();

    @Override
    public void ajouter(Parcelle p) throws SQLException {
        String req = "INSERT INTO parcelles (nom_parcelle, surface, coordonnees_gps, type_sol) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            if (conn == null) {
                throw new SQLException("Connexion à la base de données est nulle !");
            }
            pst.setString(1, p.getNom());
            pst.setDouble(2, p.getSurface());
            pst.setString(3, p.getGps());
            pst.setString(4, p.getTypeSol());
            pst.executeUpdate();
            System.out.println("Parcelle ajoutée avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur d'ajout: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Parcelle> afficher() throws SQLException {
        return afficherParcelles();
    }

    /** Pour listes déroulantes (module irrigation, etc.) */
    public List<Parcelle> afficherToutes() throws SQLException {
        return afficher();
    }

    public List<Parcelle> afficherParcelles() {
        List<Parcelle> list = new ArrayList<>();
        String req = "SELECT * FROM parcelles";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Parcelle p = new Parcelle();
                p.setId(rs.getInt("id_parcelle"));
                p.setNom(rs.getString("nom_parcelle"));
                p.setSurface(rs.getDouble("surface"));
                p.setGps(rs.getString("coordonnees_gps"));
                p.setTypeSol(rs.getString("type_sol"));
                list.add(p);
            }
        } catch (SQLException ex) {
            System.out.println("Erreur Affichage: " + ex.getMessage());
        }
        return list;
    }

    @Override
    public void modifier(Parcelle p) throws SQLException {
        String req = "UPDATE parcelles SET nom_parcelle=?, surface=?, coordonnees_gps=?, type_sol=? WHERE id_parcelle=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, p.getNom());
            pst.setDouble(2, p.getSurface());
            pst.setString(3, p.getGps());
            pst.setString(4, p.getTypeSol());
            pst.setInt(5, p.getId());
            pst.executeUpdate();
            System.out.println("Parcelle mise à jour !");
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM parcelles WHERE id_parcelle = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Parcelle supprimée !");
        }
    }
}

