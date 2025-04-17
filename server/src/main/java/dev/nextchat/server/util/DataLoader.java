package dev.nextchat.server.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * A helper class used for adding sample data and/or create {@code message_db} 
 * database with its necessary collections by using {@code InitiateMessageDB}.
 * This class main function is to run immediately after start-up the method 
 * {@link InitiateMessageDB#loadMessagesFromJson(String)} with a provided
 * {@code JSON} file path. The data are only added when the collections does
 * not exist.
 */

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