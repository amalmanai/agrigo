package Entites;

import org.junit.jupiter.api.Test;
import java.sql.Date;
import java.sql.Time;
import static org.junit.jupiter.api.Assertions.*;

class TacheTest {

    @Test
    void testConstructeurEtGetters() {
        User u = new User(1);
        Date date = Date.valueOf("2026-02-12");
        Time debut = Time.valueOf("09:00:00");
        Time fin = Time.valueOf("11:00:00");

        Tache t = new Tache(2, "Réunion", "Préparer slides", "Travail",
                u, date, debut, fin, "En cours", "Apporter docs");

        assertEquals(2, t.getId());
        assertEquals("Réunion", t.getTitre_tache());
        assertEquals("Préparer slides", t.getDescription_tache());
        assertEquals("Travail", t.getType_tache());
        assertEquals(u, t.getUser());
        assertEquals(date, t.getDate_tache());
        assertEquals(debut, t.getHeure_debut_tache());
        assertEquals(fin, t.getHeure_fin_tache());
        assertEquals("En cours", t.getStatus_tache());
        assertEquals("Apporter docs", t.getRemarque_tache());
    }

    @Test
    void testEqualsEtHashCode() {
        Tache t1 = new Tache(1, "Titre", "Desc", "Type", new User(1),
                Date.valueOf("2026-02-12"), Time.valueOf("09:00:00"),
                Time.valueOf("10:00:00"), "Status", "Remarque");

        Tache t2 = new Tache(1, "Autre", "Autre", "Autre", new User(2),
                Date.valueOf("2026-02-13"), Time.valueOf("11:00:00"),
                Time.valueOf("12:00:00"), "Autre", "Autre");

        assertEquals(t1, t2); // même id
        assertEquals(t1.hashCode(), t2.hashCode());
    }
}
