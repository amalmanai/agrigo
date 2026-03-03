package Entites;

import Entites.User;

import java.sql.Date;
import java.sql.Time;
import java.util.Objects;

public class Tache {
    private int id;
    private String titre_tache;
    private String description_tache;
    private String type_tache;
    private User user; // relation avec table user
    private Date date_tache;
    private Time heure_debut_tache;
    private Time heure_fin_tache;
    private String status_tache;
    private String remarque_tache;

    // ======= Constructeurs =======
    public Tache() {}

    // Constructeur INSERT
    public Tache(String titre_tache, String description_tache, String type_tache,
                 User user, Date date_tache, Time heure_debut_tache,
                 Time heure_fin_tache, String status_tache, String remarque_tache) {
        this.titre_tache = titre_tache;
        this.description_tache = description_tache;
        this.type_tache = type_tache;
        this.user = user;
        this.date_tache = date_tache;
        this.heure_debut_tache = heure_debut_tache;
        this.heure_fin_tache = heure_fin_tache;
        this.status_tache = status_tache;
        this.remarque_tache = remarque_tache;
    }

    // Constructeur SELECT
    public Tache(int id, String titre_tache, String description_tache, String type_tache,
                 User user, Date date_tache, Time heure_debut_tache,
                 Time heure_fin_tache, String status_tache, String remarque_tache) {
        this.id = id;
        this.titre_tache = titre_tache;
        this.description_tache = description_tache;
        this.type_tache = type_tache;
        this.user = user;
        this.date_tache = date_tache;
        this.heure_debut_tache = heure_debut_tache;
        this.heure_fin_tache = heure_fin_tache;
        this.status_tache = status_tache;
        this.remarque_tache = remarque_tache;
    }

    // ======= Getters & Setters =======
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre_tache() { return titre_tache; }
    public void setTitre_tache(String titre_tache) { this.titre_tache = titre_tache; }

    public String getDescription_tache() { return description_tache; }
    public void setDescription_tache(String description_tache) { this.description_tache = description_tache; }

    public String getType_tache() { return type_tache; }
    public void setType_tache(String type_tache) { this.type_tache = type_tache; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Date getDate_tache() { return date_tache; }
    public void setDate_tache(Date date_tache) { this.date_tache = date_tache; }

    public Time getHeure_debut_tache() { return heure_debut_tache; }
    public void setHeure_debut_tache(Time heure_debut_tache) { this.heure_debut_tache = heure_debut_tache; }

    public Time getHeure_fin_tache() { return heure_fin_tache; }
    public void setHeure_fin_tache(Time heure_fin_tache) { this.heure_fin_tache = heure_fin_tache; }

    public String getStatus_tache() { return status_tache; }
    public void setStatus_tache(String status_tache) { this.status_tache = status_tache; }

    public String getRemarque_tache() { return remarque_tache; }
    public void setRemarque_tache(String remarque_tache) { this.remarque_tache = remarque_tache; }

    // ======= equals & hashCode =======
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tache)) return false;
        Tache tache = (Tache) o;
        return id == tache.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ======= toString =======
    @Override
    public String toString() {
        return "Tache{" +
                "id=" + id +
                ", titre_tache='" + titre_tache + '\'' +
                ", description_tache='" + description_tache + '\'' +
                ", type_tache='" + type_tache + '\'' +
                ", user=" + user +
                ", date_tache=" + date_tache +
                ", heure_debut_tache=" + heure_debut_tache +
                ", heure_fin_tache=" + heure_fin_tache +
                ", status_tache='" + status_tache + '\'' +
                ", remarque_tache='" + remarque_tache + '\'' +
                '}';
    }
}
