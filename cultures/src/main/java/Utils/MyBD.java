package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBD {
    private Connection connection;
    private final String URL = "jdbc:mysql://localhost:3306/agri_go_db";
    private final String USER = "root";
    private final String PASS = "";

    private static MyBD instance;

    private MyBD() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected to Database!");
        } catch (SQLException e) {
            System.err.println("Database Connection Failed: " + e.getMessage());
        }
    }

    public static MyBD getInstance() {
        if (instance == null) {
            instance = new MyBD();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}