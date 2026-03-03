package Services.Dto;

/**
 * Résultat API : une recommandation d'irrigation (système à irriguer ou OK).
 */
public class RecommandationIrrigationDto {
    private long idSysteme;
    private String nomSysteme;
    private int idParcelle;
    private String nomParcelle;
    private String priorite;
    private String motif;
    private Double humiditeDerniere;
    private Double seuilHumidite;

    public long getIdSysteme() { return idSysteme; }
    public void setIdSysteme(long idSysteme) { this.idSysteme = idSysteme; }
    public String getNomSysteme() { return nomSysteme; }
    public void setNomSysteme(String nomSysteme) { this.nomSysteme = nomSysteme; }
    public int getIdParcelle() { return idParcelle; }
    public void setIdParcelle(int idParcelle) { this.idParcelle = idParcelle; }
    public String getNomParcelle() { return nomParcelle; }
    public void setNomParcelle(String nomParcelle) { this.nomParcelle = nomParcelle; }
    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public Double getHumiditeDerniere() { return humiditeDerniere; }
    public void setHumiditeDerniere(Double humiditeDerniere) { this.humiditeDerniere = humiditeDerniere; }
    public Double getSeuilHumidite() { return seuilHumidite; }
    public void setSeuilHumidite(Double seuilHumidite) { this.seuilHumidite = seuilHumidite; }
}
