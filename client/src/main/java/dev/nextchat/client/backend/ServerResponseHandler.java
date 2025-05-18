package dev.nextchat.client.backend;

import org.json.JSONObject;

public interface ServerResponseHandler {
    void onServerResponse(JSONObject response);
}
