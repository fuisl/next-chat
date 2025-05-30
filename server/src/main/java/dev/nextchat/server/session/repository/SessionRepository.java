package dev.nextchat.server.session.repository;

import dev.nextchat.server.session.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, String> {
    Optional<Session> findByToken(String token);
    Optional<Session> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}
