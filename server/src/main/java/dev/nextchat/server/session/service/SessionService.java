package dev.nextchat.server.session.service;

import java.util.Optional;
import java.util.UUID;

public interface SessionService {

    void registerSession(String token, UUID userId);

    boolean isValid(String token);

    Optional<UUID> getUserId(String token);

    void removeSession(String token);

    boolean isUserOnline(UUID userId);
}
