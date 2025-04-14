package dev.nextchat.client.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.nextchat.client.models.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueueManager {
    private static final File SEND_FILE = new File("sendQueue.json");
    private static final ObjectMapper mapper;
    private static final LinkedBlockingQueue<Message> sendQueue = new LinkedBlockingQueue<>();

    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // Called from UI when user sends a message
    public static void enqueueMessage(Message message) {
        sendQueue.offer(message); // non-blocking add
        System.out.println("Enqueued message: " + message.getMessage());
    }

    // Writes all queued messages into the JSON file
    public static void flushQueueToFile() {
        try {
            // Ensure file exists
            if (!SEND_FILE.exists() || SEND_FILE.length() == 0) {
                Files.writeString(SEND_FILE.toPath(), "[]");
            }
            // Load existing messages
            List<Message> existing = mapper.readValue(SEND_FILE, new TypeReference<>() {});

            List<Message> toAdd = new ArrayList<>();
            sendQueue.drainTo(toAdd);

            // Append them
            existing.addAll(toAdd);

            mapper.writeValue(SEND_FILE, existing);
            System.out.println("Flushed " + toAdd.size() + " message(s) to sendQueue.json");

        } catch (IOException e) {
            System.err.println("Failed to flush messages to file.");
            e.printStackTrace();
        }
    }
}
