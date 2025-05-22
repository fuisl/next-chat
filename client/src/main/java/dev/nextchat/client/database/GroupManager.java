package dev.nextchat.client.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.nextchat.client.database.GroupInfo;

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

    public List<GroupInfo> getAllGroups() {
        return List.copyOf(groups);
    }

    public UUID getOtherMember(UUID groupId, UUID selfId) {
        return groups.stream()
                .filter(g -> g.getGroupId().equals(groupId))
                .flatMap(g -> g.getMembers().stream())
                .filter(id -> !id.equals(selfId))
                .findFirst().orElse(null);
    }

    public boolean isUserInGroup(UUID userId, UUID groupId) {
        return groups.stream()
                .filter(g -> g.getGroupId().equals(groupId))
                .anyMatch(g -> g.getMembers().contains(userId));
    }

    public synchronized UUID getExistingGroupId(UUID userA, UUID userB) {
        Set<UUID> pair = new HashSet<>(Arrays.asList(userA, userB));
        for (GroupInfo info : groups) {
            if (new HashSet<>(info.getMembers()).equals(pair)) {
                return info.getGroupId();
            }
        }
        return null;
    }

    public synchronized void addGroupMapping(UUID groupId, UUID userA, UUID userB) {
        groups.add(new GroupInfo(groupId, Arrays.asList(userA, userB)));
        saveGroups();
    }

}
