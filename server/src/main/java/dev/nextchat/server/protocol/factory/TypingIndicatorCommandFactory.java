package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.messaging.service.RelayService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.TypingIndicatorCommand;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TypingIndicatorCommandFactory implements CommandFactory {

    private final GroupService groupService;
    private final RelayService relayService;

    public TypingIndicatorCommandFactory(GroupService groupService, RelayService relayService) {
        this.groupService = groupService;
        this.relayService = relayService;
    }

    @Override
    public Command create(JSONObject json) throws Exception {
        UUID groupId = UUID.fromString(json.getString("groupId"));
        boolean isTyping = json.getBoolean("isTyping");
        return new TypingIndicatorCommand(groupId, isTyping, groupService, relayService);
    }

    @Override
    public CommandType getType() {
        return CommandType.TYPING_INDICATOR;
    }
}
