package dev.nextchat.client.database;

import java.util.List;
import java.util.UUID;

public class GroupInfo {
    private UUID groupId;
    private List<UUID> members;

    public GroupInfo() {
    }

    public GroupInfo(UUID groupId, List<UUID> members) {
        this.groupId = groupId;
        this.members = members;
    }

    public UUID getGroupId() { return groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }

    public List<UUID> getMembers() { return members; }
    public void setMembers(List<UUID> members) { this.members = members; }
}
