package Tests;

import Entites.Tache;
import Entites.User;
import Services.ServiceTache;
import Utils.MyBD;

import java.sql.Date;
import java.sql.Time;

public class MainConnection {

    public static void main(String[] args) {

        // 1️⃣ Test connexion DB
        MyBD myBD = MyBD.getInstance();
        System.out.println("✅ Connexion DB réussie !");

        // 2️⃣ Création ServiceTache
        ServiceTache st = new ServiceTache();

        // 3️⃣ Création User (clé étrangère)
        // ⚠️ Assure-toi qu'un user avec id=11 existe déjà dans ta table user
        User user = new User(12);

        // 4️⃣ Création Taches (TEST INSERT)
        Tache t1 = new Tache(
                "Préparer réunion",
                "Préparer les slides pour la réunion",
                "Travail",
                user,
                Date.valueOf("2026-02-12"),
                Time.valueOf("09:00:00"),
                Time.valueOf("11:00:00"),
                "En cours",
                "Apporter les documents"
        );

        Tache t2 = new Tache(
                "Sport",
                "Aller courir au parc",
                "Personnel",
                user,
                Date.valueOf("2026-02-13"),
                Time.valueOf("18:00:00"),
                Time.valueOf("19:00:00"),
                "Planifié",
                "Ne pas oublier la bouteille d’eau"
        );

        // 5️⃣ Ajouter taches (décommente pour tester)
        // st.ajouter(t1);
        // st.ajouter(t2);

        // 6️⃣ Afficher toutes les taches
        System.out.println("===== ALL TACHES =====");
        for (Tache t : st.getAll()) {
            System.out.println(t);
        }

        // 7️⃣ Test GET BY ID (ici ID=2)
        System.out.println("===== GET BY ID (2) =====");
        Tache t = st.getOneByID(2);
        if (t != null) {
            System.out.println("Tâche trouvée : " + t);

            // 8️⃣ Test UPDATE sur la tâche ID=2
            t.setStatus_tache("Terminée");
            t.setRemarque_tache("Activité terminée avec succès");
            st.modifier(t);
            System.out.println("Tâche ID=2 mise à jour !");
        } else {
            System.out.println("Aucune tâche avec ID=2");
        }

        // 9️⃣ Test DELETE (⚠️ change id selon ton besoin)
        // st.supprimer(2);
        // System.out.println("Tâche ID=2 supprimée !");
    }
}
