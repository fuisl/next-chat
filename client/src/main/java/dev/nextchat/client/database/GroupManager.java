package dev.nextchat.client.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GroupManager {
    private static final File GROUP_FILE = new File("src/main/resources/Db/groupIDs.json");
    private final ObjectMapper mapper;
    private List<GroupInfo> groups;

    public GroupManager() {
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        ensureFileExists();
        loadGroups();
    }

    private void ensureFileExists() {
        try {
            if (!GROUP_FILE.exists()) {
                mapper.writeValue(GROUP_FILE, new ArrayList<GroupInfo>());
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot create " + GROUP_FILE, e);
        }
    }

    private void loadGroups() {
        try {
            groups = mapper.readValue(
                    GROUP_FILE,
                    new TypeReference<List<GroupInfo>>() {}
            );
        } catch (IOException e) {
            throw new RuntimeException("Cannot read " + GROUP_FILE, e);
        }
    }

    private void saveGroups() {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(GROUP_FILE, groups);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write " + GROUP_FILE, e);
        }
    }

    public UUID getOtherMember(UUID groupId, UUID selfId) {
        return groups.stream()
                .filter(g -> g.getGroupId().equals(groupId))
                .findFirst()
                .map(g -> g.getMembers().stream()
                        .filter(id -> !id.equals(selfId))
                        .findFirst()
                        .orElse(null)
                )
                .orElse(null);
    }


    public boolean isUserInGroup(UUID userId, UUID groupId) {
        return groups.stream()
                .filter(g -> g.getGroupId().equals(groupId))
                .findFirst()
                .map(g -> g.getMembers().contains(userId))
                .orElse(false);
    }

    public UUID getOrCreateGroupId(UUID userA, UUID userB) {
        // 1) Build a set for easy comparison (order doesn't matter)
        Set<UUID> pair = new HashSet<>(Arrays.asList(userA, userB));

        // 2) Search existing groups for exactly those two members
        for (GroupInfo info : groups) {
            Set<UUID> members = new HashSet<>(info.getMembers());
            if (members.equals(pair)) {
                return info.getGroupId();
            }
        }

        // 3) Not found → create a new group
        UUID newGroupId = UUID.randomUUID();
        GroupInfo newGroup = new GroupInfo(newGroupId, new ArrayList<>(pair));
        groups.add(newGroup);

        // 4) Persist back to disk
        saveGroups();

        return newGroupId;
    }

    public List<GroupInfo> getAllGroups() {
        return List.copyOf(groups);
    }
}
