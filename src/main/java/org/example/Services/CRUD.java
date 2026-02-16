package org.example.Services;

import java.sql.*;
import java.util.List;

public interface CRUD <T> {

    void ajouter(T t)throws SQLException;
    void modifier(T t) throws SQLException;
    void supprimer(T t)throws SQLException;
    List<T> afficher()throws SQLException;

}