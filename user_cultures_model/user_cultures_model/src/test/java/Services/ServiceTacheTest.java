package Services;

import Entites.Tache;
import Entites.User;
import org.junit.jupiter.api.Test;
import java.sql.Date;
import java.sql.Time;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ServiceTacheTest {

    ServiceTache service = new ServiceTache();

    @Test
    void testAjouterEtGetAll() {
        User u = new User(1); // doit exister en DB
        Tache t = new Tache("Test Titre", "Description longue",
                "Travail", u, Date.valueOf("2026-02-12"),
                Time.valueOf("09:00:00"), Time.valueOf("10:00:00"),
                "En cours", "Remarque");

        service.ajouter(t);

        Set<Tache> all = service.getAll();
        assertTrue(all.stream().anyMatch(x -> x.getTitre_tache().equals("Test Titre")));
    }

    @Test
    void testSupprimer() {
        // suppose qu'une tâche avec id=1 existe
        service.supprimer(1);
        Tache t = service.getOneByID(1);
        assertNull(t);
    }
}
