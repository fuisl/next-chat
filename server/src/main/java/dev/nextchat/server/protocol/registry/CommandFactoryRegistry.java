package dev.nextchat.server.protocol.registry;

import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.factory.CommandFactory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class CommandFactoryRegistry {

    private final Map<CommandType, CommandFactory> factories = new EnumMap<>(CommandType.class);

    public CommandFactoryRegistry(List<CommandFactory> commandFactories) {
        for (CommandFactory factory : commandFactories) {
            factories.put(factory.getType(), factory);
        }
    }

    public CommandFactory getFactory(CommandType type) {
        if (!factories.containsKey(type)) {
            throw new IllegalArgumentException("No CommandFactory registered for type: " + type);
        }
        return factories.get(type);
    }

    public boolean supports(CommandType type) {
        return factories.containsKey(type);
    }

    public void registerFactory(CommandFactory factory) {
        factories.put(factory.getType(), factory);
    }

    public void unregisterFactory(CommandType type) {
        factories.remove(type);
    }
}
