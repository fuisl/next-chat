package dev.nextchat.server.protocol;

import dev.nextchat.server.auth.service.Authenticator;
import dev.nextchat.server.session.service.SessionService;

public record CommandContext(
        Authenticator authenticator,
        SessionService sessionService) {
}
