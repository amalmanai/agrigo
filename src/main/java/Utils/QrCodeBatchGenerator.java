package Utils;

import Entites.User;
import Services.ServiceUser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Génère les QR codes manquants pour tous les utilisateurs existants en base.
 * À exécuter une seule fois si vous avez déjà des users enregistrés avant l'ajout du QR.
 */
public class QrCodeBatchGenerator {

    public static void main(String[] args) {
        ServiceUser serviceUser = new ServiceUser();
        Set<User> users = serviceUser.getAll();

        int generated = 0;
        int alreadyExists = 0;
        int failed = 0;

        for (User u : users) {
            try {
                Path expected = QrCodeUtil.getExpectedUserQrPath(u);
                if (Files.exists(expected)) {
                    alreadyExists++;
                    continue;
                }

                QrCodeUtil.generateUserQrPng(u);
                generated++;
            } catch (Exception e) {
                failed++;
                System.err.println("QR failed for user id=" + u.getId_user() + " email=" + u.getEmail_user());
                e.printStackTrace();
            }
        }

        System.out.println("Done. generated=" + generated + ", alreadyExists=" + alreadyExists + ", failed=" + failed);
        System.out.println("Folder: " + QrCodeUtil.getQrBaseDirectory().toAbsolutePath());
    }
}

