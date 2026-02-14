package Services;

import Entites.Historique;
import Utils.MyBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoriqueCRUD {
    Connection conn = MyBD.getInstance().getConnection();

    public void archiverCulture(Historique h) {
        String req = "INSERT INTO historique_cultures (id_parcelle, ancienne_culture, date_recolte_effective, rendement_final) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pst = conn.prepareStatement(req);
            pst.setInt(1, h.getIdParcelle());
            pst.setString(2, h.getAncienneCulture());
            pst.setDate(3, h.getDateRecolte());
            pst.setDouble(4, h.getRendementFinal());
            pst.executeUpdate();
            System.out.println("Culture archivée avec succès !");
        } catch (SQLException e) { System.out.println(e.getMessage()); }
    }

    public List<Historique> afficherHistorique() {
        List<Historique> list = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM historique_cultures");
            while (rs.next()) {
                Historique h = new Historique();
                h.setId(rs.getInt("id_historique"));
                h.setIdParcelle(rs.getInt("id_parcelle"));
                h.setAncienneCulture(rs.getString("ancienne_culture"));
                h.setDateRecolte(rs.getDate("date_recolte_effective"));
                h.setRendementFinal(rs.getDouble("rendement_final"));
                list.add(h);
            }
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return list;
    }
}