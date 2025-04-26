package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.messaging.service.MessageService;
import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.FetchBeforeMessageCommand;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.UUID;

import java.time.Instant;

@Component
public class FetchBeforeMessageCommandFactory implements CommandFactory {
    
    private final MessageService messageService;
    private final GroupService groupService;

    public FetchBeforeMessageCommandFactory(MessageService messageService, GroupService groupService) {
        this.messageService = messageService;
        this.groupService = groupService;
    }

    @Override
    public Command create(JSONObject json) throws Exception {
        UUID groupId = UUID.fromString(json.getString("groupId"));
        Instant timestamp = Instant.parse(json.getString("timestamp"));
        return new FetchBeforeMessageCommand(groupId, messageService, groupService, timestamp);
    }

    @Override
    public CommandType getType() {
        return CommandType.FETCH_BEFORE;
    }
}
