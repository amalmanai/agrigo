package Services;

import Entites.User;
import Utils.MyBD;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ServiceUser {

    Connection cnx = MyBD.getInstance().getConn();


    // ================== AJOUT ==================
    public void ajouter(User user) {
        String req = "INSERT INTO `user`(`nom_user`, `prenom_user`, `email_user`, `role_user`, `num_user`, `password`, `adresse_user`) VALUES (?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, user.getNom_user());
            ps.setString(2, user.getPrenom_user());

            if (!isValidEmail(user.getEmail_user())) {
                throw new IllegalArgumentException("Invalid email address");
            }
            ps.setString(3, user.getEmail_user());
            ps.setString(4, user.getRole_user());
            ps.setInt(5, user.getNum_user());
            ps.setString(6, user.getPassword());
            ps.setString(7, user.getAdresse_user());

            ps.executeUpdate();
            System.out.println("User added!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================== AUTH ==================
    public User authenticate(String email, String password) {
        String query = "SELECT * FROM `user` WHERE `email_user`=? AND `password`=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id_user"),
                        rs.getString("nom_user"),
                        rs.getString("prenom_user"),
                        rs.getString("email_user"),
                        rs.getString("role_user"),
                        rs.getInt("num_user"),
                        rs.getString("password"),
                        rs.getString("adresse_user")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // ================== UPDATE ==================
    public void modifier(User user) {
        String req = "UPDATE `user` SET `nom_user`=?, `prenom_user`=?, `email_user`=?, `role_user`=?, `num_user`=?, `password`=?, `adresse_user`=? WHERE `id_user`=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, user.getNom_user());
            ps.setString(2, user.getPrenom_user());

            if (!isValidEmail(user.getEmail_user())) {
                throw new IllegalArgumentException("Invalid email address");
            }
            ps.setString(3, user.getEmail_user());
            ps.setString(4, user.getRole_user());
            ps.setInt(5, user.getNum_user());
            ps.setString(6, user.getPassword());
            ps.setString(7, user.getAdresse_user());
            ps.setInt(8, user.getId_user());

            ps.executeUpdate();
            System.out.println("User updated!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================== DELETE ==================
    public void supprimer(int id) {
        String req = "DELETE FROM `user` WHERE `id_user`=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("User deleted!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================== GET ALL ==================
    public Set<User> getAll() {
        Set<User> users = new HashSet<>();
        String req = "SELECT * FROM `user`";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                User u = new User(
                        rs.getInt("id_user"),
                        rs.getString("nom_user"),
                        rs.getString("prenom_user"),
                        rs.getString("email_user"),
                        rs.getString("role_user"),
                        rs.getInt("num_user"),
                        rs.getString("password"),
                        rs.getString("adresse_user")
                );
                users.add(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    // ================== GET BY ID ==================
    public User getOneByID(int id) {
        String req = "SELECT * FROM `user` WHERE `id_user`=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id_user"),
                        rs.getString("nom_user"),
                        rs.getString("prenom_user"),
                        rs.getString("email_user"),
                        rs.getString("role_user"),
                        rs.getInt("num_user"),
                        rs.getString("password"),
                        rs.getString("adresse_user")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // ================== GET BY EMAIL ==================
    public User getOneByEmail(String email) {
        String req = "SELECT * FROM `user` WHERE `email_user`=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id_user"),
                        rs.getString("nom_user"),
                        rs.getString("prenom_user"),
                        rs.getString("email_user"),
                        rs.getString("role_user"),
                        rs.getInt("num_user"),
                        rs.getString("password"),
                        rs.getString("adresse_user")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // ================== EMAIL VALIDATION ==================
    private boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.compile(regex).matcher(email).matches();
    }
}
