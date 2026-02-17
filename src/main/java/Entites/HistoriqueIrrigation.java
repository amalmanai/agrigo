package Entites;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class HistoriqueIrrigation {

    private long id;
    private long idSysteme;
    private Timestamp dateIrrigation;
    private int dureeMinutes;
    private BigDecimal volumeEau;
    private BigDecimal humiditeAvant;
    private String typeDeclenchement; // 'AUTO' ou 'MANUEL'

    public HistoriqueIrrigation() {
    }

    public HistoriqueIrrigation(long idSysteme,
                                int dureeMinutes,
                                BigDecimal volumeEau,
                                BigDecimal humiditeAvant,
                                String typeDeclenchement) {
        this.idSysteme = idSysteme;
        this.dureeMinutes = dureeMinutes;
        this.volumeEau = volumeEau;
        this.humiditeAvant = humiditeAvant;
        this.typeDeclenchement = typeDeclenchement;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdSysteme() {
        return idSysteme;
    }

    public void setIdSysteme(long idSysteme) {
        this.idSysteme = idSysteme;
    }

    public Timestamp getDateIrrigation() {
        return dateIrrigation;
    }

    public void setDateIrrigation(Timestamp dateIrrigation) {
        this.dateIrrigation = dateIrrigation;
    }

    public int getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(int dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }

    public BigDecimal getVolumeEau() {
        return volumeEau;
    }

    public void setVolumeEau(BigDecimal volumeEau) {
        this.volumeEau = volumeEau;
    }

    public BigDecimal getHumiditeAvant() {
        return humiditeAvant;
    }

    public void setHumiditeAvant(BigDecimal humiditeAvant) {
        this.humiditeAvant = humiditeAvant;
    }

    public String getTypeDeclenchement() {
        return typeDeclenchement;
    }

    public void setTypeDeclenchement(String typeDeclenchement) {
        this.typeDeclenchement = typeDeclenchement;
    }

    @Override
    public String toString() {
        return "HistoriqueIrrigation{" +
                "id=" + id +
                ", idSysteme=" + idSysteme +
                ", dateIrrigation=" + dateIrrigation +
                ", dureeMinutes=" + dureeMinutes +
                ", volumeEau=" + volumeEau +
                ", humiditeAvant=" + humiditeAvant +
                ", typeDeclenchement='" + typeDeclenchement + '\'' +
                '}';
    }
}

