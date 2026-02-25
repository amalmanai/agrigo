package Entites;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testConstructeurEtGetters() {
        User u = new User(1, "Amine", "Jaafar", "amine@test.com",
                "Admin", 12345678, "secret", "Ariana");

        assertEquals(1, u.getId_user());
        assertEquals("Amine", u.getNom_user());
        assertEquals("Jaafar", u.getPrenom_user());
        assertEquals("amine@test.com", u.getEmail_user());
        assertEquals("Admin", u.getRole_user());
        assertEquals(12345678, u.getNum_user());
        assertEquals("secret", u.getPassword());
        assertEquals("Ariana", u.getAdresse_user());
    }

    @Test
    void testEqualsEtHashCode() {
        User u1 = new User(1);
        User u2 = new User(1);
        User u3 = new User(2);

        assertEquals(u1, u2);
        assertNotEquals(u1, u3);
        assertEquals(u1.hashCode(), u2.hashCode());
    }
}
