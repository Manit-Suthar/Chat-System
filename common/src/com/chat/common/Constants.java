package com.chat.common;

/**
 * Shared constants used by both the client and server components.
 * This class ensures consistency across the entire chat application.
 */
public class Constants {
    /** The default port number for the chat server to listen on. */
    public static final int DEFAULT_PORT = 5000;

    /** The default hostname for the server (usually localhost). */
    public static final String DEFAULT_HOST = "localhost";

    /** The command string used by clients to disconnect gracefully. */
    public static final String EXIT_COMMAND = "/quit";
}

