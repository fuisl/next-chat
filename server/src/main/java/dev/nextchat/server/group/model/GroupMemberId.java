package dev.nextchat.server.group.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class GroupMemberId implements Serializable {

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID groupId;

    public GroupMemberId() {
    }

    public GroupMemberId(UUID userId, UUID groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GroupMemberId that))
            return false;
        return Objects.equals(userId, that.userId) && Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, groupId);
    }

    @Override
    public String toString() {
        return "GroupMemberId{" +
                "userId=" + userId +
                ", groupId=" + groupId +
                '}';
    }
}
