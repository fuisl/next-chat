package dev.nextchat.server.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    private final InitiateMessageDB InitiateMessageDB;
    @Autowired
    private MongoTemplate mongoTemplate;

    public DataLoader(InitiateMessageDB service) {
        this.InitiateMessageDB = service;
    }

    @Override
    public void run(String... args) {
        // Adjust path if needed

        if (!mongoTemplate.collectionExists("received_message")) {
            System.out.println("Loading sample received messages...");
            InitiateMessageDB.loadMessagesFromJson("database/received_message.json");
        }

        if (!mongoTemplate.collectionExists("pending_message")) {
            System.out.println("Loading sample pending messages...");
            InitiateMessageDB.loadMessagesFromJson("database/pending_message.json"); 
        }
    }
}