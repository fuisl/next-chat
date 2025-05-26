package dev.nextchat.server.auth.repository;

import dev.nextchat.server.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    Optional<User> findById(UUID id);

    boolean existsByUsername(String username);

    @Modifying
    @Query(value = "UPDATE user_account acc SET acc.last_online = :time WHERE acc.id = :id", nativeQuery = true)
    int setLastOnlineTimeStamp(@Param("time") Instant time, @Param("id") UUID id);

    @Query(value = "SELECT username FROM user_account WHERE user_account.id IN ?1", nativeQuery = true)
    List<String> findUserNamesByUserIds(List<UUID> user_ids);

    @Query(value = "SELECT * FROM user_account WHERE LOWER(username) LIKE LOWER(CONCAT('%', ?1, '%')) LIMIT 30", nativeQuery = true)
    List<User> findUserByPattern(String search);
}
