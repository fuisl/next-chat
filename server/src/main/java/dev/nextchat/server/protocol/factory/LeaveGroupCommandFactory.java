package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.LeaveGroupCommand;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class LeaveGroupCommandFactory implements CommandFactory {

    @Override
    public Command create(JSONObject json) throws Exception {
        String requestId = json.optString("requestId");
        String groupId = json.optString("groupId");

        return new LeaveGroupCommand(requestId, groupId);
    }

    @Override
    public CommandType getType() {
        return CommandType.LEAVE_GROUP;
    }
}
