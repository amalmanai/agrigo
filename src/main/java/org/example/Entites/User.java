package org.example.Entites;

import java.util.Objects;

public class User {
    private int idUser;
    private String nomUser;
    private String prenomUser;
    private String emailUser;
    private String roleUser;
    private int numUser;
    private String password;
    private String adresseUser;

    public User() {}

    public User(int idUser, String nomUser, String prenomUser, String emailUser, String roleUser,
                int numUser, String password, String adresseUser) {
        this.idUser = idUser;
        this.nomUser = nomUser;
        this.prenomUser = prenomUser;
        this.emailUser = emailUser;
        this.roleUser = roleUser;
        this.numUser = numUser;
        this.password = password;
        this.adresseUser = adresseUser;
    }

    public User(String nomUser, String prenomUser, String emailUser, String roleUser,
                int numUser, String password, String adresseUser) {
        this.nomUser = nomUser;
        this.prenomUser = prenomUser;
        this.emailUser = emailUser;
        this.roleUser = roleUser;
        this.numUser = numUser;
        this.password = password;
        this.adresseUser = adresseUser;
    }

    // Getters & Setters
    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public String getNomUser() { return nomUser; }
    public void setNomUser(String nomUser) { this.nomUser = nomUser; }

    public String getPrenomUser() { return prenomUser; }
    public void setPrenomUser(String prenomUser) { this.prenomUser = prenomUser; }

    public String getEmailUser() { return emailUser; }
    public void setEmailUser(String emailUser) { this.emailUser = emailUser; }

    public String getRoleUser() { return roleUser; }
    public void setRoleUser(String roleUser) { this.roleUser = roleUser; }

    public int getNumUser() { return numUser; }
    public void setNumUser(int numUser) { this.numUser = numUser; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAdresseUser() { return adresseUser; }
    public void setAdresseUser(String adresseUser) { this.adresseUser = adresseUser; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return idUser == user.idUser;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUser);
    }

    @Override
    public String toString() {
        return "User{" +
                "idUser=" + idUser +
                ", nomUser='" + nomUser + '\'' +
                ", prenomUser='" + prenomUser + '\'' +
                ", emailUser='" + emailUser + '\'' +
                ", roleUser='" + roleUser + '\'' +
                ", numUser=" + numUser +
                ", adresseUser='" + adresseUser + '\'' +
                '}';
    }
}
