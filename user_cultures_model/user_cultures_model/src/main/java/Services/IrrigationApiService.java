package Services;

import Services.Dto.ConsommationParcelleDto;
import Services.Dto.RecommandationIrrigationDto;
import Utils.MyBD;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Deux APIs irrigation : consommation par parcelle, recommandations.
 */
public class IrrigationApiService {

    private Connection getConn() {
        return MyBD.getInstance().getConn();
    }

    public ConsommationParcelleDto getConsommationEauParParcelle(int idParcelle, LocalDate dateDebut, LocalDate dateFin) throws SQLException {
        ConsommationParcelleDto dto = new ConsommationParcelleDto();
        dto.setIdParcelle(idParcelle);
        dto.setDateDebut(dateDebut != null ? dateDebut.toString() : null);
        dto.setDateFin(dateFin != null ? dateFin.toString() : null);
        dto.setVolumeTotalEau(BigDecimal.ZERO);
        dto.setNombreIrrigations(0);
        Connection conn = getConn();
        if (conn == null) return dto;
        String sqlParcelle = "SELECT nom_parcelle, surface FROM parcelles WHERE id_parcelle = ?";
        try (PreparedStatement pst = conn.prepareStatement(sqlParcelle)) {
            pst.setInt(1, idParcelle);
            ResultSet rs = pst.executeQuery();
            if (!rs.next()) return dto;
            dto.setNomParcelle(rs.getString("nom_parcelle"));
            dto.setSurface(rs.getDouble("surface"));
        }
        StringBuilder sql = new StringBuilder(
                "SELECT COALESCE(SUM(hi.volume_eau), 0) AS volume_total, COUNT(hi.id) AS nb_irrigations " +
                "FROM historique_irrigation hi " +
                "JOIN systeme_irrigation si ON hi.id_systeme = si.id_systeme " +
                "WHERE si.id_parcelle = ?");
        if (dateDebut != null) sql.append(" AND DATE(hi.date_irrigation) >= ?");
        if (dateFin != null) sql.append(" AND DATE(hi.date_irrigation) <= ?");
        try (PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            int i = 1;
            pst.setInt(i++, idParcelle);
            if (dateDebut != null) pst.setDate(i++, Date.valueOf(dateDebut));
            if (dateFin != null) pst.setDate(i, Date.valueOf(dateFin));
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                dto.setVolumeTotalEau(rs.getBigDecimal("volume_total"));
                dto.setNombreIrrigations(rs.getInt("nb_irrigations"));
            }
        }
        return dto;
    }

    public List<RecommandationIrrigationDto> getRecommandationsIrrigation() throws SQLException {
        List<RecommandationIrrigationDto> list = new ArrayList<>();
        Connection conn = getConn();
        if (conn == null) return list;
        String sql = "SELECT si.id_systeme, si.nom_systeme, si.id_parcelle, si.seuil_humidite, p.nom_parcelle, " +
                "  (SELECT hi.humidite_avant FROM historique_irrigation hi WHERE hi.id_systeme = si.id_systeme ORDER BY hi.date_irrigation DESC LIMIT 1) AS derniere_humidite " +
                "FROM systeme_irrigation si " +
                "JOIN parcelles p ON si.id_parcelle = p.id_parcelle " +
                "WHERE si.statut = 'ACTIF'";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                RecommandationIrrigationDto dto = new RecommandationIrrigationDto();
                dto.setIdSysteme(rs.getLong("id_systeme"));
                dto.setNomSysteme(rs.getString("nom_systeme"));
                dto.setIdParcelle(rs.getInt("id_parcelle"));
                dto.setNomParcelle(rs.getString("nom_parcelle"));
                double seuil = rs.getDouble("seuil_humidite");
                dto.setSeuilHumidite(seuil);
                String humStr = rs.getString("derniere_humidite");
                Double derniereHum = humStr != null ? rs.getDouble("derniere_humidite") : null;
                dto.setHumiditeDerniere(derniereHum);
                boolean sousSeuil = derniereHum != null && derniereHum < seuil;
                boolean alerteMeteo = hasAlerteMeteoSurParcelle(conn, rs.getInt("id_parcelle"));
                if (alerteMeteo && sousSeuil) {
                    dto.setPriorite("HAUTE");
                    dto.setMotif("Humidité sous seuil + alerte météo sur la parcelle");
                } else if (alerteMeteo) {
                    dto.setPriorite("MOYENNE");
                    dto.setMotif("Alerte météo sur la parcelle");
                } else if (sousSeuil) {
                    dto.setPriorite("HAUTE");
                    dto.setMotif("Humidité sous seuil");
                } else {
                    dto.setPriorite("OK");
                    dto.setMotif("Humidité suffisante");
                }
                list.add(dto);
            }
        }
        return list;
    }

    private boolean hasAlerteMeteoSurParcelle(Connection conn, int idParcelle) throws SQLException {
        String sql = "SELECT 1 FROM alertes_risques ar " +
                "JOIN cultures c ON ar.id_culture = c.id_culture AND c.id_parcelle = ? " +
                "WHERE ar.type_alerte = 'Météo' LIMIT 1";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, idParcelle);
            return pst.executeQuery().next();
        }
    }
}
