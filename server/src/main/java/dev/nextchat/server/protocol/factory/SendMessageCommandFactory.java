package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.messaging.handler.SendMessageHandler;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.SendMessageCommand;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class SendMessageCommandFactory implements CommandFactory {

    private final SendMessageHandler handler;

    public SendMessageCommandFactory(SendMessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public CommandType getType() {
        return CommandType.SEND_MESSAGE;
    }

    @Override
    public Command create(JSONObject json) {
        String content = json.getString("content");
        String groupId = json.getString("groupId");
        return new SendMessageCommand(content, groupId, handler);
    }
}
