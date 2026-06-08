package com.chat.common;

/**
 * Structured data model representing a message in the chat system.
 * This class will be serialized to JSON and sent over the network.
 */
public class Message {
    
    private Command type;
    private String username;
    private String textContent;
    private String targetUser; // Used for PRIVATE messages
    private String timestamp;  // Represents when the message was processed

    // Default constructor needed for Gson reflection
    public Message() {}

    public Message(Command type, String username, String textContent) {
        this.type = type;
        this.username = username;
        this.textContent = textContent;
    }

    public Message(Command type, String username, String textContent, String targetUser) {
        this.type = type;
        this.username = username;
        this.textContent = textContent;
        this.targetUser = targetUser;
    }

    // Getters and Setters
    public Command getType() { return type; }
    public void setType(Command type) { this.type = type; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }

    public String getTargetUser() { return targetUser; }
    public void setTargetUser(String targetUser) { this.targetUser = targetUser; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
