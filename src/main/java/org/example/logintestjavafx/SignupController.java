package org.example.logintestjavafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignupController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Button loginButton;

    private String imagePath = null;

    @FXML
    public void signupButton(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String phone = phoneField.getText();
        String role = roleComboBox.getValue();

        if (isAnyFieldEmpty(username, email, password, role)) {
            showAlert(Alert.AlertType.ERROR, "Signup Failed", "All fields are required.");
            return;
        }

        addUserToDatabase(username, email, password, phone, role, imagePath);
        showAlert(Alert.AlertType.INFORMATION, "Signup Successful", "Welcome, " + username + "!");
    }

    private boolean isAnyFieldEmpty(String username, String email, String password, String role) {
        return username.isEmpty() || email.isEmpty() || password.isEmpty() || role == null || role.isEmpty();
    }

    private void addUserToDatabase(String username, String email, String password, String phone, String role, String imagePath) {
        DatabaseConnection con = new DatabaseConnection();
        try (Connection connection = con.getConnection()) {
            String query = "INSERT INTO Users (Nom, Email, MotDePasse, telephone, imageUser, Role) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, phone);
            statement.setString(5, imagePath);
            statement.setString(6, role);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("User has been inserted successfully.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error inserting user into database: " + e.getMessage());
        }
    }

    @FXML
    public void uploadImageButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            String destinationDir = "C:/Users/ALIENWARE/Pictures/wallpapers/";
            File destinationFile = new File(destinationDir + file.getName());
            try {
                Files.copy(file.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                imagePath = destinationFile.getAbsolutePath();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "File Error", "Error saving image: " + e.getMessage());
            }
        }
    }

    public void handleLoginButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            BorderPane root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du fichier login.fxml : " + e.getMessage());
        }
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
