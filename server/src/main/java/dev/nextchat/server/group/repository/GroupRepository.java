package dev.nextchat.server.group.repository;

import dev.nextchat.server.group.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    boolean existsByName(String name);

    @Query(value = "SELECT cg.* " +
            "FROM nextchat.chat_group cg " +
            "JOIN nextchat.group_member gm ON gm.group_id = cg.id " +
            "WHERE LOWER(cg.name) LIKE LOWER(CONCAT('%', ?1, '%')) " +
            "GROUP BY cg.id " +
            "HAVING COUNT(gm.user_id) > 2 " +
            "LIMIT 30;", nativeQuery = true)
    List<Group> findGroupByPattern(String search);

    @Modifying
    @Query(value = "UPDATE chat_group cg SET cg.name = :name WHERE cg.id = :groupId", nativeQuery = true)
    int updateGroupName(@Param("name") String name, @Param("groupId") UUID groupId);
}
