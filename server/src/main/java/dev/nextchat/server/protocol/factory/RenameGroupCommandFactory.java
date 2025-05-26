package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.messaging.service.RelayService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.RenameGroupCommand;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class RenameGroupCommandFactory implements CommandFactory {
    private final RelayService relayService;

    public RenameGroupCommandFactory(RelayService relayService) {
        this.relayService = relayService;
    }

    @Override
    public Command create(JSONObject json) throws Exception {
        String requestId = json.optString("requestId");
        String groupId = json.optString("groupId");
        String name = json.optString("name");

        return new RenameGroupCommand(requestId, groupId, name, relayService);
    }

    @Override
    public CommandType getType() {
        return CommandType.RENAME_GROUP;
    }
}
