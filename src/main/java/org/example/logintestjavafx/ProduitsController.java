package org.example.logintestjavafx;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class ProduitsController {

    @FXML
    private ListView<Produits> produitsListView;

    @FXML
    private ListView<String> notificationsListView;

    @FXML
    private TextField txtNom, txtDescription, txtPrix, txtQuantite, txtImageProduits;

    @FXML
    private Button btnAjouter, btnModifier, btnSupprimer, btnEffacer, btnImage, btnQuitter;

    @FXML
    private Label welcomeLabel;

    @FXML
    private FlowPane produitsDisponiblesListView;

    private int userId;       // Stocke l'ID de l'utilisateur connecté
    private String userName;  // Stocke le nom de l'utilisateur connecté


    public void setUserId(int userId) {
        this.userId = userId;
        loadUserData();
    }

    private void loadUserData() {
        welcomeLabel.setText("Welcome, User ID: " + userId);
    }

    public void setUserSession(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;

        welcomeLabel.setText("Welcome, " + userName); // Affiche le nom de l'utilisateur sur la page
        loadProduits(); // Charge les produits de l'utilisateur connecté
        loadNotifications(); // Charge les notifications de l'utilisateur connecté
    }


    @FXML
    public void initialize() {
        loadProduits();
        loadProduitsDisponibles();
        loadNotifications();

        btnAjouter.setOnAction(e -> ajouterProduit());
        btnModifier.setOnAction(e -> modifierProduit());
        btnSupprimer.setOnAction(e -> supprimerProduit());
        btnEffacer.setOnAction(e -> effacerChamps());
        btnImage.setOnAction(e -> handleImageSelection());
        btnQuitter.setOnAction(e -> handleQuitter());


        produitsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                txtNom.setText(newValue.getNom());
                txtDescription.setText(newValue.getDescription());
                txtPrix.setText(String.valueOf(newValue.getPrix()));
                txtQuantite.setText(String.valueOf(newValue.getQuantiteStock()));
                txtImageProduits.setText(newValue.getImageProduits());
            }
        });
    }

    private void loadProduits() {
        produitsListView.getItems().clear();
        DatabaseConnection con = new DatabaseConnection();
        Connection conDb = con.getConnection();
        String query = "SELECT * FROM Produits WHERE UserID = ?";

        try (PreparedStatement preparedStatement = conDb.prepareStatement(query)) {
            preparedStatement.setInt(1, userId); // Filtre par UserID

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int produitID = resultSet.getInt("ProduitID");
                String nom = resultSet.getString("Nom");
                String description = resultSet.getString("Description");
                double prix = resultSet.getDouble("Prix");
                int quantiteStock = resultSet.getInt("QuantiteStock");
                String imageProduits = resultSet.getString("ImageProduits");

                Produits produit = new Produits(produitID, nom, description, prix, quantiteStock, imageProduits);
                produitsListView.getItems().add(produit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les produits : " + e.getMessage());
        }
    }

    private void loadProduitsDisponibles() {
        produitsDisponiblesListView.getChildren().clear();
        DatabaseConnection con = new DatabaseConnection();
        Connection conDb = con.getConnection();

        String query = """
    SELECT p.nom AS produitNom, p.description, p.prix, p.imageProduits, u.Nom AS userNom, u.UserID AS proprietaireID
    FROM Produits p
    JOIN Users u ON p.UserID = u.UserID
    """;

        try (PreparedStatement preparedStatement = conDb.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String produitNom = resultSet.getString("produitNom");
                String description = resultSet.getString("description");
                double prix = resultSet.getDouble("prix");
                String imagePath = resultSet.getString("imageProduits");
                String userNom = resultSet.getString("userNom");
                int proprietaireID = resultSet.getInt("proprietaireID");

                VBox carteProduit = new VBox(10);
                carteProduit.setAlignment(Pos.CENTER);
                carteProduit.setStyle("""
            -fx-background-color: #ffffff; 
            -fx-border-color: #4CAF50; 
            -fx-border-radius: 10; 
            -fx-padding: 10; 
            -fx-background-radius: 10;
            """);
                carteProduit.setPrefSize(200, 250);

                ImageView imageView = new ImageView();
                imageView.setFitHeight(100);
                imageView.setFitWidth(100);
                if (imagePath != null && !imagePath.isEmpty()) {
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        imageView.setImage(new Image(imageFile.toURI().toString()));
                    } else {
                        imageView.setImage(new Image("file:placeholder.png"));
                    }
                }

                Label labelNom = new Label(produitNom);
                labelNom.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

                Label labelPrix = new Label("Prix : " + prix + "€");
                labelPrix.setStyle("-fx-font-size: 12px;");

                Label labelUser = new Label("Ajouté par : " + userNom);
                labelUser.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

                Button btnCommander = new Button("Commander");
                btnCommander.setStyle("""
            -fx-background-color: #4CAF50; 
            -fx-text-fill: white; 
            -fx-border-radius: 5;
            """);
                btnCommander.setOnAction(e -> commanderProduit(produitNom, proprietaireID, userId));

                carteProduit.getChildren().addAll(imageView, labelNom, labelPrix, labelUser, btnCommander);
                produitsDisponiblesListView.getChildren().add(carteProduit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les produits disponibles : " + e.getMessage());
        }
    }

    private void commanderProduit(String produitNom, int proprietaireID, int acheteurID) {
        // Récupérer le nom et le téléphone de l'acheteur
        DatabaseConnection con = new DatabaseConnection();
        Connection conDb = con.getConnection();
        String acheteurNom = "";
        String acheteurTelephone = "";

        String queryAcheteur = "SELECT Nom, Telephone FROM Users WHERE UserID = ?";

        try (PreparedStatement preparedStatement = conDb.prepareStatement(queryAcheteur)) {
            preparedStatement.setInt(1, acheteurID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                acheteurNom = resultSet.getString("Nom");
                acheteurTelephone = resultSet.getString("Telephone");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de récupérer les informations de l'acheteur : " + e.getMessage());
            return; // Ne pas continuer si les informations de l'acheteur ne sont pas disponibles
        }

        // Créer le message de notification
        String message = "Votre produit \"" + produitNom + "\" a été commandé par : " + acheteurNom +
                " (Tel : " + acheteurTelephone + ")";

        // Insérer la notification dans la table Notifications
        String queryNotification = "INSERT INTO Notifications (UserID, Message) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = conDb.prepareStatement(queryNotification)) {
            preparedStatement.setInt(1, proprietaireID);
            preparedStatement.setString(2, message);
            preparedStatement.executeUpdate();
            showAlert("Succès", "Produit commandé avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'envoyer la notification : " + e.getMessage());
        }
    }


    private void loadNotifications() {
        notificationsListView.getItems().clear();
        DatabaseConnection con = new DatabaseConnection();
        Connection conDb = con.getConnection();

        String query = "SELECT Message, Date FROM Notifications WHERE UserID = ? ORDER BY Date DESC";

        try (PreparedStatement preparedStatement = conDb.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String message = resultSet.getString("Message");
                String date = resultSet.getTimestamp("Date").toString();
                notificationsListView.getItems().add(date + " - " + message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les notifications : " + e.getMessage());
        }
    }

    private void ajouterProduit() {
        String nom = txtNom.getText();
        String description = txtDescription.getText();
        String imageProduits = txtImageProduits.getText();
        double prix;
        int quantite;

        try {
            prix = Double.parseDouble(txtPrix.getText());
            quantite = Integer.parseInt(txtQuantite.getText());
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Prix et Quantité doivent être des valeurs numériques !");
            return;
        }

        if (nom.isEmpty() || description.isEmpty() || imageProduits.isEmpty()) {
            showAlert("Erreur", "Tous les champs doivent être remplis !");
            return;
        }

        DatabaseConnection con = new DatabaseConnection();
        Connection conDb = con.getConnection();
        String query = "INSERT INTO Produits (Nom, Description, Prix, QuantiteStock, ImageProduits, UserID) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = conDb.prepareStatement(query)) {
            preparedStatement.setString(1, nom);
            preparedStatement.setString(2, description);
            preparedStatement.setDouble(3, prix);
            preparedStatement.setInt(4, quantite);
            preparedStatement.setString(5, imageProduits);
            preparedStatement.setInt(6, userId); // Associe le produit à l'utilisateur connecté
            preparedStatement.executeUpdate();

            loadProduits(); // Recharge la liste des produits
            effacerChamps();
            showAlert("Succès", "Produit ajouté avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ajouter le produit : " + e.getMessage());
        }

    }

    private void modifierProduit() {
        Produits produit = produitsListView.getSelectionModel().getSelectedItem();
        if (produit == null) {
            showAlert("Erreur", "Veuillez sélectionner un produit à modifier !");
            return;
        }

        String nom = txtNom.getText();
        String description = txtDescription.getText();
        String imageProduits = txtImageProduits.getText();
        double prix;
        int quantite;

        try {
            prix = Double.parseDouble(txtPrix.getText());
            quantite = Integer.parseInt(txtQuantite.getText());
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Prix et Quantité doivent être des valeurs numériques !");
            return;
        }

        DatabaseConnection con = new DatabaseConnection();
        Connection conDb = con.getConnection();
        String query = "UPDATE Produits SET nom = ?, description = ?, prix = ?, quantiteStock = ?, imageProduits = ? WHERE produitID = ?";

        try (PreparedStatement preparedStatement = conDb.prepareStatement(query)) {
            preparedStatement.setString(1, nom);
            preparedStatement.setString(2, description);
            preparedStatement.setDouble(3, prix);
            preparedStatement.setInt(4, quantite);
            preparedStatement.setString(5, imageProduits);
            preparedStatement.setInt(6, produit.getProduitID());
            preparedStatement.executeUpdate();

            loadProduits();
            loadProduitsDisponibles();
            effacerChamps();
            showAlert("Succès", "Produit modifié avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de modifier le produit : " + e.getMessage());
        }

    }

    private void supprimerProduit() {
        Produits produit = produitsListView.getSelectionModel().getSelectedItem();
        if (produit == null) {
            showAlert("Erreur", "Veuillez sélectionner un produit à supprimer !");
            return;
        }

        DatabaseConnection con = new DatabaseConnection();
        Connection conDb = con.getConnection();
        String query = "DELETE FROM Produits WHERE produitID = ?";
        try (PreparedStatement preparedStatement = conDb.prepareStatement(query)) {
            preparedStatement.setInt(1, produit.getProduitID());
            preparedStatement.executeUpdate();

            loadProduits();
            loadProduitsDisponibles();
            showAlert("Succès", "Produit supprimé avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de supprimer le produit : " + e.getMessage());
        }

    }

    private void effacerChamps() {
        txtNom.clear();
        txtDescription.clear();
        txtPrix.clear();
        txtQuantite.clear();
        txtImageProduits.clear();
    }

    @FXML
    private void handleQuitter() {
        try {
            // Charger la page login.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Stage currentStage = (Stage) btnQuitter.getScene().getWindow();

            // Remplacer la scène par celle du login
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Login");

            // Facultatif : Réinitialiser l'utilisateur connecté
            userId = 0;
            userName = null;

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de connexion.");
        }
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void handleImageSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        Stage stage = (Stage) btnImage.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            txtImageProduits.setText(file.getAbsolutePath());
        }
    }
}
