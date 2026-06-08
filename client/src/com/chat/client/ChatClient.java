package com.chat.client;

import com.chat.common.Constants;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.chat.common.Command;
import com.chat.common.Message;

/**
 * A simple console-based chat client that connects to the ChatServer.
 * It uses two threads: one for sending user input and another for receiving messages from the server.
 */
public class ChatClient {

    public static void main(String[] args) {

        String serverAddress = Constants.DEFAULT_HOST;
        int port = Constants.DEFAULT_PORT;
        Gson gson = new Gson();

        try {
            // BufferedReader to read input from the console (system keyboard)
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter your username: ");
            String username = userInput.readLine();
            if (username == null || username.trim().isEmpty()) {
                System.out.println("Username cannot be empty. Exiting.");
                return;
            }

            // Establish connection to the server
            Socket socket = new Socket(serverAddress, port);
            System.out.println("Connected to the chat server at " + serverAddress + ":" + port);

            // BufferedReader to read messages incoming from the server
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // PrintWriter to send messages to the server
            PrintWriter serverWriter = new PrintWriter(socket.getOutputStream(), true);

            // Send a JOIN message immediately upon connection
            Message joinMessage = new Message(Command.JOIN, username, "");
            serverWriter.println(gson.toJson(joinMessage));

            /**
             * Dedicated background thread to listen for server messages.
             */
            Thread listenerThread = new Thread(() -> {
                try {
                    String response;
                    while ((response = serverReader.readLine()) != null) {
                        try {
                            Message msg = gson.fromJson(response, Message.class);
                            String time = msg.getTimestamp() != null ? "[" + msg.getTimestamp() + "] " : "";

                            if (msg.getType() == Command.JOIN || msg.getType() == Command.LEAVE || msg.getType() == Command.USERS) {
                                System.out.println(time + "*** " + msg.getTextContent() + " ***"); // System notifications
                            } else if (msg.getType() == Command.ERROR) {
                                System.out.println(time + "[ERROR] " + msg.getTextContent());
                                if (msg.getTextContent().contains("Username already taken")) {
                                    System.exit(1); // Exit if duplicate username
                                }
                            } else if (msg.getType() == Command.PRIVATE) {
                                System.out.println(time + "(Private from " + msg.getUsername() + "): " + msg.getTextContent());
                            } else {
                                System.out.println(time + msg.getUsername() + ": " + msg.getTextContent());
                            }
                        } catch (JsonSyntaxException e) {
                            // Fallback if server sends raw string (like older versions)
                            System.out.println(response);
                        }
                    }
                } catch (IOException e) {
                    if (!socket.isClosed()) {
                        System.err.println("Lost connection to server.");
                    }
                }
            });
            listenerThread.start();

            /**
             * Main thread: Send user input continuously to the server.
             */
            String input;
            while ((input = userInput.readLine()) != null) {
                if (input.equalsIgnoreCase(Constants.EXIT_COMMAND)) {
                    Message quitMessage = new Message(Command.QUIT, username, "");
                    serverWriter.println(gson.toJson(quitMessage));
                    break;
                }
                else if (input.equalsIgnoreCase("/users")) {
                    Message usersMsg = new Message(Command.USERS, username, "");
                    serverWriter.println(gson.toJson(usersMsg));
                    continue;
                }
                else if (input.toLowerCase().startsWith("/msg ")) {
                    // Expected format: /msg targetUser message...
                    String[] parts = input.split(" ", 3);
                    if (parts.length < 3) {
                        System.out.println("Usage: /msg <username> <message>");
                        continue;
                    }
                    String targetUser = parts[1];
                    String privateContent = parts[2];
                    
                    Message privMsg = new Message(Command.PRIVATE, username, privateContent, targetUser);
                    serverWriter.println(gson.toJson(privMsg));
                    System.out.println("(Private to " + targetUser + "): " + privateContent);
                    continue;
                }
                
                // Normal broadcast message
                Message chatMessage = new Message(Command.MESSAGE, username, input);
                serverWriter.println(gson.toJson(chatMessage));
            }

            socket.close();
            System.out.println("Disconnected from server.");

        } catch (IOException e) {
            System.err.println("Could not connect to server: " + e.getMessage());
        }
    }
}

