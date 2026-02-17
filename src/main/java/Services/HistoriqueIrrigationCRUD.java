package Services;

import Entites.HistoriqueIrrigation;
import Utils.MyBD;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoriqueIrrigationCRUD {

    private Connection conn;

    public HistoriqueIrrigationCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    /**
     * Contrôle de saisie simple pour un HistoriqueIrrigation.
     * @throws IllegalArgumentException si une donnée est invalide
     */
    private void valider(HistoriqueIrrigation h) {
        if (h == null) {
            throw new IllegalArgumentException("L'historique ne peut pas être null.");
        }
        if (h.getIdSysteme() <= 0) {
            throw new IllegalArgumentException("L'id du système doit être strictement positif.");
        }
        if (h.getDureeMinutes() <= 0) {
            throw new IllegalArgumentException("La durée en minutes doit être strictement positive.");
        }
        if (h.getVolumeEau() == null || h.getVolumeEau().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le volume d'eau doit être renseigné et positif ou nul.");
        }
        if (h.getHumiditeAvant() == null || h.getHumiditeAvant().compareTo(BigDecimal.ZERO) < 0
                || h.getHumiditeAvant().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("L'humidité avant doit être entre 0 et 100.");
        }
        if (h.getTypeDeclenchement() == null || h.getTypeDeclenchement().trim().isEmpty()) {
            throw new IllegalArgumentException("Le type de déclenchement est obligatoire.");
        }
        String type = h.getTypeDeclenchement().trim().toUpperCase();
        if (!"AUTO".equals(type) && !"MANUEL".equals(type)) {
            throw new IllegalArgumentException("Le type de déclenchement doit être AUTO ou MANUEL.");
        }
    }

    public void ajouter(HistoriqueIrrigation h) throws SQLException {
        valider(h);
        String sql = "INSERT INTO historique_irrigation " +
                "(id_systeme, duree_minutes, volume_eau, humidite_avant, type_declenchement) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setLong(1, h.getIdSysteme());
        pst.setInt(2, h.getDureeMinutes());
        pst.setBigDecimal(3, h.getVolumeEau());
        pst.setBigDecimal(4, h.getHumiditeAvant());
        pst.setString(5, h.getTypeDeclenchement());
        pst.executeUpdate();
        System.out.println("Historique d'irrigation ajouté");
    }

    public void modifier(HistoriqueIrrigation h) throws SQLException {
        valider(h);
        if (h.getId() <= 0) {
            throw new IllegalArgumentException("L'id de l'historique doit être strictement positif pour la modification.");
        }
        String sql = "UPDATE historique_irrigation SET " +
                "id_systeme=?, duree_minutes=?, volume_eau=?, humidite_avant=?, type_declenchement=? " +
                "WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setLong(1, h.getIdSysteme());
        pst.setInt(2, h.getDureeMinutes());
        pst.setBigDecimal(3, h.getVolumeEau());
        pst.setBigDecimal(4, h.getHumiditeAvant());
        pst.setString(5, h.getTypeDeclenchement());
        pst.setLong(6, h.getId());
        pst.executeUpdate();
        System.out.println("Historique d'irrigation modifié");
    }

    public void supprimer(long id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("L'id doit être strictement positif pour la suppression.");
        }
        String sql = "DELETE FROM historique_irrigation WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setLong(1, id);
        pst.executeUpdate();
        System.out.println("Historique d'irrigation supprimé");
    }

    public List<HistoriqueIrrigation> afficherTous() throws SQLException {
        String sql = "SELECT * FROM historique_irrigation ORDER BY date_irrigation DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<HistoriqueIrrigation> list = new ArrayList<>();
        while (rs.next()) {
            HistoriqueIrrigation h = new HistoriqueIrrigation();
            h.setId(rs.getLong("id"));
            h.setIdSysteme(rs.getLong("id_systeme"));
            h.setDateIrrigation(rs.getTimestamp("date_irrigation"));
            h.setDureeMinutes(rs.getInt("duree_minutes"));
            h.setVolumeEau(rs.getBigDecimal("volume_eau"));
            h.setHumiditeAvant(rs.getBigDecimal("humidite_avant"));
            h.setTypeDeclenchement(rs.getString("type_declenchement"));
            list.add(h);
        }
        return list;
    }

    public List<HistoriqueIrrigation> afficherParSysteme(long idSysteme) throws SQLException {
        String sql = "SELECT * FROM historique_irrigation " +
                "WHERE id_systeme=? ORDER BY date_irrigation DESC";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setLong(1, idSysteme);
        ResultSet rs = pst.executeQuery();
        List<HistoriqueIrrigation> list = new ArrayList<>();

        while (rs.next()) {
            HistoriqueIrrigation h = new HistoriqueIrrigation();
            h.setId(rs.getLong("id"));
            h.setIdSysteme(rs.getLong("id_systeme"));
            h.setDateIrrigation(rs.getTimestamp("date_irrigation"));
            h.setDureeMinutes(rs.getInt("duree_minutes"));
            h.setVolumeEau(rs.getBigDecimal("volume_eau"));
            h.setHumiditeAvant(rs.getBigDecimal("humidite_avant"));
            h.setTypeDeclenchement(rs.getString("type_declenchement"));
            list.add(h);
        }
        return list;
    }
}

