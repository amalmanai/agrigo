package Utils;

import Entites.User;

import jakarta.mail.Authenticator;
import java.util.Properties;

public class Session {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }

    public static Session getInstance(Properties props, Authenticator authenticator) {
        return new Session();
    }
}
