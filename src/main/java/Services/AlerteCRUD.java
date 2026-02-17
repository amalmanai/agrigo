package Services;

import Entites.Alerte;
import Utils.MyBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Implement the interface to fix the "Cannot resolve method" errors
public class AlerteCRUD implements IntrefaceCRUD<Alerte> {
    Connection conn = MyBD.getInstance().getConnection();

    @Override
    public void ajouter(Alerte a) throws SQLException { // Renamed from ajouterAlerte
        String req = "INSERT INTO alertes_risques (type_alerte, description, id_culture) VALUES (?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, a.getType());
        pst.setString(2, a.getDescription());
        pst.setInt(3, a.getIdCulture());
        pst.executeUpdate();
        System.out.println("Alerte ajoutée !");
    }

    @Override
    public List<Alerte> afficher() throws SQLException { // Renamed from afficherAlertes
        List<Alerte> list = new ArrayList<>();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM alertes_risques");
        while (rs.next()) {
            Alerte a = new Alerte();
            a.setId(rs.getInt("id_alerte"));
            a.setType(rs.getString("type_alerte"));
            a.setDescription(rs.getString("description"));
            a.setDate(rs.getTimestamp("date_alerte"));
            a.setIdCulture(rs.getInt("id_culture"));
            list.add(a);
        }
        return list;
    }

    @Override
    public void modifier(Alerte a) throws SQLException {
        String req = "UPDATE alertes_risques SET type_alerte=?, description=? WHERE id_alerte=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, a.getType());
        pst.setString(2, a.getDescription());
        pst.setInt(3, a.getId());
        pst.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM alertes_risques WHERE id_alerte = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    // Keep this specialized method for logic in Culture management
    public void supprimerAlertesParCulture(int idCulture) {
        String req = "DELETE FROM alertes_risques WHERE id_culture = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(req);
            pst.setInt(1, idCulture);
            pst.executeUpdate();
        } catch (SQLException e) { System.out.println(e.getMessage()); }
    }
}