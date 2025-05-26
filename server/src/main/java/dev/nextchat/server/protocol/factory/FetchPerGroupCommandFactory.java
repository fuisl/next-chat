package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.messaging.service.MessageService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.FetchPerGroupCommand;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class FetchPerGroupCommandFactory implements CommandFactory {
    private final MessageService messageService;

    public FetchPerGroupCommandFactory(MessageService messageService) {
        this.messageService = messageService;

    }

    @Override
    public Command create(JSONObject json) throws Exception {
        String requestId = json.optString("requestId");
        String timestampStr = json.getString("timestamp");

        // Validate and parse the timestamp
        Instant timestamp;
        try {
            if (!timestampStr.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z$")) {
                throw new IllegalArgumentException("Invalid timestamp format. Expected ISO-8601 format.");
            }
            timestamp = Instant.parse(timestampStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse timestamp: " + e.getMessage(), e);
        }

        return new FetchPerGroupCommand(requestId, timestamp, messageService);
    }

    @Override
    public CommandType getType() {
        return CommandType.FETCH_PER_GROUP;
    }
}
