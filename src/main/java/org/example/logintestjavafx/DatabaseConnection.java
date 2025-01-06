package org.example.logintestjavafx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private Connection databaseLink;


    public Connection getConnection() {
        String databaseName = "nhdb";
        String databaseUser = "root"; // Remplacez par votre nom d'utilisateur
        String databasePassword = "No@rc@321"; // Remplacez par votre mot de passe
        String databaseUrl = "jdbc:mysql://localhost:3306/" + databaseName +
                "?useSSL=false&serverTimezone=UTC";

        try {
            // Charger le driver JDBC pour MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Établir la connexion
            databaseLink = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        } catch (ClassNotFoundException e) {
            System.out.println("Le driver JDBC n'a pas été trouvé !");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Erreur lors de la connexion à la base de données !");
            e.printStackTrace();
        }

        return databaseLink;
    }
}
