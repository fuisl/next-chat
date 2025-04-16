package dev.nextchat.server.protocol;

import org.json.JSONObject;

public class ProtocolDecoder {

    public static Command parse(String rawJson) throws Exception {
        JSONObject json = new JSONObject(rawJson);

        String type = json.optString("type");
        CommandType commandType = CommandType.fromString(type);

        return switch (commandType) {
            case LOGIN -> new LoginCommand(
                    json.getString("username"),
                    json.getString("passwd"));
            case SIGNUP -> new SignupCommand(
                    json.getString("username"),
                    json.getString("passwd"));
            default -> throw new IllegalArgumentException("Unknown command type: " + type);
        };
    }
}
