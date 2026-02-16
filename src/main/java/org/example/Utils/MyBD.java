package org.example.Utils;

import java.sql.*;

public class MyBD {

    private Connection conn;
    final private String url = "jdbc:mysql://localhost:3306/agri_go_db";
    final private String user = "root";
    final private String pass = "";
    private static MyBD instance;

    private MyBD() {
        connect();
    }

    private void connect() {
        try {
            conn = DriverManager.getConnection(url, user, pass);
            System.out.println("✓ Connecté à la base de données");
        } catch (SQLException e) {
            System.err.println("ERREUR connexion BD: " + e.getMessage());
        }
    }

    public static MyBD getInstance() {
        if (instance == null) {
            instance = new MyBD();
        }
        return instance;
    }

    /**
     * FIX PRINCIPAL : Vérifie si la connexion est valide avant de la retourner.
     * Si la connexion est fermée ou invalide, elle est recréée automatiquement.
     */
    public Connection getConnection() {
        try {
            if (conn == null || conn.isClosed() || !conn.isValid(2)) {
                System.out.println("⚠ Connexion invalide, reconnexion...");
                connect();
            }
        } catch (SQLException e) {
            System.err.println("ERREUR vérification connexion: " + e.getMessage());
            connect();
        }
        return conn;
    }
}
