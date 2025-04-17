package dev.nextchat.server.protocol;

import dev.nextchat.server.auth.service.Authenticator;
import dev.nextchat.server.session.service.SessionService;
import dev.nextchat.server.group.service.GroupService;
import java.util.UUID;

public record CommandContext(
        Authenticator authenticator,
        SessionService sessionService,
        GroupService groupService,
        UUID sessionUserId) {

    public boolean isAuthenticated() {
        return sessionUserId != null;
    }
}
