package Entites;

import java.sql.Timestamp;

public class Alerte {
    private int id;
    private String type;
    private String description;
    private Timestamp date;
    private int idCulture;

    public Alerte() {}

    public Alerte(String type, String description, int idCulture) {
        this.type = type;
        this.description = description;
        this.idCulture = idCulture;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }
    public int getIdCulture() { return idCulture; }
    public void setIdCulture(int idCulture) { this.idCulture = idCulture; }
}