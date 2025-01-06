package org.example.logintestjavafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginAffichage extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Chargez le fichier FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();

        // Configurez la scène
        Scene scene = new Scene(root);

        // Configurez la fenêtre (Stage)
        primaryStage.setTitle("Login Page");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // Lance l'application JavaFX
    }
}
