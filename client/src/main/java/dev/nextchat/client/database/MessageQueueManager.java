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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueueManager {
    private static final File messageFile = new File("src/main/resources/Db/messages.json");
    private static final ObjectMapper mapper;
    private static final LinkedBlockingQueue<Message> sendQueue = new LinkedBlockingQueue<>();

    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static List<Message> loadMessages() {
        try {
            if (messageFile.exists()) {
                return Arrays.asList(mapper.readValue(messageFile, Message[].class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static void saveMessage(Message message) {
        List<Message> messages = loadMessages();
        messages.add(message);
        try {
            mapper.writeValue(messageFile, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
