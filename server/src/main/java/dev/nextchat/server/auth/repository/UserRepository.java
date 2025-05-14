package dev.nextchat.server.auth.repository;

import dev.nextchat.server.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Modifying
    @Query(value = "UPDATE user_account acc SET acc.last_online = :time WHERE acc.id = :id", nativeQuery = true)
    int setLastOnlineTimeStamp(@Param("time") Instant time, @Param("id") UUID id);
}
