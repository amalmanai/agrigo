package Entites;

public class Parcelle {
    private int id;
    private String nom;
    private double surface;
    private String gps;
    private String typeSol;

    // Default constructor - Required for JavaFX and some frameworks
    public Parcelle() {}

    // Constructor WITH ID - Used when loading data from the database
    public Parcelle(int id, String nom, double surface, String gps, String typeSol) {
        this.id = id;
        this.nom = nom;
        this.surface = surface;
        this.gps = gps;
        this.typeSol = typeSol;
    }

    // Constructor WITHOUT ID - Used when creating a new parcelle to save to DB
    public Parcelle(String nom, double surface, String gps, String typeSol) {
        this.nom = nom;
        this.surface = surface;
        this.gps = gps;
        this.typeSol = typeSol;
    }

    // Getters and Setters - Names must match PropertyValueFactory in controllers
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public double getSurface() { return surface; }
    public void setSurface(double surface) { this.surface = surface; }

    public String getGps() { return gps; }
    public void setGps(String gps) { this.gps = gps; }

    public String getTypeSol() { return typeSol; }
    public void setTypeSol(String typeSol) { this.typeSol = typeSol; }

    @Override
    public String toString() {
        if (nom == null) {
            return String.valueOf(id);
        }
        return id + " - " + nom;
    }
}
