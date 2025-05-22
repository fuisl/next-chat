package dev.nextchat.client.database;

import java.util.List;
import java.util.UUID;

public class GroupInfo {
    private UUID groupId;
    private List<UUID> members;
    private String groupName; // **** ADDED FIELD ****

    public GroupInfo() {
    }


    public GroupInfo(UUID groupId, List<UUID> members) {
        this.groupId = groupId;
        this.members = members;
        this.groupName = null;
    }

    public GroupInfo(UUID groupId, String groupName, List<UUID> members) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.members = members;
    }


    public UUID getGroupId() { return groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }

    public List<UUID> getMembers() { return members; }
    public void setMembers(List<UUID> members) { this.members = members; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
}