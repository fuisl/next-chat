package dev.nextchat.client.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// Removed: dev.nextchat.client.database.GroupInfo; (already in same package)

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GroupManager {
    private static final File GROUP_FILE = new File("src/main/resources/Db/groupIDs.json"); // Ensure this path is correct
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
            File parentDir = GROUP_FILE.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            if (!GROUP_FILE.exists()) {
                mapper.writeValue(GROUP_FILE, new ArrayList<GroupInfo>());
            }
        } catch (IOException e) {
            System.err.println("Cannot create " + GROUP_FILE + ": " + e.getMessage());
            // throw new RuntimeException("Cannot create " + GROUP_FILE, e); // Or handle more gracefully
        }
    }

    private void loadGroups() {
        try {
            if (GROUP_FILE.exists() && GROUP_FILE.length() > 0) { // Check if file is not empty
                groups = mapper.readValue(
                        GROUP_FILE,
                        new TypeReference<List<GroupInfo>>() {}
                );
            } else {
                groups = new ArrayList<>(); // Initialize if file doesn't exist or is empty
            }
        } catch (IOException e) {
            System.err.println("Cannot read " + GROUP_FILE + ", initializing empty list: " + e.getMessage());
            groups = new ArrayList<>(); // Initialize to empty list on error
            // throw new RuntimeException("Cannot read " + GROUP_FILE, e);
        }
        if (groups == null) { // Sanity check
            groups = new ArrayList<>();
        }
    }

    private synchronized void saveGroups() {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(GROUP_FILE, groups);
        } catch (IOException e) {
            System.err.println("Cannot write " + GROUP_FILE + ": " + e.getMessage());
            // throw new RuntimeException("Cannot write " + GROUP_FILE, e);
        }
    }

    public List<GroupInfo> getAllGroups() {
        return List.copyOf(groups);
    }

    public synchronized GroupInfo getGroupInfo(UUID groupId) {
        if (groupId == null) return null;
        return groups.stream()
                .filter(g -> groupId.equals(g.getGroupId()))
                .findFirst()
                .orElse(null);
    }

    public UUID getOtherMember(UUID groupId, UUID selfId) {
        GroupInfo group = getGroupInfo(groupId);
        if (group != null && group.getMembers() != null) {
            return group.getMembers().stream()
                    .filter(id -> !id.equals(selfId))
                    .findFirst().orElse(null);
        }
        return null;
    }

    public boolean isUserInGroup(UUID userId, UUID groupId) {
        GroupInfo group = getGroupInfo(groupId);
        return group != null && group.getMembers() != null && group.getMembers().contains(userId);
    }

    public synchronized UUID getExistingGroupId(UUID userA, UUID userB) {
        Set<UUID> pair = new HashSet<>(Arrays.asList(userA, userB));
        for (GroupInfo info : groups) {
            if (info.getMembers() != null && new HashSet<>(info.getMembers()).equals(pair)) {
                return info.getGroupId();
            }
        }
        return null;
    }

    public synchronized void addGroupMapping(UUID groupId, UUID userA, UUID userB) {
        if (getGroupInfo(groupId) == null) { // Avoid duplicates
            groups.add(new GroupInfo(groupId, new ArrayList<>(Arrays.asList(userA, userB))));
            saveGroups();
        }
    }

    public synchronized void addGroupWithMembers(UUID groupId, String groupName, List<UUID> memberIds) {
        if (getGroupInfo(groupId) == null) { // Avoid duplicates
            List<UUID> distinctMembers = memberIds.stream().distinct().collect(Collectors.toList());
            GroupInfo newGroup = new GroupInfo(groupId, groupName, distinctMembers);
            groups.add(newGroup);
            saveGroups();
            System.out.println("[GroupManager] Added group '" + groupName + "' with ID: " + groupId);
        } else {
            System.out.println("[GroupManager] Group with ID: " + groupId + " already exists. Not adding again.");
        }
    }

    public synchronized void addGroupMapping(UUID groupId, UUID creatorId, String groupName) {
        if (getGroupInfo(groupId) == null) { // Avoid duplicates
            List<UUID> initialMembers = new ArrayList<>();
            initialMembers.add(creatorId);
            GroupInfo newGroup = new GroupInfo(groupId, groupName, initialMembers);
            groups.add(newGroup);
            saveGroups();
            System.out.println("[GroupManager] Added group '" + groupName + "' with ID: " + groupId + " by creator " + creatorId);
        } else {
            System.out.println("[GroupManager] Group with ID: " + groupId + " already exists. Not adding via creator method.");
        }
    }
    public synchronized void addMemberToGroup(UUID groupId, UUID userIdToAdd) {
        GroupInfo groupInfo = getGroupInfo(groupId);
        if (groupInfo != null) {
            List<UUID> members = groupInfo.getMembers();
            if (members == null) {
                members = new ArrayList<>();
                groupInfo.setMembers(members);
            }
            if (!members.contains(userIdToAdd)) {
                members.add(userIdToAdd);
                saveGroups(); // Save the updated member list
                System.out.println("[GroupManager] Added member " + userIdToAdd + " to group " + groupId);
            } else {
                System.out.println("[GroupManager] Member " + userIdToAdd + " already in group " + groupId);
            }
        } else {
            System.err.println("[GroupManager] Cannot add member: Group " + groupId + " not found.");
        }
    }

    public synchronized void removeGroupLocally(UUID groupId) {
        if (groupId == null) return;
        boolean removed = this.groups.removeIf(group -> group.getGroupId().equals(groupId));
        if (removed) {
            saveGroups();
            System.out.println("[GroupManager] Removed group locally: " + groupId);
        } else {
            System.out.println("[GroupManager] Group not found locally to remove: " + groupId);
        }
    }

    public synchronized void updateGroupNameLocally(UUID groupId, String newName) {
        if (groupId == null || newName == null || newName.trim().isEmpty()) return;
        Optional<GroupInfo> groupOpt = this.groups.stream()
                .filter(group -> group.getGroupId().equals(groupId))
                .findFirst();

        if (groupOpt.isPresent()) {
            groupOpt.get().setGroupName(newName.trim()); //
            saveGroups();
            System.out.println("[GroupManager] Updated group name locally for " + groupId + " to: " + newName.trim());
        } else {
            System.out.println("[GroupManager] Group not found locally to update name: " + groupId);
        }
    }

    public synchronized void clearAllGroups() {
        this.groups.clear();
        saveGroups();
        System.out.println("[GroupManager] All groups cleared from local storage.");
    }
}