package dev.nextchat.server.protocol;

import dev.nextchat.server.auth.repository.UserRepository;
import dev.nextchat.server.auth.service.Authenticator;
import dev.nextchat.server.session.service.SessionService;
import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.session.model.Session;
import java.util.UUID;

public record CommandContext(
        Authenticator authenticator,
        SessionService sessionService,
        GroupService groupService,
        UUID sessionUserId,
        UserRepository userRepository) {

    public boolean isAuthenticated() {
        return sessionUserId != null;
    }

    public Session getSession() {
        return sessionService.getSession(sessionUserId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
    }
    public UserRepository getUserRepository() {
        return userRepository;
    }
}
