package org.example.Entites;

import java.util.Objects;

public class Produit {
    private int idProduit;
    private String nomProduit;
    private String categorie;
    private int quantiteDisponible;
    private String unite;
    private int seuilAlerte;
    private String dateExpiration;
    private double prixUnitaire;

    public Produit() {}

    public Produit(int idProduit, String nomProduit, int quantiteDisponible, String categorie,
                   String unite, int seuilAlerte, String dateExpiration, double prixUnitaire) {
        this.idProduit = idProduit;
        this.nomProduit = nomProduit;
        this.quantiteDisponible = quantiteDisponible;
        this.categorie = categorie;
        this.unite = unite;
        this.seuilAlerte = seuilAlerte;
        this.dateExpiration = dateExpiration;
        this.prixUnitaire = prixUnitaire;
    }

    public Produit(String nomProduit, String categorie, int quantiteDisponible, String unite,
                   int seuilAlerte, String dateExpiration, double prixUnitaire) {
        this.nomProduit = nomProduit;
        this.categorie = categorie;
        this.quantiteDisponible = quantiteDisponible;
        this.unite = unite;
        this.seuilAlerte = seuilAlerte;
        this.dateExpiration = dateExpiration;
        this.prixUnitaire = prixUnitaire;
    }

    // Getters
    public int getIdProduit() { return idProduit; }
    public String getNomProduit() { return nomProduit; }
    public String getCategorie() { return categorie; }
    public int getQuantiteDisponible() { return quantiteDisponible; }
    public String getUnite() { return unite; }
    public int getSeuilAlerte() { return seuilAlerte; }
    public String getDateExpiration() { return dateExpiration; }
    public double getPrixUnitaire() { return prixUnitaire; }

    // Setters
    public void setIdProduit(int idProduit) { this.idProduit = idProduit; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public void setQuantiteDisponible(int quantiteDisponible) { this.quantiteDisponible = quantiteDisponible; }
    public void setUnite(String unite) { this.unite = unite; }
    public void setSeuilAlerte(int seuilAlerte) { this.seuilAlerte = seuilAlerte; }
    public void setDateExpiration(String dateExpiration) { this.dateExpiration = dateExpiration; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Produit)) return false;
        Produit produit = (Produit) o;
        return idProduit == produit.idProduit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProduit);
    }

    @Override
    public String toString() {
        return "Produit{" +
                "idProduit=" + idProduit +
                ", nomProduit='" + nomProduit + '\'' +
                ", categorie='" + categorie + '\'' +
                ", quantiteDisponible=" + quantiteDisponible +
                ", unite='" + unite + '\'' +
                ", seuilAlerte=" + seuilAlerte +
                ", dateExpiration='" + dateExpiration + '\'' +
                ", prixUnitaire=" + prixUnitaire +
                '}';
    }
}