package entity;

import java.sql.Date;

public class recolte {
    private int id_recolte;
    private String nom_produit;
    private double quantite;
    private String unite;
    private Date date_recolte;
    private double cout_production;
    private int id_user;

    // Constructeur pour l'affichage (avec ID)
    public recolte(int id_recolte, String nom_produit, double quantite, String unite, Date date_recolte, double cout_production, int id_user) {
        this.id_recolte = id_recolte;
        this.nom_produit = nom_produit;
        this.quantite = quantite;
        this.unite = unite;
        this.date_recolte = date_recolte;
        this.cout_production = cout_production;
        this.id_user = id_user;
    }

    // Getters indispensables pour le TableView
    public int getId_recolte() { return id_recolte; }
    public String getNom_produit() { return nom_produit; }
    public double getQuantite() { return quantite; }
    public String getUnite() { return unite; }
    public Date getDate_recolte() { return date_recolte; }
    public double getCout_production() { return cout_production; }
}