package dev.nextchat.server.group.repository;

import dev.nextchat.server.group.model.GroupMember;
import dev.nextchat.server.group.model.GroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {

    // Find all members of a specific group
    List<GroupMember> findByIdGroupId(UUID groupId);

    // Find all groups that a user belongs to
    List<GroupMember> findByIdUserId(UUID userId);

    @Query(value = """
            SELECT gm.group_id
            FROM group_member gm
            WHERE gm.user_id IN (?1, ?2)
            GROUP BY gm.group_id
            HAVING COUNT(DISTINCT gm.user_id) = 2
            AND SUM(gm.user_id = ?1) > 0
            AND SUM(gm.user_id = ?2) > 0
            LIMIT 1;
            """, nativeQuery = true)
    Optional<byte[]> findGroupsWithExactlyTwoUsers(UUID userA, UUID userB);

    @Transactional
    @Modifying
    @Query("DELETE FROM GroupMember gm WHERE gm.id.userId = :memberId AND gm.id.groupId = :groupId")
    int deleteByMemberIdAndGroupId(@Param("memberId") UUID memberId, @Param("groupId") UUID groupId);
}
