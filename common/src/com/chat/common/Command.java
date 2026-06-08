package com.chat.common;

/**
 * Enumeration of supported commands in the chat system.
 * This helps in identifying the type of message being processed.
 */
public enum Command {
    JOIN,    // When a user joins the chat
    MESSAGE, // A standard chat message
    LEAVE,   // When a user leaves the chat
    QUIT,    // Command to exit the application
    HELP,    // Command to show help instructions
    PRIVATE, // A private message sent directly to one user
    USERS,   // Request for the list of online users
    ERROR;   // Error response from the server

    /**
     * Converts a string to its corresponding Command enum.
     * 
     * @param commandStr The string representation of the command.
     * @return The Command enum, or null if no match is found.
     */
    public static Command fromString(String commandStr) {
        try {
            return Command.valueOf(commandStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

