package org.example.logintestjavafx;

import javafx.beans.property.*;

public class Produits {
    private final IntegerProperty produitID;
    private final StringProperty nom;
    private final StringProperty description;
    private final DoubleProperty prix;
    private final IntegerProperty quantiteStock;
    private final StringProperty imageProduits;  // Propriété pour le chemin de l'image (renommée)

    // Constructeur
    public Produits(int produitID, String nom, String description, double prix, int quantiteStock, String imageProduits) {
        this.produitID = new SimpleIntegerProperty(produitID);
        this.nom = new SimpleStringProperty(nom);
        this.description = new SimpleStringProperty(description);
        this.prix = new SimpleDoubleProperty(prix);
        this.quantiteStock = new SimpleIntegerProperty(quantiteStock);
        this.imageProduits = new SimpleStringProperty(imageProduits);  // Initialisation du chemin de l'image (renommé)
    }

    // Getters et Setters
    public int getProduitID() { return produitID.get(); }
    public void setProduitID(int produitID) { this.produitID.set(produitID); }

    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    public double getPrix() { return prix.get(); }
    public void setPrix(double prix) { this.prix.set(prix); }

    public int getQuantiteStock() { return quantiteStock.get(); }
    public void setQuantiteStock(int quantiteStock) { this.quantiteStock.set(quantiteStock); }

    // Getter et Setter pour imageProduits (anciennement imagePath)
    public String getImageProduits() { return imageProduits.get(); }
    public void setImageProduits(String imageProduits) { this.imageProduits.set(imageProduits); }

    // Propriétés JavaFX pour la liaison
    public IntegerProperty produitIDProperty() {
        return produitID;
    }

    public StringProperty nomProperty() {
        return nom;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public DoubleProperty prixProperty() {
        return prix;
    }

    public IntegerProperty quantiteStockProperty() {
        return quantiteStock;
    }

    public StringProperty imageProduitsProperty() {  // Propriété pour l'image (renommé)
        return imageProduits;
    }

    // Redéfinir la méthode toString() pour un affichage personnalisé dans ListView
    @Override
    public String toString() {
        return "ID: " + produitID.get() + " | " + nom.get() + " | " + prix.get() + "€";
    }
}
