package dev.nextchat.server.protocol;

import dev.nextchat.server.protocol.registry.CommandFactoryRegistry;
import dev.nextchat.server.protocol.factory.CommandFactory;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class ProtocolDecoder {

    private final CommandFactoryRegistry registry;

    public ProtocolDecoder(CommandFactoryRegistry registry) {
        this.registry = registry;
    }

    public Command parse(String rawJson) throws Exception {
        JSONObject json = new JSONObject(rawJson);
        CommandType type = CommandType.fromString(json.getString("type"));

        if (!registry.supports(type)) {
            throw new IllegalArgumentException("Unsupported command type: " + type);
        }

        CommandFactory factory = registry.getFactory(type);
        return factory.create(json);
    }
}
