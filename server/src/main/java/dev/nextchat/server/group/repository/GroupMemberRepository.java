package dev.nextchat.server.group.repository;

import dev.nextchat.server.group.model.GroupMember;
import dev.nextchat.server.group.model.GroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {

    // Find all members of a specific group
    List<GroupMember> findByIdGroupId(UUID groupId);

    // Find all groups that a user belongs to
    List<GroupMember> findByIdUserId(UUID userId);
}