# Java Chat System

A robust, multi-threaded Client-Server Chat Application built in Java using TCP sockets.

## 🚀 Features

*   **Structured JSON Payloads:** Uses Google's Gson library (`gson-2.10.1.jar`) to wrap messages in JSON, ensuring clean and reliable communication between the client and server.
*   **Security First:** Server-side spoofing prevention ensures clients cannot impersonate others. Duplicate usernames are automatically rejected.
*   **Private Messaging:** Send direct messages that only the intended recipient can see.
*   **Online Users List:** See who is currently connected to the chat.
*   **Message Timestamps:** All messages are automatically stamped by the server with the exact time they were received.
*   **High Performance:** Uses `ConcurrentHashMap` for O(1) direct message routing and thread-safe operations.

## 🛠️ How to Compile & Run

### 1. Compile Everything
First, compile all the Java files from the root directory of the project:
```bash
javac -cp "lib/gson-2.10.1.jar" -d out -sourcepath common/src:server/src:client/src client/src/com/chat/client/ChatClient.java server/src/com/chat/server/ChatServer.java
```

### 2. Start the Server
In a terminal, start the server. It will listen on port 5000.
```bash
java -cp "out:lib/gson-2.10.1.jar" com.chat.server.ChatServer
```

### 3. Start a Client
Open a new terminal window to start a client instance. You will be prompted for a username.
```bash
java -cp "out:lib/gson-2.10.1.jar" com.chat.client.ChatClient
```
*(You can open as many terminal windows as you want to simulate multiple users chatting!)*

## 💬 Chat Commands

Once connected to the server, you can use the following commands in the client:

*   `/msg <username> <message>` - Send a private message to a specific user.
*   `/users` - View a list of all currently online users.
*   `/quit` - Disconnect gracefully from the chat system.

## 🏗️ Architecture
*   **Server (`com.chat.server.ChatServer`)**: Listens for incoming connections, spawns a `ClientHandler` thread for each new socket, and routes messages using JSON commands.
*   **Client (`com.chat.client.ChatClient`)**: Connects to the server, handles user input on the main thread, and listens for incoming server messages on a background thread.
*   **Common (`com.chat.common.Message`)**: A shared library defining the `Message` object and `Command` enums used for Gson serialization.
