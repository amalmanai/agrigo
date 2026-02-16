package org.example.Entites;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Mouvement {
    private int idMouvement;
    private String typeMouvement; // ENTREE ou SORTIE
    private String dateMouvement;
    private int quantite;
    private String motif;
    private int idProduit;
    private int idUser; // Static pour le moment
    private String nomProduit; // Pour l'affichage jointure

    // Constructeurs
    public Mouvement() {}

    public Mouvement(int idMouvement, String typeMouvement, String dateMouvement, int quantite,
                     String motif, int idProduit, int idUser) {
        this.idMouvement = idMouvement;
        this.typeMouvement = typeMouvement;
        this.dateMouvement = dateMouvement;
        this.quantite = quantite;
        this.motif = motif;
        this.idProduit = idProduit;
        this.idUser = idUser;
    }

    // Getters et Setters
    public int getIdMouvement() {
        return idMouvement;
    }

    public void setIdMouvement(int idMouvement) {
        this.idMouvement = idMouvement;
    }

    public String getTypeMouvement() {
        return typeMouvement;
    }

    public void setTypeMouvement(String typeMouvement) {
        this.typeMouvement = typeMouvement;
    }

    public String getDateMouvement() {
        return dateMouvement;
    }

    public void setDateMouvement(String dateMouvement) {
        this.dateMouvement = dateMouvement;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public int getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    // Méthode utilitaire pour formater la date
    public String getDateFormatee() {
        try {
            LocalDateTime date = LocalDateTime.parse(dateMouvement, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return dateMouvement;
        }
    }

    @Override
    public String toString() {
        return "Mouvement{" +
                "idMouvement=" + idMouvement +
                ", typeMouvement='" + typeMouvement + '\'' +
                ", quantite=" + quantite +
                ", motif='" + motif + '\'' +
                '}';
    }
}