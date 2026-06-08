package com.chat.server;

import com.chat.common.Constants;
import com.chat.common.Message;
import com.google.gson.Gson;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The main server class that listens for incoming chat client connections.
 * It manages a list of active clients and handles broadcasting and direct routing.
 */
public class ChatServer {

    /**
     * Map of online clients: Username -> ClientHandler
     * ConcurrentHashMap provides O(1) thread-safe lookups and modifications.
     */
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        int port = Constants.DEFAULT_PORT;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chat Server started on port " + port);

            while (true) {
                System.out.println("Waiting for client...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Start the handler thread. It will register itself once it receives a JOIN message.
                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Attempts to register a new client with a unique username.
     * @return true if successful, false if username is already taken.
     */
    public static boolean registerClient(String username, ClientHandler handler) {
        // putIfAbsent is an atomic, thread-safe operation
        if (clients.putIfAbsent(username, handler) == null) {
            return true;
        }
        return false;
    }

    /**
     * Removes a client from the active map.
     */
    public static void removeClient(String username) {
        if (username != null) {
            clients.remove(username);
        }
    }

    /**
     * Broadcasts a message to all connected clients except the sender.
     */
    public static void broadcast(Message message, ClientHandler sender) {
        String jsonMessage = gson.toJson(message);
        for (ClientHandler client : clients.values()) {
            if (client != sender) {
                client.sendMessage(jsonMessage);
            }
        }
    }

    /**
     * Sends a direct message to a specific user.
     * @return true if delivered, false if user is offline.
     */
    public static boolean sendToUser(Message message, String targetUsername) {
        ClientHandler targetClient = clients.get(targetUsername);
        if (targetClient != null) {
            targetClient.sendMessage(gson.toJson(message));
            return true;
        }
        return false;
    }

    /**
     * Retrieves a list of all currently online usernames.
     */
    public static List<String> getOnlineUsers() {
        return new ArrayList<>(clients.keySet());
    }
}