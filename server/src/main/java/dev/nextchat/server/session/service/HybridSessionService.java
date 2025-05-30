package dev.nextchat.server.session.service;

import dev.nextchat.server.session.model.Session;
import dev.nextchat.server.session.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HybridSessionService implements SessionService {

    private final SessionRepository sessionRepository;

    // In-memory session maps for quick access
    private final Map<String, UUID> tokenToUserMap = new ConcurrentHashMap<>();
    private final Map<UUID, String> userToTokenMap = new ConcurrentHashMap<>();

    public HybridSessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void registerSession(String token, UUID userId) {
        tokenToUserMap.put(token, userId);
        userToTokenMap.put(userId, token);

        // Persist the session for durability
        Session session = new Session(token, userId, Instant.now());
        sessionRepository.save(session);
    }

    @Override
    public boolean isValid(String token) {
        return tokenToUserMap.containsKey(token);
    }

    @Override
    public Optional<UUID> getUserId(String token) {
        return Optional.ofNullable(tokenToUserMap.get(token));
    }

    @Override
    public void removeSession(String token) {
        UUID userId = tokenToUserMap.remove(token);
        if (userId != null) {
            userToTokenMap.remove(userId);
        }
        sessionRepository.deleteById(token);
    }

    @Override
    public boolean isUserOnline(UUID userId) {
        return userToTokenMap.containsKey(userId);
    }

    // Load existing sessions from database into memory
    public void preloadSessionsFromDatabase() {
        for (Session entry : sessionRepository.findAll()) {
            tokenToUserMap.put(entry.getToken(), entry.getUserId());
            userToTokenMap.put(entry.getUserId(), entry.getToken());
        }
    }

    @Override
    public Optional<Session> getSession(String token) {
        return sessionRepository.findById(token);
    }

    @Override
    public Optional<Session> getSession(UUID userId) {
        return userToTokenMap.containsKey(userId) ? 
               sessionRepository.findById(userToTokenMap.get(userId)) : 
               Optional.empty();
    }
}
