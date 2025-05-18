package dev.nextchat.server.group.service;

import dev.nextchat.server.group.model.Group;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupService {

    Group createGroup(String name, String description, UUID creatorId);

    boolean addUserToGroup(UUID groupId, UUID userId);

    boolean removeUserFromGroup(UUID groupId, UUID userId);

    boolean isUserInGroup(UUID groupId, UUID userId);

    List<Group> getGroupsForUser(UUID userId);

    List<UUID> getUserIdsInGroup(UUID groupId);

    Optional<Group> getGroupInfo(UUID groupId);

    void updateLastRead(UUID groupId, UUID userId, Instant timestamp);

    Optional<Instant> getLastRead(UUID groupId, UUID userId);
}
