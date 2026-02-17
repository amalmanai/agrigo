package Entites;

import java.sql.Date;

public class Culture {
    private int id;
    private String nom;
    private Date dateSemis;
    private String etat;
    private double rendement;
    private int idParcelle;

    // Empty constructor for JavaFX PropertyValueFactory
    public Culture() {}

    // Constructor WITH ID (Crucial for database fetching)
    public Culture(int id, String nom, Date dateSemis, String etat, double rendement) {
        this.id = id;
        this.nom = nom;
        this.dateSemis = dateSemis;
        this.etat = etat;
        this.rendement = rendement;
    }

    // Constructor WITHOUT ID (For adding new records)
    public Culture(String nom, Date dateSemis, String etat, double rendement) {
        this.nom = nom;
        this.dateSemis = dateSemis;
        this.etat = etat;
        this.rendement = rendement;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Date getDateSemis() { return dateSemis; }
    public void setDateSemis(Date dateSemis) { this.dateSemis = dateSemis; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public double getRendement() { return rendement; }
    public void setRendement(double rendement) { this.rendement = rendement; }

    public int getIdParcelle() { return idParcelle; }
    public void setIdParcelle(int idParcelle) { this.idParcelle = idParcelle; }
}
