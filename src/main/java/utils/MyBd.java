package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBd {
    private String url = "jdbc:mysql://localhost:3306/agri_go_db";
    private String user = "root";
    private String password = "";
    private Connection conn;
    private static MyBd instance;

    private MyBd() {
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connexion établie avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion : " + e.getMessage());
        }
    }

    public static MyBd getInstance() {
        if (instance == null) instance = new MyBd();
        return instance;
    }

    public Connection getConnection() {
        return conn;
    }
}