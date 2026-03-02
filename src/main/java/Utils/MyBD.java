package Utils;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class MyBD {
   private static final Logger logger = LoggerFactory.getLogger(MyBD.class);
   private Connection conn;
   final private String URL="jdbc:mysql://localhost:3306/agri_go_db";
   final private String USER="root";
   final private String PASS="";
   private static MyBD instance;
    private MyBD(){
        tryConnect();
    }

    private synchronized void tryConnect(){
        try {
            if (conn != null && !conn.isClosed() && conn.isValid(2)) return;
            conn = DriverManager.getConnection(URL,USER,PASS);
            logger.info("Connected to database: {}", URL);
        }catch (SQLException s){
            conn = null;
            logger.error("Erreur de connexion à la base de données ({}): {}", URL, s.getMessage());
        }
    }
   public static MyBD getInstance(){
        if(instance==null){
            instance=new MyBD();
        }
        return instance;
    }

    /**
     * Retourne la connexion JDBC si disponible (essaie de se reconnecter si nécessaire).
     * Peut retourner null si la base est indisponible.
     */
    public synchronized Connection getConn() {
        tryConnect();
        return conn;
    }

    /**
     * Compatibilité avec l'ancien code (module cultures) qui utilisait getConnection().
     * Délègue simplement à getConn().
     */
    public synchronized Connection getConnection() {
        return getConn();
    }

    /**
     * Indique si la connexion à la base est valide.
     */
    public synchronized boolean isConnected(){
        try{
            return conn != null && !conn.isClosed() && conn.isValid(2);
        }catch(SQLException e){
            return false;
        }
    }
}
