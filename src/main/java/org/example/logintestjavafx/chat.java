package org.example.logintestjavafx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class chat {

    @FXML
    private ListView<String> conversationList;

    @FXML
    private ListView<Message> messageList;

    @FXML
    private TextArea messageInput;

    @FXML
    private Label chatTitle;

    @FXML
    private Button btnSend;

    @FXML
    private TextField searchUserInput;

    @FXML
    private Button btnSearchUser;

    private int currentUserId;
    private int selectedUserId = -1;

    public chat() {
        // Ensure FXML is loaded
        FXMLLoader loader = new FXMLLoader(getClass().getResource("chat.fxml"));
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        loadConversations();
        chatTitle.setText("Chat");
        messageList.getItems().clear();
        searchUserInput.clear();
    }

    @FXML
    public void initialize() {
        loadConversations();

        // Customize conversation list cell rendering
        conversationList.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String username, boolean empty) {
                super.updateItem(username, empty);
                if (empty || username == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(username);
                    setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0;");
                }
            }
        });

        // Customize message list cell rendering
        messageList.setCellFactory(param -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create a custom layout for each message
                    HBox messageBox = new HBox(10);
                    Text senderText = new Text(message.getSender() + ": ");
                    senderText.setStyle("-fx-font-weight: bold;");

                    Text contentText = new Text(message.getContent());
                    TextFlow textFlow = new TextFlow(senderText, contentText);

                    messageBox.getChildren().add(textFlow);
                    setGraphic(messageBox);
                    setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0;");
                }
            }
        });

        conversationList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedUserId = getUserIdFromUsername(newVal);
                loadMessages(currentUserId, selectedUserId);
                chatTitle.setText("Chat with " + newVal);
            }
        });

        btnSend.setOnAction(e -> sendMessage());
        btnSearchUser.setOnAction(e -> handleSearchUser());
    }

    private void loadConversations() {
        conversationList.getItems().clear();
        List<String> conversations = fetchConversationsFromDatabase(currentUserId);
        conversationList.getItems().addAll(conversations);
    }

    private void loadMessages(int userId1, int userId2) {
        messageList.getItems().clear();
        List<Message> messages = fetchMessagesFromDatabase(userId1, userId2);
        messageList.getItems().addAll(messages);
    }

    private void sendMessage() {
        String messageText = messageInput.getText();
        if (!messageText.isEmpty() && selectedUserId != -1) {
            sendMessageToDatabase(currentUserId, selectedUserId, messageText);
            messageInput.clear();
            loadMessages(currentUserId, selectedUserId);
        }
    }

    private void handleSearchUser() {
        String username = searchUserInput.getText().trim();
        if (!username.isEmpty()) {
            int userId = getUserIdFromUsername(username);
            if (userId != -1) {
                if (userId == currentUserId) {
                    showAlert("Error", "You cannot chat with yourself!");
                } else {
                    selectedUserId = userId;
                    loadMessages(currentUserId, selectedUserId);
                    chatTitle.setText("Chat with " + username);
                }
            } else {
                showAlert("Error", "User not found!");
            }
        } else {
            showAlert("Error", "Please enter a username.");
        }
    }

    private void sendMessageToDatabase(int senderId, int receiverId, String messageText) {
        DatabaseConnection con = new DatabaseConnection();
        Connection conDb = con.getConnection();
        String query = "INSERT INTO messages (sender_id, receiver_id, message_text, timestamp) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conDb.prepareStatement(query)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, messageText);
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to send message: " + e.getMessage());
        }
    }

    private List<String> fetchConversationsFromDatabase(int userId) {
        DatabaseConnection con = new DatabaseConnection();
        Connection conDb = con.getConnection();
        List<String> conversations = new ArrayList<>();
        String query = """
            SELECT u.Nom 
            FROM users u 
            JOIN messages m ON u.UserID = m.sender_id OR u.UserID = m.receiver_id 
            WHERE ? IN (m.sender_id, m.receiver_id) 
            GROUP BY u.Nom
            """;
        try (PreparedStatement stmt = conDb.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                conversations.add(rs.getString("Nom"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load conversations: " + e.getMessage());
        }
        return conversations;
    }

    private List<Message> fetchMessagesFromDatabase(int userId1, int userId2) {
        DatabaseConnection con = new DatabaseConnection();
        Connection conDb = con.getConnection();
        List<Message> messages = new ArrayList<>();
        String query = """
            SELECT u.Nom, m.message_text, m.timestamp 
            FROM messages m 
            JOIN users u ON m.sender_id = u.UserID 
            WHERE (m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?) 
            ORDER BY m.timestamp DESC
            """;
        try (PreparedStatement stmt = conDb.prepareStatement(query)) {
            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId2);
            stmt.setInt(4, userId1);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String sender = rs.getString("Nom");
                String content = rs.getString("message_text");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                messages.add(new Message(sender, content, timestamp));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load messages: " + e.getMessage());
        }
        return messages;
    }

    private int getUserIdFromUsername(String username) {
        DatabaseConnection con = new DatabaseConnection();
        Connection conDb = con.getConnection();
        String query = "SELECT UserID FROM users WHERE Nom = ?";
        try (PreparedStatement stmt = conDb.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("UserID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to fetch user ID: " + e.getMessage());
        }
        return -1;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}