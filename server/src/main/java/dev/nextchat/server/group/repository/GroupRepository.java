package dev.nextchat.server.group.repository;

import dev.nextchat.server.group.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    boolean existsByName(String name);
}