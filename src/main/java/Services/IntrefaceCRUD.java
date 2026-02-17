package Services;

import java.sql.SQLException;
import java.util.List;

public interface IntrefaceCRUD<T> {
    // Standard Create operation
    void ajouter(T t) throws SQLException;

    // Standard Read operation (List all)
    List<T> afficher() throws SQLException;

    // Standard Update operation
    void modifier(T t) throws SQLException;

    // Standard Delete operation
    void supprimer(int id) throws SQLException;
}