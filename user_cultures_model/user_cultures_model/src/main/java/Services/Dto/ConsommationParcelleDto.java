package Services.Dto;

import java.math.BigDecimal;

/**
 * Résultat API : consommation d'eau pour une parcelle sur une période.
 */
public class ConsommationParcelleDto {
    private int idParcelle;
    private String nomParcelle;
    private double surface;
    private BigDecimal volumeTotalEau;
    private int nombreIrrigations;
    private String dateDebut;
    private String dateFin;

    public int getIdParcelle() { return idParcelle; }
    public void setIdParcelle(int idParcelle) { this.idParcelle = idParcelle; }
    public String getNomParcelle() { return nomParcelle; }
    public void setNomParcelle(String nomParcelle) { this.nomParcelle = nomParcelle; }
    public double getSurface() { return surface; }
    public void setSurface(double surface) { this.surface = surface; }
    public BigDecimal getVolumeTotalEau() { return volumeTotalEau; }
    public void setVolumeTotalEau(BigDecimal volumeTotalEau) { this.volumeTotalEau = volumeTotalEau; }
    public int getNombreIrrigations() { return nombreIrrigations; }
    public void setNombreIrrigations(int nombreIrrigations) { this.nombreIrrigations = nombreIrrigations; }
    public String getDateDebut() { return dateDebut; }
    public void setDateDebut(String dateDebut) { this.dateDebut = dateDebut; }
    public String getDateFin() { return dateFin; }
    public void setDateFin(String dateFin) { this.dateFin = dateFin; }
}
