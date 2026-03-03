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
        String req = "INSERT INTO `user`(`nom_user`, `prenom_user`, `email_user`, `role_user`, `num_user`, `password`, `adresse_user`, `photo_path`) VALUES (?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, user.getNom_user());
            ps.setString(2, user.getPrenom_user());

            if (!isValidEmail(user.getEmail_user())) {
                throw new IllegalArgumentException("Adresse email invalide !");
            }
            ps.setString(3, user.getEmail_user());
            ps.setString(4, user.getRole_user());
            ps.setInt(5, user.getNum_user());
            ps.setString(6, user.getPassword());
            ps.setString(7, user.getAdresse_user());
            ps.setString(8, user.getPhotoPath());

            ps.executeUpdate();
            System.out.println("User ajouté !");
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
                User u = mapResultSetToUser(rs);
                return u;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Authentifie un utilisateur par une image (chemin local vers une photo).
     * Le modèle LBPH doit être préalablement entraîné (utiliser
     * FaceRecognitionService.trainFromDisk).
     * Le label retourné par le modèle correspond à l'id_user stocké dans les
     * dossiers d'entraînement.
     * Une confidence plus petite signifie une meilleure correspondance (LBPH). On
     * considère que
     * la prédiction est valide si confidence <= maxConfidence.
     *
     * @param imagePath     chemin vers l'image à tester
     * @param maxConfidence seuil de confiance (valeur maximale acceptée, ex: 60.0)
     * @return User reconnu si confiance suffisante, sinon null
     */
    public User authenticateByPhoto(String imagePath, double maxConfidence) {
        try {
            FaceRecognitionService fr = new FaceRecognitionService();
            FaceRecognitionService.PredictionResult res = fr.predictFromImagePath(imagePath);
            if (res == null) {
                System.out.println("Aucune prédiction possible (pas de visage ou erreur)");
                return null;
            }
            System.out.println("Prediction: label=" + res.label + ", confidence=" + res.confidence);
            if (res.confidence <= maxConfidence) {
                // label correspond à id_user si vous avez entraîné avec des dossiers nommés par
                // id
                return getOneByID(res.label);
            } else {
                System.out.println("Confiance trop faible : " + res.confidence + " > " + maxConfidence);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'authentification par photo: " + e.getMessage());
            return null;
        }
    }

    /** Surcharge avec un seuil par défaut de 60.0 */
    public User authenticateByPhoto(String imagePath) {
        return authenticateByPhoto(imagePath, 60.0);
    }

    // ================== UPDATE ==================
    public void modifier(User user) {
        String req = "UPDATE `user` SET `nom_user`=?, `prenom_user`=?, `email_user`=?, `role_user`=?, `num_user`=?, `password`=?, `adresse_user`=?, `photo_path`=? WHERE `id_user`=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, user.getNom_user());
            ps.setString(2, user.getPrenom_user());

            if (!isValidEmail(user.getEmail_user())) {
                throw new IllegalArgumentException("Adresse email invalide !");
            }
            ps.setString(3, user.getEmail_user());
            ps.setString(4, user.getRole_user());
            ps.setInt(5, user.getNum_user());
            ps.setString(6, user.getPassword());
            ps.setString(7, user.getAdresse_user());

            // Handle potentially null photoPath safely for the database
            if (user.getPhotoPath() != null) {
                ps.setString(8, user.getPhotoPath());
            } else {
                ps.setNull(8, Types.VARCHAR);
            }

            ps.setInt(9, user.getId_user());

            ps.executeUpdate();
            System.out.println("User mis à jour !");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================== RESET PASSWORD ==================
    public boolean updatePasswordByEmail(String email, String newPassword) {
        User user = getOneByEmail(email);
        if (user == null)
            return false;
        user.setPassword(newPassword);
        modifier(user);
        return true;
    }

    public boolean updatePasswordByPhone(String phone, String newPassword) {
        User user = getOneByPhone(phone);
        if (user == null)
            return false;
        user.setPassword(newPassword);
        modifier(user);
        return true;
    }

    // ================== DELETE ==================
    public void supprimer(int id) {
        String req = "DELETE FROM `user` WHERE `id_user`=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("User supprimé !");
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
                users.add(mapResultSetToUser(rs));
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
            if (rs.next())
                return mapResultSetToUser(rs);
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
            if (rs.next())
                return mapResultSetToUser(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // ================== GET BY PHONE ==================
    public User getOneByPhone(String phone) {
        if (phone == null)
            return null;
        String digits = phone.replaceAll("\\D", "");
        if (digits.isEmpty())
            return null;
        try {
            int num = Integer.parseInt(digits);
            String req = "SELECT * FROM `user` WHERE `num_user`=?";
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, num);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToUser(rs);
        } catch (SQLException | NumberFormatException e) {
            return null;
        }
        return null;
    }

    // ================== GET BY ROLE ==================
    public Set<User> getByRole(String role) {
        Set<User> users = new HashSet<>();
        String req = "SELECT * FROM `user` WHERE `role_user`=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    // ================== EMAIL VALIDATION ==================
    private boolean isValidEmail(String email) {
        if (email == null)
            return false;
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.compile(regex).matcher(email).matches();
    }

    // ================== UTILITY ==================
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User(
                rs.getInt("id_user"),
                rs.getString("nom_user"),
                rs.getString("prenom_user"),
                rs.getString("email_user"),
                rs.getString("role_user"),
                rs.getInt("num_user"),
                rs.getString("password"),
                rs.getString("adresse_user"));
        try {
            u.setPhotoPath(rs.getString("photo_path"));
        } catch (SQLException ignored) {
        }
        return u;
    }
}