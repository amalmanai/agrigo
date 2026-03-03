import java.sql.*;

public class CheckTacheTable {
    public static void main(String[] args) {
        try {
            Connection cnx = DriverManager.getConnection("jdbc:mysql://localhost:3306/agri_go_db", "root", "");
            DatabaseMetaData metaObject = cnx.getMetaData();
            ResultSet rs = metaObject.getColumns(null, null, "tache", null);
            System.out.println("Columns in 'tache' table:");
            boolean found = false;
            while (rs.next()) {
                found = true;
                String columnName = rs.getString("COLUMN_NAME");
                System.out.println("- " + columnName);
            }
            if (!found) {
                System.out.println("Table 'tache' not found or has no columns.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
