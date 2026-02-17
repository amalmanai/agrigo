package Entites;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class SystemeIrrigation {

    private long idSysteme;
    private long idParcelle;
    private String nomSysteme;
    private BigDecimal seuilHumidite;
    private String mode;   // 'AUTO' ou 'MANUEL'
    private String statut; // 'ACTIF' ou 'INACTIF'
    private Timestamp dateCreation;

    public SystemeIrrigation() {
    }

    public SystemeIrrigation(long idParcelle,
                             String nomSysteme,
                             BigDecimal seuilHumidite,
                             String mode,
                             String statut) {
        this.idParcelle = idParcelle;
        this.nomSysteme = nomSysteme;
        this.seuilHumidite = seuilHumidite;
        this.mode = mode;
        this.statut = statut;
    }

    public long getIdSysteme() {
        return idSysteme;
    }

    public void setIdSysteme(long idSysteme) {
        this.idSysteme = idSysteme;
    }

    public long getIdParcelle() {
        return idParcelle;
    }

    public void setIdParcelle(long idParcelle) {
        this.idParcelle = idParcelle;
    }

    public String getNomSysteme() {
        return nomSysteme;
    }

    public void setNomSysteme(String nomSysteme) {
        this.nomSysteme = nomSysteme;
    }

    public BigDecimal getSeuilHumidite() {
        return seuilHumidite;
    }

    public void setSeuilHumidite(BigDecimal seuilHumidite) {
        this.seuilHumidite = seuilHumidite;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return "SystemeIrrigation{" +
                "idSysteme=" + idSysteme +
                ", idParcelle=" + idParcelle +
                ", nomSysteme='" + nomSysteme + '\'' +
                ", seuilHumidite=" + seuilHumidite +
                ", mode='" + mode + '\'' +
                ", statut='" + statut + '\'' +
                ", dateCreation=" + dateCreation +
                '}';
    }
}

