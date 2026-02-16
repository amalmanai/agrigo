package org.example.Services;

import org.example.Entites.Produit;
import org.example.Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitCRUD implements CRUD<Produit> {

    @Override
    public void ajouter(Produit produit) throws SQLException {
        String sql = "INSERT INTO produit (nom_produit, categorie, quantite_disponible, unite, seuil_alerte, date_expiration, prix_unitaire) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = MyBD.getInstance().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, produit.getNomProduit());
            ps.setString(2, produit.getCategorie());
            ps.setInt(3, produit.getQuantiteDisponible());
            ps.setString(4, produit.getUnite());
            ps.setInt(5, produit.getSeuilAlerte());
            ps.setString(6, produit.getDateExpiration());
            ps.setDouble(7, produit.getPrixUnitaire());

            int affectedRows = ps.executeUpdate();
            System.out.println("Produit ajouté, lignes affectées: " + affectedRows);

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                produit.setIdProduit(rs.getInt(1));
                System.out.println("ID généré: " + produit.getIdProduit());
            }
        }
    }

    @Override
    public void modifier(Produit produit) throws SQLException {
        String sql = "UPDATE produit SET nom_produit=?, categorie=?, quantite_disponible=?, unite=?, seuil_alerte=?, date_expiration=?, prix_unitaire=? WHERE id_produit=?";

        try (PreparedStatement ps = MyBD.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, produit.getNomProduit());
            ps.setString(2, produit.getCategorie());
            ps.setInt(3, produit.getQuantiteDisponible());
            ps.setString(4, produit.getUnite());
            ps.setInt(5, produit.getSeuilAlerte());
            ps.setString(6, produit.getDateExpiration());
            ps.setDouble(7, produit.getPrixUnitaire());
            ps.setInt(8, produit.getIdProduit());

            int affectedRows = ps.executeUpdate();
            System.out.println("Produit modifié, lignes affectées: " + affectedRows);
        }
    }

    @Override
    public void supprimer(Produit produit) throws SQLException {
        String sql = "DELETE FROM produit WHERE id_produit=?";

        try (PreparedStatement ps = MyBD.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, produit.getIdProduit());

            int affectedRows = ps.executeUpdate();
            System.out.println("Produit supprimé, lignes affectées: " + affectedRows);
        }
    }

    @Override
    public List<Produit> afficher() throws SQLException {
        System.out.println("=== ProduitCRUD.afficher() ===");
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit ORDER BY id_produit DESC";

        try (Statement st = MyBD.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("Requête exécutée: " + sql);
            int count = 0;

            while (rs.next()) {
                count++;
                Produit p = new Produit(
                        rs.getInt("id_produit"),
                        rs.getString("nom_produit"),
                        rs.getInt("quantite_disponible"),
                        rs.getString("categorie"),
                        rs.getString("unite"),
                        rs.getInt("seuil_alerte"),
                        rs.getString("date_expiration"),
                        rs.getDouble("prix_unitaire")
                );
                produits.add(p);
                System.out.println("  Produit " + count + ": " + p.getNomProduit() + " (ID: " + p.getIdProduit() + ")");
            }

            System.out.println("Total: " + count + " produits trouvés");
            return produits;

        } catch (SQLException e) {
            System.err.println("ERREUR dans afficher(): " + e.getMessage());
            throw e;
        }
    }

    public Produit getById(int id) throws SQLException {
        String sql = "SELECT * FROM produit WHERE id_produit=?";
        try (PreparedStatement ps = MyBD.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Produit(
                        rs.getInt("id_produit"),
                        rs.getString("nom_produit"),
                        rs.getInt("quantite_disponible"),
                        rs.getString("categorie"),
                        rs.getString("unite"),
                        rs.getInt("seuil_alerte"),
                        rs.getString("date_expiration"),
                        rs.getDouble("prix_unitaire")
                );
            }
        }
        return null;
    }

    public Produit getProduitById(int id) throws SQLException {
        String req = "SELECT * FROM produit WHERE id_produit = ?";
        PreparedStatement ps = MyBD.getInstance().getConnection().prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Produit p = new Produit();
            p.setIdProduit(rs.getInt("id_produit"));
            p.setNomProduit(rs.getString("nom_produit"));
            p.setCategorie(rs.getString("categorie"));
            p.setQuantiteDisponible(rs.getInt("quantite_disponible"));
            p.setUnite(rs.getString("unite"));
            p.setSeuilAlerte(rs.getInt("seuil_alerte"));
            p.setDateExpiration(rs.getString("date_expiration"));
            p.setPrixUnitaire(rs.getDouble("prix_unitaire"));
            return p;
        }
        return null;
    }
}