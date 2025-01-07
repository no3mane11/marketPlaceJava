package org.example.logintestjavafx;

import java.sql.Timestamp;

public class Message {
    private String sender;
    private String content;
    private Timestamp timestamp;

    public Message(String sender, String content, Timestamp timestamp) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return sender + " (" + timestamp + "): " + content;
    }
}