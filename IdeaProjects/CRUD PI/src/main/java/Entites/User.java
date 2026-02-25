package Entites;

import java.util.Objects;

public class User {

    private int id_user;
    private String nom_user;
    private String prenom_user;
    private String email_user;
    private String role_user;
    private int num_user;
    private String password;
    private String adresse_user;

    // Constructeur vide
    public User() {}

    // Constructeur avec id
    public User(int id_user) {
        this.id_user = id_user;
    }

    // Constructeur INSERT
    public User(String nom_user, String prenom_user, String email_user,
                String role_user, int num_user, String password, String adresse_user) {
        this.nom_user = nom_user;
        this.prenom_user = prenom_user;
        this.email_user = email_user;
        this.role_user = role_user;
        this.num_user = num_user;
        this.password = password;
        this.adresse_user = adresse_user;
    }

    // Constructeur SELECT
    public User(int id_user, String nom_user, String prenom_user, String email_user,
                String role_user, int num_user, String password, String adresse_user) {
        this.id_user = id_user;
        this.nom_user = nom_user;
        this.prenom_user = prenom_user;
        this.email_user = email_user;
        this.role_user = role_user;
        this.num_user = num_user;
        this.password = password;
        this.adresse_user = adresse_user;
    }

    public int getId_user() { return id_user; }
    public String getNom_user() { return nom_user; }
    public String getPrenom_user() { return prenom_user; }
    public String getEmail_user() { return email_user; }
    public String getRole_user() { return role_user; }
    public int getNum_user() { return num_user; }
    public String getPassword() { return password; }
    public String getAdresse_user() { return adresse_user; }

    public void setId_user(int id_user) { this.id_user = id_user; }
    public void setNom_user(String nom_user) { this.nom_user = nom_user; }
    public void setPrenom_user(String prenom_user) { this.prenom_user = prenom_user; }
    public void setEmail_user(String email_user) { this.email_user = email_user; }
    public void setRole_user(String role_user) { this.role_user = role_user; }
    public void setNum_user(int num_user) { this.num_user = num_user; }
    public void setPassword(String password) { this.password = password; }
    public void setAdresse_user(String adresse_user) { this.adresse_user = adresse_user; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id_user == user.id_user;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_user);
    }
}
