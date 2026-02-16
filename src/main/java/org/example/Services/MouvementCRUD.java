package org.example.Services;

import org.example.Entites.Mouvement;
import org.example.Entites.Produit;
import org.example.Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MouvementCRUD {

    private Connection connection = MyBD.getInstance().getConnection();

    // Ajouter un mouvement et mettre à jour le stock
    public void ajouter(Mouvement mouvement) throws SQLException {
        String req = "INSERT INTO mouvement_stock (type_mouvement, date_mouvement, quantite, motif, id_produit, id_user) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, mouvement.getTypeMouvement());
        ps.setString(2, mouvement.getDateMouvement());
        ps.setInt(3, mouvement.getQuantite());
        ps.setString(4, mouvement.getMotif());
        ps.setInt(5, mouvement.getIdProduit());
        ps.setInt(6, mouvement.getIdUser());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            mouvement.setIdMouvement(rs.getInt(1));
        }

        mettreAJourStockProduit(mouvement);
    }

    private void mettreAJourStockProduit(Mouvement mouvement) throws SQLException {
        ProduitCRUD produitCRUD = new ProduitCRUD();
        Produit produit = produitCRUD.getProduitById(mouvement.getIdProduit());

        if (produit != null) {
            int nouvelleQuantite;
            if ("ENTREE".equals(mouvement.getTypeMouvement())) {
                nouvelleQuantite = produit.getQuantiteDisponible() + mouvement.getQuantite();
            } else {
                nouvelleQuantite = produit.getQuantiteDisponible() - mouvement.getQuantite();
            }

            String req = "UPDATE produit SET quantite_disponible = ? WHERE id_produit = ?";
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, nouvelleQuantite);
            ps.setInt(2, mouvement.getIdProduit());
            ps.executeUpdate();
        }
    }

    public List<Mouvement> afficher() throws SQLException {
        List<Mouvement> mouvements = new ArrayList<>();
        String req = "SELECT m.*, p.nom_produit FROM mouvement_stock m " +
                "JOIN produit p ON m.id_produit = p.id_produit " +
                "ORDER BY m.date_mouvement DESC";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Mouvement m = new Mouvement();
            m.setIdMouvement(rs.getInt("id_mouvement"));
            m.setTypeMouvement(rs.getString("type_mouvement"));
            m.setDateMouvement(rs.getString("date_mouvement"));
            m.setQuantite(rs.getInt("quantite"));
            m.setMotif(rs.getString("motif"));
            m.setIdProduit(rs.getInt("id_produit"));
            m.setIdUser(rs.getInt("id_user"));
            m.setNomProduit(rs.getString("nom_produit"));
            mouvements.add(m);
        }
        return mouvements;
    }

    public void modifier(Mouvement mouvement) throws SQLException {
        Mouvement ancienMouvement = getMouvementById(mouvement.getIdMouvement());

        String req = "UPDATE mouvement_stock SET type_mouvement = ?, date_mouvement = ?, quantite = ?, motif = ?, id_produit = ? WHERE id_mouvement = ?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, mouvement.getTypeMouvement());
        ps.setString(2, mouvement.getDateMouvement());
        ps.setInt(3, mouvement.getQuantite());
        ps.setString(4, mouvement.getMotif());
        ps.setInt(5, mouvement.getIdProduit());
        ps.setInt(6, mouvement.getIdMouvement());
        ps.executeUpdate();

        annulerEffetMouvement(ancienMouvement);
        mettreAJourStockProduit(mouvement);
    }

    public void supprimer(Mouvement mouvement) throws SQLException {
        annulerEffetMouvement(mouvement);

        String req = "DELETE FROM mouvement_stock WHERE id_mouvement = ?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, mouvement.getIdMouvement());
        ps.executeUpdate();
    }

    private void annulerEffetMouvement(Mouvement mouvement) throws SQLException {
        ProduitCRUD produitCRUD = new ProduitCRUD();
        Produit produit = produitCRUD.getProduitById(mouvement.getIdProduit());

        if (produit != null) {
            int nouvelleQuantite;
            if ("ENTREE".equals(mouvement.getTypeMouvement())) {
                nouvelleQuantite = produit.getQuantiteDisponible() - mouvement.getQuantite();
            } else {
                nouvelleQuantite = produit.getQuantiteDisponible() + mouvement.getQuantite();
            }

            String req = "UPDATE produit SET quantite_disponible = ? WHERE id_produit = ?";
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, nouvelleQuantite);
            ps.setInt(2, mouvement.getIdProduit());
            ps.executeUpdate();
        }
    }

    public Mouvement getMouvementById(int id) throws SQLException {
        String req = "SELECT * FROM mouvement_stock WHERE id_mouvement = ?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Mouvement m = new Mouvement();
            m.setIdMouvement(rs.getInt("id_mouvement"));
            m.setTypeMouvement(rs.getString("type_mouvement"));
            m.setDateMouvement(rs.getString("date_mouvement"));
            m.setQuantite(rs.getInt("quantite"));
            m.setMotif(rs.getString("motif"));
            m.setIdProduit(rs.getInt("id_produit"));
            m.setIdUser(rs.getInt("id_user"));
            return m;
        }
        return null;
    }

    public List<Mouvement> getMouvementsByProduit(int idProduit) throws SQLException {
        List<Mouvement> mouvements = new ArrayList<>();
        String req = "SELECT m.*, p.nom_produit FROM mouvement_stock m " +
                "JOIN produit p ON m.id_produit = p.id_produit " +
                "WHERE m.id_produit = ? ORDER BY m.date_mouvement DESC";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, idProduit);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Mouvement m = new Mouvement();
            m.setIdMouvement(rs.getInt("id_mouvement"));
            m.setTypeMouvement(rs.getString("type_mouvement"));
            m.setDateMouvement(rs.getString("date_mouvement"));
            m.setQuantite(rs.getInt("quantite"));
            m.setMotif(rs.getString("motif"));
            m.setIdProduit(rs.getInt("id_produit"));
            m.setIdUser(rs.getInt("id_user"));
            m.setNomProduit(rs.getString("nom_produit"));
            mouvements.add(m);
        }
        return mouvements;
    }

    public int calculerStockActuel(int idProduit) throws SQLException {
        String req = "SELECT SUM(CASE WHEN type_mouvement = 'ENTREE' THEN quantite ELSE -quantite END) as stock " +
                "FROM mouvement_stock WHERE id_produit = ?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, idProduit);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("stock");
        }
        return 0;
    }
}