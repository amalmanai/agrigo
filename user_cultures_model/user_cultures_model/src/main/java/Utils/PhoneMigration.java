package Utils;

import Entites.User;
import Services.ServiceUser;

import java.util.Set;

/**
 * Utilitaire de migration pour normaliser les numéros des utilisateurs déjà présents en base.
 * Exécuter après build : java -cp target/classes Utils.PhoneMigration
 * Il lit tous les utilisateurs, normalise leur numéro via PhoneUtil et met à jour num_user.
 */
public class PhoneMigration {
    public static void main(String[] args) {
        try {
            ServiceUser su = new ServiceUser();
            Set<User> users = su.getAll();
            String defaultCountry = PhoneUtil.getDefaultCountry();
            System.out.println("PhoneMigration: defaultCountry=" + defaultCountry + " found users=" + users.size());
            int updated = 0;
            for (User u : users) {
                try {
                    String raw = String.valueOf(u.getNum_user());
                    if (raw == null || raw.isEmpty()) continue;
                    String normalized = PhoneUtil.normalizePhone(raw, defaultCountry);
                    if (normalized == null) {
                        System.out.println("PhoneMigration: skip user=" + u.getId_user() + " raw=" + raw + " -> cannot normalize");
                        continue;
                    }
                    String national = normalized;
                    if (normalized.startsWith(defaultCountry)) national = normalized.substring(defaultCountry.length());
                    if (national.startsWith("0") && national.length() > 1) national = national.substring(1);
                    if (national.length() > 8) national = national.substring(national.length() - 8);
                    if (national.length() != 8) {
                        System.out.println("PhoneMigration: skip user=" + u.getId_user() + " normalized=" + normalized + " national=" + national + " (unexpected length)");
                        continue;
                    }
                    int num = Integer.parseInt(national);
                    if (u.getNum_user() != num) {
                        u.setNum_user(num);
                        su.modifier(u);
                        updated++;
                        System.out.println("PhoneMigration: updated user=" + u.getId_user() + " -> " + num);
                    }
                } catch (Exception ex) {
                    System.out.println("PhoneMigration: error for user=" + u.getId_user() + " : " + ex.getMessage());
                }
            }
            System.out.println("PhoneMigration: completed. updated=" + updated);
        } catch (Exception e) {
            System.err.println("PhoneMigration: fatal: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }
}

