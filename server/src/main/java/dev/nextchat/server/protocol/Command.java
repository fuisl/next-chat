package dev.nextchat.server.protocol;

import org.json.JSONObject;

import dev.nextchat.server.auth.service.Authenticator;

public interface Command {
    CommandType getType();

    default JSONObject execute() {
        throw new UnsupportedOperationException("Use context-aware execute()");
    }

    default JSONObject execute(Authenticator authenticator) {
        return execute(); // fallback - override in subclasses
    }
}
