package Services.Dto;

import java.math.BigDecimal;

/**
 * Résultat métier : bilan hydrique parcelle (volume / surface → m³/ha).
 */
public class BilanHydriqueDto {
    private int idParcelle;
    private String nomParcelle;
    private double surface;
    private BigDecimal volumeTotalEau;
    private BigDecimal consommationParHectare;
    private String dateDebut;
    private String dateFin;
    private String unite;

    public int getIdParcelle() { return idParcelle; }
    public void setIdParcelle(int idParcelle) { this.idParcelle = idParcelle; }
    public String getNomParcelle() { return nomParcelle; }
    public void setNomParcelle(String nomParcelle) { this.nomParcelle = nomParcelle; }
    public double getSurface() { return surface; }
    public void setSurface(double surface) { this.surface = surface; }
    public BigDecimal getVolumeTotalEau() { return volumeTotalEau; }
    public void setVolumeTotalEau(BigDecimal volumeTotalEau) { this.volumeTotalEau = volumeTotalEau; }
    public BigDecimal getConsommationParHectare() { return consommationParHectare; }
    public void setConsommationParHectare(BigDecimal consommationParHectare) { this.consommationParHectare = consommationParHectare; }
    public String getDateDebut() { return dateDebut; }
    public void setDateDebut(String dateDebut) { this.dateDebut = dateDebut; }
    public String getDateFin() { return dateFin; }
    public void setDateFin(String dateFin) { this.dateFin = dateFin; }
    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }
}
