package Services;

import Entites.Tache;
import Entites.User;
import Utils.MyBD;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class ServiceTache {

    Connection cnx = MyBD.getInstance().getConn();

    // ================== AJOUT ==================
    public void ajouter(Tache tache) {
        String req = "INSERT INTO tache (tittre_tache, description_tache, type_tache, id_user, date_tache, heure_debut_tache, heure_fin_tache, status_tache, remarque_tache) VALUES (?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, tache.getTitre_tache());
            ps.setString(2, tache.getDescription_tache());
            ps.setString(3, tache.getType_tache());
            ps.setInt(4, tache.getUser().getId_user());
            ps.setDate(5, tache.getDate_tache());
            ps.setTime(6, tache.getHeure_debut_tache());
            ps.setTime(7, tache.getHeure_fin_tache());
            ps.setString(8, tache.getStatus_tache());
            ps.setString(9, tache.getRemarque_tache());
            ps.executeUpdate();
            System.out.println("✅ Tâche ajoutée !");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================== UPDATE ==================
    public void modifier(Tache tache) {
        String req = "UPDATE tache SET tittre_tache=?, description_tache=?, type_tache=?, id_user=?, date_tache=?, heure_debut_tache=?, heure_fin_tache=?, status_tache=?, remarque_tache=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, tache.getTitre_tache());
            ps.setString(2, tache.getDescription_tache());
            ps.setString(3, tache.getType_tache());
            ps.setInt(4, tache.getUser().getId_user());
            ps.setDate(5, tache.getDate_tache());
            ps.setTime(6, tache.getHeure_debut_tache());
            ps.setTime(7, tache.getHeure_fin_tache());
            ps.setString(8, tache.getStatus_tache());
            ps.setString(9, tache.getRemarque_tache());
            ps.setInt(10, tache.getId());
            ps.executeUpdate();
            System.out.println("✅ Tâche modifiée !");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================== DELETE ==================
    public void supprimer(int id) {
        String req = "DELETE FROM tache WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Tâche supprimée !");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================== GET ALL ==================
    public Set<Tache> getAll() {
        Set<Tache> taches = new HashSet<>();
        String req = "SELECT * FROM tache";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Tache t = new Tache(
                        rs.getInt("id"),
                        rs.getString("tittre_tache"),
                        rs.getString("description_tache"),
                        rs.getString("type_tache"),
                        new User(rs.getInt("id_user")),
                        rs.getDate("date_tache"),
                        rs.getTime("heure_debut_tache"),
                        rs.getTime("heure_fin_tache"),
                        rs.getString("status_tache"),
                        rs.getString("remarque_tache")
                );
                taches.add(t);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return taches;
    }

    // ================== GET BY ID ==================
    public Tache getOneByID(int id) {
        String req = "SELECT * FROM tache WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Tache(
                        rs.getInt("id"),
                        rs.getString("tittre_tache"),
                        rs.getString("description_tache"),
                        rs.getString("type_tache"),
                        new User(rs.getInt("id_user")),
                        rs.getDate("date_tache"),
                        rs.getTime("heure_debut_tache"),
                        rs.getTime("heure_fin_tache"),
                        rs.getString("status_tache"),
                        rs.getString("remarque_tache")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // ================== GET BY USER ==================
    public Set<Tache> getByUser(int id_user) {
        Set<Tache> taches = new HashSet<>();
        String req = "SELECT * FROM tache WHERE id_user=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id_user);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Tache t = new Tache(
                        rs.getInt("id"),
                        rs.getString("tittre_tache"),
                        rs.getString("description_tache"),
                        rs.getString("type_tache"),
                        new User(rs.getInt("id_user")),
                        rs.getDate("date_tache"),
                        rs.getTime("heure_debut_tache"),
                        rs.getTime("heure_fin_tache"),
                        rs.getString("status_tache"),
                        rs.getString("remarque_tache")
                );
                taches.add(t);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return taches;
    }
}
