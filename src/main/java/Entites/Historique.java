package Entites;

import java.sql.Date;

public class Historique {
    private int id;
    private int idParcelle;
    private String ancienneCulture;
    private Date dateRecolte;
    private double rendementFinal;

    public Historique() {}

    // Constructor, Getters and Setters...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdParcelle() { return idParcelle; }
    public void setIdParcelle(int idParcelle) { this.idParcelle = idParcelle; }
    public String getAncienneCulture() { return ancienneCulture; }
    public void setAncienneCulture(String c) { this.ancienneCulture = c; }
    public Date getDateRecolte() { return dateRecolte; }
    public void setDateRecolte(Date d) { this.dateRecolte = d; }
    public double getRendementFinal() { return rendementFinal; }
    public void setRendementFinal(double r) { this.rendementFinal = r; }
}