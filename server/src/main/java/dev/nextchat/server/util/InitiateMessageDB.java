package dev.nextchat.server.util;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Instant;
import java.util.Scanner;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import dev.nextchat.server.model.ReceivedMessage;
import dev.nextchat.server.repository.ReceivedMessageRepository;

@Service
public class InitiateMessageDB {
    
    private final ReceivedMessageRepository messageRepository;

    public InitiateMessageDB(ReceivedMessageRepository repository) {
        this.messageRepository = repository;
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

        System.out.println("Loading sample data...");

        Object jsonObj = this.loadJsonFromResources(filePath);

        if (jsonObj instanceof JSONArray) {
            JSONArray messagesArray = (JSONArray) jsonObj;

            // Iterate through messages
            for (Object obj : messagesArray) {
                JSONObject messageObj = (JSONObject) obj;

                // Extract values
                UUID senderId = UUID.fromString((String) messageObj.get("senderId"));
                UUID groupId = UUID.fromString((String) messageObj.get("groupId"));
                String message = (String) messageObj.get("message");
                String timestamp = (String) messageObj.get("timestamp");

                // Print or process data
                System.out.println("Sender: " + senderId);
                System.out.println("Group: " + groupId);
                System.out.println("Message: " + message);
                System.out.println("Timestamp: " + timestamp);
                System.out.println("----------------------------");

                ReceivedMessage receivedMessage = new ReceivedMessage(senderId, groupId, message, Instant.parse(timestamp));
                messageRepository.save(receivedMessage);
            }
        }
    }
}
