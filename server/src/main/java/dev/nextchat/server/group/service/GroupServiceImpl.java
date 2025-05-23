package dev.nextchat.server.group.service;

import dev.nextchat.server.group.model.Group;
import dev.nextchat.server.group.model.GroupMember;
import dev.nextchat.server.group.model.GroupMemberId;
import dev.nextchat.server.group.repository.GroupRepository;
import dev.nextchat.server.group.repository.GroupMemberRepository;

import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupServiceImpl(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Override
    public Group createGroup(String name, String description, UUID creatorId) {
        Group group = new Group(UUID.randomUUID(), name, description);
        groupRepository.save(group);
        addUserToGroup(group.getId(), creatorId);
        return group;
    }

    @Override
    public boolean addUserToGroup(UUID groupId, UUID userId) {
        GroupMemberId id = new GroupMemberId(userId, groupId);
        if (groupMemberRepository.existsById(id))
            return false;
        groupMemberRepository.save(new GroupMember(userId, groupId, Instant.now()));
        return true;
    }

    @Override
    public boolean removeUserFromGroup(UUID groupId, UUID userId) {
        GroupMemberId id = new GroupMemberId(userId, groupId);
        if (!groupMemberRepository.existsById(id))
            return false;
        groupMemberRepository.deleteById(id);
        return true;
    }

    @Override
    public boolean isUserInGroup(UUID groupId, UUID userId) {
        return groupMemberRepository.existsById(new GroupMemberId(userId, groupId));
    }

    @Override
    public List<Group> getGroupsForUser(UUID userId) {
        return groupMemberRepository.findByIdUserId(userId).stream()
                .map(gm -> groupRepository.findById(gm.getId().getGroupId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<UUID> getUserIdsInGroup(UUID groupId) {
        return groupMemberRepository.findByIdGroupId(groupId).stream()
                .map(gm -> gm.getId().getUserId())
                .collect(Collectors.toList());
    }

    @Override
    public void updateLastRead(UUID groupId, UUID userId, Instant timestamp) {
        GroupMemberId id = new GroupMemberId(userId, groupId);
        groupMemberRepository.findById(id).ifPresent(member -> {
            member.setLastRead(timestamp);
            groupMemberRepository.save(member);
        });
    }

    @Override
    public Optional<Instant> getLastRead(UUID groupId, UUID userId) {
        return groupMemberRepository.findById(new GroupMemberId(userId, groupId))
                .map(GroupMember::getLastRead);
    }

    @Override
    public Optional<Group> getGroupInfo(UUID groupId) {
        return groupRepository.findById(groupId);
    }

    @Override
    public List<Group> getGroupsByPattern(String search) {
        return groupRepository.findGroupByPattern(search);
    }

    @Override
    public Optional<UUID> getGroupWithTwoUsers(UUID userA, UUID userB) {
        Optional<byte[]> groupIdInBytes = groupMemberRepository.findGroupsWithExactlyTwoUsers(userA, userB);

        if (!groupIdInBytes.isPresent()) {
            return Optional.empty();
        }

        ByteBuffer bb = ByteBuffer.wrap(groupIdInBytes.get());
        long high = bb.getLong();
        long low = bb.getLong();

        return Optional.of(new UUID(high, low));
    }
}
