package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import org.json.JSONObject;

public interface CommandFactory {
    Command create(JSONObject json) throws Exception;
    CommandType getType(); // Add this method to get the command type
}
