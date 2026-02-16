package org.example.Services;

import org.example.Entites.User;
import org.example.Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserCRUD {

    private Connection conn;

    public UserCRUD() {
        conn = MyBD.getInstance().getConnection();
    }

    // ================= CREATE =================
    public void createUser(User user) throws SQLException {
        String req = "INSERT INTO user " +
                "(nom_user, prenom_user, email_user, role_user, num_user, password, adresse_user) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getNomUser());
            ps.setString(2, user.getPrenomUser());
            ps.setString(3, user.getEmailUser());
            ps.setString(4, user.getRoleUser());
            ps.setInt(5, user.getNumUser());
            ps.setString(6, hashPassword(user.getPassword()));
            ps.setString(7, user.getAdresseUser());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                user.setIdUser(rs.getInt(1));
            }
        }

        System.out.println("Utilisateur ajouté avec ID = " + user.getIdUser());
    }

    // ================= UPDATE =================
    public void updateUser(User user) throws SQLException {
        String req = "UPDATE user SET " +
                "nom_user=?, prenom_user=?, email_user=?, role_user=?, num_user=?, adresse_user=? " +
                "WHERE id_user=?";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, user.getNomUser());
            ps.setString(2, user.getPrenomUser());
            ps.setString(3, user.getEmailUser());
            ps.setString(4, user.getRoleUser());
            ps.setInt(5, user.getNumUser());
            ps.setString(6, user.getAdresseUser());
            ps.setInt(7, user.getIdUser());

            ps.executeUpdate();
        }

        System.out.println("Utilisateur modifié !");
    }

    // ================= DELETE =================
    public void deleteUser(int id) throws SQLException {
        String req = "DELETE FROM user WHERE id_user=?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        System.out.println("Utilisateur supprimé !");
    }

    // ================= SHOW ALL =================
    public List<User> showUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String req = "SELECT * FROM user";

        try (PreparedStatement ps = conn.prepareStatement(req);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }

        return users;
    }

    // ================= GET BY ID =================
    public User getById(int id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id_user=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setIdUser(rs.getInt("id_user"));
                u.setNomUser(rs.getString("nom_user"));
                u.setPrenomUser(rs.getString("prenom_user"));
                u.setEmailUser(rs.getString("email_user"));
                u.setRoleUser(rs.getString("role_user"));
                u.setNumUser(rs.getInt("num_user"));
                u.setPassword(rs.getString("password"));
                u.setAdresseUser(rs.getString("adresse_user"));
                return u;
            }
        }
        return null;
    }

    // ================= MAP RESULT =================
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setIdUser(rs.getInt("id_user"));
        u.setNomUser(rs.getString("nom_user"));
        u.setPrenomUser(rs.getString("prenom_user"));
        u.setEmailUser(rs.getString("email_user"));
        u.setRoleUser(rs.getString("role_user"));
        u.setNumUser(rs.getInt("num_user"));
        u.setPassword(rs.getString("password"));
        u.setAdresseUser(rs.getString("adresse_user"));
        return u;
    }

    // ================= HASH PASSWORD =================
    private static String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
