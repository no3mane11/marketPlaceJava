package org.example.logintestjavafx;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HelloController {

    private Stage stage; // Stage principal

    public void setStage(Stage stage) {
        this.stage = stage; // Setter pour passer le Stage au contrôleur
    }

    @FXML
    private Button cancelButton;

    @FXML
    private Button signupButton;

    @FXML
    private Button faceRecognitionButton;

    @FXML
    private Label loginMessageLabel;

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordPasswordField;

    @FXML
    public void loginButtonOnAction(ActionEvent event) {
        if (!usernameTextField.getText().isBlank() && !passwordPasswordField.getText().isBlank()) {
            validateLogin();
        } else {
            loginMessageLabel.setText("Please enter your username and password.");
        }
    }

    @FXML
    public void cancelButtonAction(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleFaceRecognitionAction(ActionEvent event) {
        loginMessageLabel.setText("Starting face recognition...");

        String username = usernameTextField.getText();

        if (username.isBlank()) {
            loginMessageLabel.setText("Please enter your username before using face recognition.");
            return;
        }

        try (Connection conDb = new DatabaseConnection().getConnection()) {
            // Récupérer l'image utilisateur, l'ID utilisateur et le nom de la base de données
            String query = "SELECT imageUser, UserID, Nom FROM Users WHERE Nom = ?";
            PreparedStatement preparedStatement = conDb.prepareStatement(query);
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String imagePath = resultSet.getString("imageUser");
                int userId = resultSet.getInt("UserID"); // Récupère l'ID utilisateur
                String userName = resultSet.getString("Nom"); // Récupère le nom de l'utilisateur

                if (compareUserImage(imagePath)) {
                    loginMessageLabel.setText("Face recognition successful. Welcome!");
                    redirectToProduits(userId, userName); // Passe l'ID et le nom à la méthode
                } else {
                    loginMessageLabel.setText("Face not recognized. Access denied.");
                }
            } else {
                loginMessageLabel.setText("User not found in database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            loginMessageLabel.setText("Error during face recognition. Please try again later.");
        }
    }


    @FXML
    public void validateLogin() {
        DatabaseConnection con = new DatabaseConnection();
        Connection conDb = con.getConnection();

        String username = usernameTextField.getText();
        String password = passwordPasswordField.getText();

        String query = "SELECT * FROM Users WHERE Nom = ? AND MotDePasse = ?";

        try {
            PreparedStatement preparedStatement = conDb.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int userId = resultSet.getInt("UserID");
                String userName = resultSet.getString("Nom");

                loginMessageLabel.setText("Login successful!");

                redirectToProduits(userId, userName); // Passez UserID et Nom
            } else {
                loginMessageLabel.setText("Invalid username or password. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            loginMessageLabel.setText("Error during login. Please try again later.");
        }
    }


    @FXML
    public void handleSignupButtonAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("signup.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Récupérer le Stage à partir de l'événement
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(scene); // Utiliser le Stage récupéré
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            loginMessageLabel.setText("Error loading signup page.");
        }
    }


    private boolean compareUserImage(String databaseImagePath) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            loginMessageLabel.setText("Camera not detected. Please check your device.");
            return false;
        }

        Mat frame = new Mat();
        if (camera.read(frame)) {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
            String tempImagePath = "temp_face.jpg";
            Imgcodecs.imwrite(tempImagePath, frame);

            boolean match = isFaceMatched(tempImagePath, databaseImagePath);
            camera.release();
            return match;
        } else {
            camera.release();
            loginMessageLabel.setText("Failed to capture image. Please try again.");
            return false;
        }
    }

    private boolean isFaceMatched(String testImagePath, String referenceImagePath) {
        try {
            Mat testImage = Imgcodecs.imread(testImagePath, Imgcodecs.IMREAD_GRAYSCALE);
            Mat referenceImage = Imgcodecs.imread(referenceImagePath, Imgcodecs.IMREAD_GRAYSCALE);

            Imgproc.resize(testImage, testImage, referenceImage.size());

            Mat diff = new Mat();
            Core.absdiff(testImage, referenceImage, diff);

            Scalar diffScalar = Core.mean(diff);
            double meanDiff = diffScalar.val[0];

            return meanDiff < 50;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void redirectToProduits(int userId, String userName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("produits.fxml"));
            Parent root = loader.load();

            // Configure le contrôleur de la page produits
            ProduitsController produitsController = loader.getController();
            produitsController.setUserSession(userId, userName); // Passez UserID et Nom

            Stage stage = (Stage) usernameTextField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            loginMessageLabel.setText("Error loading produits page. Check the file path.");
        }
    }


}
