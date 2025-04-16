package dev.nextchat.server.protocol;

import org.json.JSONObject;

public interface Command {
    CommandType getType();

    JSONObject execute();
}
