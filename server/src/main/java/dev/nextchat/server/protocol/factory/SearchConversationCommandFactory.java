package dev.nextchat.server.protocol.factory;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.SearchConversationCommand;

@Component
public class SearchConversationCommandFactory implements CommandFactory {

    @Override
    public CommandType getType() {
        return CommandType.SEARCH_CONVERSATION;
    }

    @Override
    public Command create(JSONObject json) {
        String requestId = json.optString("requestId");
        String search = json.optString("search");
        return new SearchConversationCommand(requestId, search);
    }
}
