package Utils;

import Entites.User;

import jakarta.mail.Authenticator;
import java.util.Properties;

public class Session {
    private static User currentUser;
    private static Session instance;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public User getUser() {
        return currentUser;
    }

    public void setUser(User user) {
        currentUser = user;
    }

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
