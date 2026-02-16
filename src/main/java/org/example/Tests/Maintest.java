package org.example.Tests;

import org.example.Entites.User;
import org.example.Entites.Produit;
import org.example.Entites.Mouvement;
import org.example.Services.UserCRUD;
import org.example.Services.ProduitCRUD;
import org.example.Services.MouvementCRUD;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Maintest {

    public static void main(String[] args) {

        // Création des instances CRUD
        UserCRUD userCRUD = new UserCRUD();
        ProduitCRUD produitCRUD = new ProduitCRUD();
        MouvementCRUD mouvementCRUD = new MouvementCRUD();

        try {
            // ================== 1. Ajouter un User ==================
            User user = new User(
                    "Ali",
                    "Ben Salah",
                    "ali@gmail.com",
                    "USER",
                    98765432,
                    "123456",
                    "Sfax"
            );
            userCRUD.createUser(user);
            System.out.println("Utilisateur ajouté : " + user);

            // ================== 2. Ajouter un Produit ==================
            Produit produit = new Produit(
                    "Tomates",
                    "Légumes",
                    100,
                    "kg",
                    20,
                    "2026-06-30",
                    3.5
            );
            produitCRUD.ajouter(produit);
            System.out.println("Produit ajouté : " + produit);

            // ================== 3. Ajouter un Mouvement ==================
            Mouvement mouvement = new Mouvement();
            mouvement.setTypeMouvement("ENTREE");
            mouvement.setDateMouvement(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            mouvement.setQuantite(50);
            mouvement.setMotif("Réception du fournisseur");
            mouvement.setIdProduit(produit.getIdProduit());
            mouvement.setIdUser(user.getIdUser());

            mouvementCRUD.ajouter(mouvement);
            System.out.println("Mouvement ajouté : " + mouvement);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}