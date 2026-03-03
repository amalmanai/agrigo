package Services;

import Services.Dto.BilanHydriqueDto;
import Services.Dto.LigneIrrigationDto;
import Utils.MyBD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Métier avancé : irrigations après une date + bilan hydrique parcelle.
 */
public class IrrigationMetierAvanceService {

    private Connection getConn() {
        return MyBD.getInstance().getConn();
    }

    public List<LigneIrrigationDto> getIrrigationsApresDate(LocalDate date) throws SQLException {
        List<LigneIrrigationDto> list = new ArrayList<>();
        Connection conn = getConn();
        if (conn == null) return list;
        String sql = "SELECT hi.id, hi.date_irrigation, hi.duree_minutes, hi.volume_eau, hi.humidite_avant, hi.type_declenchement, " +
                "  si.nom_systeme, p.nom_parcelle " +
                "FROM historique_irrigation hi " +
                "JOIN systeme_irrigation si ON hi.id_systeme = si.id_systeme " +
                "JOIN parcelles p ON si.id_parcelle = p.id_parcelle " +
                "WHERE DATE(hi.date_irrigation) >= ? " +
                "ORDER BY hi.date_irrigation DESC";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setDate(1, Date.valueOf(date));
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                LigneIrrigationDto dto = new LigneIrrigationDto();
                dto.setId(rs.getLong("id"));
                Timestamp ts = rs.getTimestamp("date_irrigation");
                dto.setDateIrrigation(ts != null ? ts.toLocalDateTime() : null);
                dto.setNomSysteme(rs.getString("nom_systeme"));
                dto.setNomParcelle(rs.getString("nom_parcelle"));
                dto.setDureeMinutes(rs.getInt("duree_minutes"));
                dto.setVolumeEau(rs.getBigDecimal("volume_eau"));
                dto.setHumiditeAvant(rs.getBigDecimal("humidite_avant"));
                dto.setTypeDeclenchement(rs.getString("type_declenchement"));
                list.add(dto);
            }
        }
        return list;
    }

    public BilanHydriqueDto getBilanHydriqueParcelle(int idParcelle, LocalDate dateDebut, LocalDate dateFin) throws SQLException {
        BilanHydriqueDto dto = new BilanHydriqueDto();
        dto.setIdParcelle(idParcelle);
        dto.setDateDebut(dateDebut != null ? dateDebut.toString() : null);
        dto.setDateFin(dateFin != null ? dateFin.toString() : null);
        dto.setUnite("m3_ha");
        Connection conn = getConn();
        if (conn == null) return dto;
        String sql = "SELECT p.nom_parcelle, p.surface, " +
                "  COALESCE(SUM(hi.volume_eau), 0) AS volume_total " +
                "FROM parcelles p " +
                "LEFT JOIN systeme_irrigation si ON si.id_parcelle = p.id_parcelle " +
                "LEFT JOIN historique_irrigation hi ON hi.id_systeme = si.id_systeme " +
                "  AND (? IS NULL OR DATE(hi.date_irrigation) >= ?) " +
                "  AND (? IS NULL OR DATE(hi.date_irrigation) <= ?) " +
                "WHERE p.id_parcelle = ? " +
                "GROUP BY p.id_parcelle, p.nom_parcelle, p.surface";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            Date dD = dateDebut != null ? Date.valueOf(dateDebut) : null;
            Date dF = dateFin != null ? Date.valueOf(dateFin) : null;
            pst.setDate(1, dD);
            pst.setDate(2, dD);
            pst.setDate(3, dF);
            pst.setDate(4, dF);
            pst.setInt(5, idParcelle);
            ResultSet rs = pst.executeQuery();
            if (!rs.next()) return dto;
            dto.setNomParcelle(rs.getString("nom_parcelle"));
            double surface = rs.getDouble("surface");
            dto.setSurface(surface);
            BigDecimal volume = rs.getBigDecimal("volume_total");
            dto.setVolumeTotalEau(volume);
            if (surface > 0 && volume != null) {
                double vol = volume.doubleValue();
                double consoHa = vol / surface;
                dto.setConsommationParHectare(BigDecimal.valueOf(consoHa).setScale(2, RoundingMode.HALF_UP));
            } else {
                dto.setConsommationParHectare(BigDecimal.ZERO);
            }
        }
        return dto;
    }
}
