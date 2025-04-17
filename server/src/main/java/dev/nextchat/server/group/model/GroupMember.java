package dev.nextchat.server.group.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "group_member")
public class GroupMember {

    @EmbeddedId
    private GroupMemberId id;

    @Column(nullable = false)
    private Instant lastRead;

    public GroupMember() {}

    public GroupMember(UUID userId, UUID groupId, Instant lastRead) {
        this.id = new GroupMemberId(userId, groupId);
        this.lastRead = lastRead;
    }

    public GroupMemberId getId() {
        return id;
    }

    public void setId(GroupMemberId id) {
        this.id = id;
    }

    public Instant getLastRead() {
        return lastRead;
    }

    public void setLastRead(Instant lastRead) {
        this.lastRead = lastRead;
    }

}

