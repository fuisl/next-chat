package dev.nextchat.server.messaging.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RelayService manages live socket connections and real-time message delivery.
 */
@Service
public class RelayService {

    // Map from userId to their active socket writer
    private final Map<UUID, PrintWriter> onlineUsers = new ConcurrentHashMap<>();

    /**
     * Registers a user with their socket writer.
     * This should be called once the user logs in successfully.
     */
    public void register(UUID userId, Socket socket) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        onlineUsers.put(userId, writer);
    }

    /**
     * Unregisters a user from relay tracking.
     * Should be called when user disconnects.
     */
    public void unregister(UUID userId) {
        onlineUsers.remove(userId);
    }

    /**
     * Sends a message to a user if they are currently online.
     * The message should already be JSON formatted.
     */
    public void sendToUser(UUID userId, String messageJson) {
        PrintWriter writer = onlineUsers.get(userId);
        if (writer != null) {
            writer.println(messageJson);
        }
    }

    /**
     * Checks if a user is currently connected and registered.
     */
    public boolean isUserOnline(UUID userId) {
        return onlineUsers.containsKey(userId);
    }
}
