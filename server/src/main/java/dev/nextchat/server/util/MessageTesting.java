package dev.nextchat.server.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dev.nextchat.server.model.ReceivedMessage;
import dev.nextchat.server.repository.PendingMessageRepository;
import dev.nextchat.server.repository.ReceivedMessageRepository;

// This is a useless helper class to test the outputs of created repositories.
// No doc is provided since this class should be deleted as soon as the message
// service is deployed.
@Service
public class MessageTesting {
    private final ReceivedMessageRepository receivedMessageRepository;
    private final PendingMessageRepository pendingMessageRepository;

    public MessageTesting(ReceivedMessageRepository received_repo, PendingMessageRepository pending_repo) {
        this.receivedMessageRepository = received_repo;
        this.pendingMessageRepository = pending_repo;
    }

    public void printReceivedMessages() {
        List<ReceivedMessage> messages = receivedMessageRepository.findByGroupId(UUID.fromString("6563d8a8-03b7-4422-bf18-78eecd5f707c"));
        System.out.printf("Found %d\n", messages.size());
        
        if (messages.isEmpty()) {
            System.out.println("No messages found.");
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
            try {
                objectMapper.writeValue(new File(getClass().getClassLoader().getResource("database/result_messages.json").getPath()), messages);
                System.out.println("Messages saved to messages.json");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
