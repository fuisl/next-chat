package dev.nextchat.server.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    private final InitiateMessageDB InitiateMessageDB;

    public DataLoader(InitiateMessageDB service) {
        this.InitiateMessageDB = service;
    }

    @Override
    public void run(String... args) {
        InitiateMessageDB.loadMessagesFromJson("database/message.json"); // Adjust path if needed
    }
}