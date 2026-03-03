package Services.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Une ligne d'irrigation pour affichage (avec nom système et parcelle).
 */
public class LigneIrrigationDto {
    private long id;
    private LocalDateTime dateIrrigation;
    private String nomSysteme;
    private String nomParcelle;
    private int dureeMinutes;
    private BigDecimal volumeEau;
    private BigDecimal humiditeAvant;
    private String typeDeclenchement;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public LocalDateTime getDateIrrigation() { return dateIrrigation; }
    public void setDateIrrigation(LocalDateTime dateIrrigation) { this.dateIrrigation = dateIrrigation; }
    public String getNomSysteme() { return nomSysteme; }
    public void setNomSysteme(String nomSysteme) { this.nomSysteme = nomSysteme; }
    public String getNomParcelle() { return nomParcelle; }
    public void setNomParcelle(String nomParcelle) { this.nomParcelle = nomParcelle; }
    public int getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(int dureeMinutes) { this.dureeMinutes = dureeMinutes; }
    public BigDecimal getVolumeEau() { return volumeEau; }
    public void setVolumeEau(BigDecimal volumeEau) { this.volumeEau = volumeEau; }
    public BigDecimal getHumiditeAvant() { return humiditeAvant; }
    public void setHumiditeAvant(BigDecimal humiditeAvant) { this.humiditeAvant = humiditeAvant; }
    public String getTypeDeclenchement() { return typeDeclenchement; }
    public void setTypeDeclenchement(String typeDeclenchement) { this.typeDeclenchement = typeDeclenchement; }
}
