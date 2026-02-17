package Services;

import Entites.SystemeIrrigation;
import Utils.MyBD;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SystemeIrrigationCRUD implements IntrefaceCRUD<SystemeIrrigation> {

    private Connection conn;

    public SystemeIrrigationCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(SystemeIrrigation s) throws SQLException {
        String sql = "INSERT INTO systeme_irrigation " +
                "(id_parcelle, nom_systeme, seuil_humidite, mode, statut) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setLong(1, s.getIdParcelle());
        pst.setString(2, s.getNomSysteme());
        pst.setBigDecimal(3, s.getSeuilHumidite());
        pst.setString(4, s.getMode());
        pst.setString(5, s.getStatut());
        pst.executeUpdate();
        System.out.println("Système d'irrigation ajouté");
    }

    @Override
    public void modifier(SystemeIrrigation s) throws SQLException {
        String sql = "UPDATE systeme_irrigation SET " +
                "id_parcelle=?, nom_systeme=?, seuil_humidite=?, mode=?, statut=? " +
                "WHERE id_systeme=?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setLong(1, s.getIdParcelle());
        pst.setString(2, s.getNomSysteme());
        pst.setBigDecimal(3, s.getSeuilHumidite());
        pst.setString(4, s.getMode());
        pst.setString(5, s.getStatut());
        pst.setLong(6, s.getIdSysteme());
        pst.executeUpdate();
        System.out.println("Système d'irrigation modifié");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM systeme_irrigation WHERE id_systeme=?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setLong(1, id);
        pst.executeUpdate();
        System.out.println("Système d'irrigation supprimé");
    }

    @Override
    public List<SystemeIrrigation> afficher() throws SQLException {
        String sql = "SELECT * FROM systeme_irrigation";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<SystemeIrrigation> list = new ArrayList<>();

        while (rs.next()) {
            SystemeIrrigation s = new SystemeIrrigation();
            s.setIdSysteme(rs.getLong("id_systeme"));
            s.setIdParcelle(rs.getLong("id_parcelle"));
            s.setNomSysteme(rs.getString("nom_systeme"));
            s.setSeuilHumidite(rs.getBigDecimal("seuil_humidite"));
            s.setMode(rs.getString("mode"));
            s.setStatut(rs.getString("statut"));
            s.setDateCreation(rs.getTimestamp("date_creation"));
            list.add(s);
        }
        return list;
    }
}

