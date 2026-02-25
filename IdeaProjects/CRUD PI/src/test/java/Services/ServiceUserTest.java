package Services;

import Entites.User;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ServiceUserTest {

    ServiceUser service = new ServiceUser();

    @Test
    void testAjouterEtGetAll() {
        User u = new User("Amine", "Jaafar", "amine@test.com",
                "Admin", 12345678, "secret", "Ariana");

        service.ajouter(u);

        Set<User> all = service.getAll();
        assertTrue(all.stream().anyMatch(x -> x.getEmail_user().equals("amine@test.com")));
    }

    @Test
    void testGetOneByID() {
        User u = service.getOneByID(1); // suppose qu'un user avec id=1 existe
        assertNotNull(u);
        assertEquals(1, u.getId_user());
    }

    @Test
    void testSupprimer() {
        // suppose qu'un user avec id=2 existe
        service.supprimer(2);
        User u = service.getOneByID(2);
        assertNull(u);
    }

    @Test
    void testAuthenticate() {
        User u = service.authenticate("amine@test.com", "secret");
        assertNotNull(u, "Authentification échouée alors que l'utilisateur existe");
        assertEquals("amine@test.com", u.getEmail_user());
    }
}
