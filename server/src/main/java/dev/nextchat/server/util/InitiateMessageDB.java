package dev.nextchat.server.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.time.Instant;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import dev.nextchat.server.model.PendingMessage;
import dev.nextchat.server.model.ReceivedMessage;
import dev.nextchat.server.repository.PendingMessageRepository;
import dev.nextchat.server.repository.ReceivedMessageRepository;

@Service
public class InitiateMessageDB {
    
    private final ReceivedMessageRepository receivedRepository;
    private final PendingMessageRepository pendingRepository;

    public InitiateMessageDB(ReceivedMessageRepository received_repo, PendingMessageRepository pending_repo) {
        this.receivedRepository = received_repo;
        this.pendingRepository = pending_repo;
    }

    private Object loadJsonFromResources(String filePath) {
        Object ret = null;

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            JSONParser parser = new JSONParser();

            ret = parser.parse(reader);
        } catch (Exception e) {
            throw new RuntimeException("Error reading JSON file", e);
        }

        return ret;
    }

    public void loadMessagesFromJson(String filePath) {

        Object jsonObj = this.loadJsonFromResources(filePath);

        if (jsonObj instanceof JSONArray) {
            JSONArray messagesArray = (JSONArray) jsonObj;

            // Iterate through messages
            for (Object obj : messagesArray) {
                JSONObject messageObj = (JSONObject) obj;

                // Extract values
                UUID userId = UUID.fromString((String) messageObj.get("userId"));
                UUID groupId = UUID.fromString((String) messageObj.get("groupId"));
                String message = (String) messageObj.get("message");
                String timestamp = (String) messageObj.get("timestamp");

                if (filePath.contains("received_message")) {
                    ReceivedMessage messageDocument = new ReceivedMessage(userId, groupId, message, Instant.parse(timestamp));
                    receivedRepository.save(messageDocument);
                } 
                
                if (filePath.contains("pending_message")) {
                    PendingMessage messageDocument = new PendingMessage(userId, groupId, message, Instant.parse(timestamp));
                    pendingRepository.save(messageDocument);
                }
           }
        }
    }
}
