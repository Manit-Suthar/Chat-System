package com.chat.server;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.chat.common.Command;
import com.chat.common.Message;

/**
 * Handles communication with a single connected chat client.
 */
public class ClientHandler extends Thread {

    private Socket socket;
    private PrintWriter writer;
    private String username;
    private Gson gson;
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.gson = new Gson();
        try {
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error creating writer: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    private String getCurrentTime() {
        return LocalTime.now().format(timeFormatter);
    }

    @Override
    public void run() {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            String rawLine;
            while ((rawLine = reader.readLine()) != null) {
                if (rawLine.trim().isEmpty()) continue;

                try {
                    Message msg = gson.fromJson(rawLine, Message.class);

                    // 1. Enforce JOIN protocol first
                    if (this.username == null) {
                        if (msg.getType() == Command.JOIN) {
                            String requestedUsername = msg.getUsername();
                            if (requestedUsername == null || requestedUsername.trim().isEmpty()) {
                                sendMessage(gson.toJson(new Message(Command.ERROR, "Server", "Invalid username.")));
                                break;
                            }
                            if (ChatServer.registerClient(requestedUsername, this)) {
                                this.username = requestedUsername;
                                System.out.println(username + " joined the chat.");
                                Message sysMsg = new Message(Command.JOIN, "Server", username + " has joined the chat.");
                                sysMsg.setTimestamp(getCurrentTime());
                                ChatServer.broadcast(sysMsg, this);
                                
                                // Send confirmation back to the client
                                Message welcomeMsg = new Message(Command.JOIN, "Server", "Welcome to the chat, " + username + "!");
                                welcomeMsg.setTimestamp(getCurrentTime());
                                sendMessage(gson.toJson(welcomeMsg));
                            } else {
                                sendMessage(gson.toJson(new Message(Command.ERROR, "Server", "Username already taken.")));
                                break; // Disconnect
                            }
                        } else {
                            sendMessage(gson.toJson(new Message(Command.ERROR, "Server", "You must JOIN first.")));
                            break;
                        }
                        continue;
                    }

                    // 2. SECURITY: Force the username to match the registered handler
                    // This prevents malicious clients from spoofing as other users
                    msg.setUsername(this.username);
                    msg.setTimestamp(getCurrentTime());

                    // 3. Handle commands
                    if (msg.getType() == Command.MESSAGE) {
                        System.out.println(msg.getUsername() + ": " + msg.getTextContent());
                        ChatServer.broadcast(msg, this);
                    } 
                    else if (msg.getType() == Command.PRIVATE) {
                        boolean delivered = ChatServer.sendToUser(msg, msg.getTargetUser());
                        if (!delivered) {
                            Message errorMsg = new Message(Command.ERROR, "Server", "User " + msg.getTargetUser() + " not found.");
                            errorMsg.setTimestamp(getCurrentTime());
                            sendMessage(gson.toJson(errorMsg));
                        }
                    }
                    else if (msg.getType() == Command.USERS) {
                        List<String> users = ChatServer.getOnlineUsers();
                        Message usersMsg = new Message(Command.USERS, "Server", "Online users: " + String.join(", ", users));
                        usersMsg.setTimestamp(getCurrentTime());
                        sendMessage(gson.toJson(usersMsg));
                    }
                    else if (msg.getType() == Command.QUIT) {
                        break;
                    }
                } catch (JsonSyntaxException e) {
                    System.err.println("Received malformed JSON from client: " + rawLine);
                }
            }
        } catch (IOException e) {
            System.err.println("Connection error with " + (username != null ? username : "unknown client") + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        if (username != null) {
            System.out.println(username + " left the chat.");
            Message sysMsg = new Message(Command.LEAVE, "Server", username + " has left the chat.");
            sysMsg.setTimestamp(getCurrentTime());
            ChatServer.broadcast(sysMsg, this);
            ChatServer.removeClient(username);
        }
        try {
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

