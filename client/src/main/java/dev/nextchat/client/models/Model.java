package dev.nextchat.client.models;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import dev.nextchat.client.database.UserDatabase;
import dev.nextchat.client.views.ViewFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import dev.nextchat.client.database.MessageQueueManager;

public class Model {
    private  static Model model;
    private final ViewFactory viewFactory;
    private final ObservableList<ChatCell> chatCells;
    private final StringProperty loggedInUser = new SimpleStringProperty();
    private final LinkedBlockingQueue<Message> sendMessageQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Message> receivedMessageQueue = new LinkedBlockingQueue<>();
    private final Map<UUID, Set<UUID>> groupIdToUsers = new HashMap<>();
    private final Map<String, UUID> userPairToGroupMap = new HashMap<>();

    BiMap<String, UUID> userIdMap = HashBiMap.create();
    private final MessageQueueManager messageQueueManager = new MessageQueueManager();


    private Model() {
        this.viewFactory = new ViewFactory();
        this.chatCells = FXCollections.observableArrayList();
    }

    public static synchronized Model getInstance() {
        if (model == null) {
            model = new Model();
        }
        return model;
    }

    public void preloadUserIdMapFromJson() {
        try {
            for (User user : UserDatabase.loadUsers()) {
                String username = user.getUsername();
                UUID userId = user.getUserId();
    
                if (username != null && userId != null) {
                    userIdMap.put(username, userId);
                } else {
                    System.err.println("Skipped user due to null: username=" + username + ", userId=" + userId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StringProperty loggedInUserProperty() {
        return loggedInUser;
    }

    public String getLoggedInUser() {
        return loggedInUser.get();
    }

    public UUID getLoggedInUserId() {
        return userIdMap.get(getLoggedInUser());
    }

    public void setLoggedInUser(String username) {
        this.loggedInUser.set(username);
    }



    public boolean isUserInGroup(UUID userId, UUID groupId) {
        Set<UUID> members = groupIdToUsers.get(groupId);
        if (members == null) return false;

        for (UUID member : members) {
            if (member.equals(userId)) {
                return true;
            }
        }
        return false;
    }
    public Map<UUID, String> getUserIdToUsernameMap() {
        return userIdMap.inverse();
    }

    public boolean userExists(String username) {
        return userIdMap.containsKey(username);
    }

    public boolean login(String username, String password) {
        boolean valid = UserDatabase.authenticate(username, password);
        if (valid) setLoggedInUser(username);
        return valid;
    }

    public boolean registerUser(String username, String password) {
        try {
            if (UserDatabase.userExists(username)) {
                return false; // User already exists
            }

            UUID uuid = UUID.randomUUID();
            User newUser = new User(uuid, username, password);
            UserDatabase.registerUser(newUser);
            setLoggedInUser(username);
            userIdMap.put(username, uuid);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void loadMessagesForUser(UUID loggedInUserId) {
        List<Message> allMessages = messageQueueManager.loadMessages();
        System.out.println("🔍 Loading messages for: " + userIdMap.inverse().get(loggedInUserId));


        for (Message msg : allMessages) {
            UUID groupId = msg.getGroupId();
            UUID senderId = msg.getSenderId();

            System.out.println("Checking message:");
            System.out.println("  - groupId: " + msg.getGroupId());
            System.out.println("  - senderId: " + msg.getSenderId());
            System.out.println("  - userInGroup? " + isUserInGroup(loggedInUserId, msg.getGroupId()));

            if (!isUserInGroup(loggedInUserId, groupId)) continue;

            // Case: RECEIVED message (sender ≠ me)
            if (!senderId.equals(loggedInUserId)) {
                String senderUsername = userIdMap.inverse().get(senderId);
                ChatCell cell = findOrCreateChatCell(senderUsername);
                if (cell != null) {
                    cell.addMessage(msg);
                }
                // Case: SENT message → figure out recipient
            } else {
                String recipientUsername = resolveRecipientFromGroup(groupId, loggedInUserId);
                if (recipientUsername != null && !recipientUsername.equals(userIdMap.inverse().get(loggedInUserId))) {
                    ChatCell cell = findOrCreateChatCell(recipientUsername);
                    if (cell != null) {
                        cell.addMessage(msg);
                    }
                } else {
                    System.out.println("⚠️ Could not resolve recipient for groupId: " + groupId);
                }
            }


        }

    }


    public String resolveRecipientFromGroup(UUID groupId, UUID selfId) {
        Set<UUID> members = groupIdToUsers.get(groupId);
        if (members == null) return null;

        for (UUID uid : members) {
            if (!uid.equals(selfId)) {
                return userIdMap.inverse().get(uid);
            }
        }

        System.out.println("⚠️ Failed to resolve recipient for groupId: " + groupId + " and selfId: " + selfId);
        return null;
    }


    public UUID getOrCreateGroupId(String userA, String userB) {
        List<String> sorted = new ArrayList<>(List.of(userA, userB));
        Collections.sort(sorted);
        String pairKey = sorted.get(0) + "-" + sorted.get(1);

        if (userPairToGroupMap.containsKey(pairKey)) {
            return userPairToGroupMap.get(pairKey);
        }

        UUID groupId = UUID.randomUUID();
        userPairToGroupMap.put(pairKey, groupId);

        UUID idA = userIdMap.get(userA);
        UUID idB = userIdMap.get(userB);
        Set<UUID> members = new HashSet<>();
        members.add(idA);
        members.add(idB); // duplicate won't throw here

        groupIdToUsers.put(groupId, members);

        System.out.println("🔗 Created groupId: " + groupId + " for pair: " + pairKey);
        return groupId;
    }


    public ChatCell findOrCreateChatCell(String fusername) {
        Optional<ChatCell> existing = chatCells.stream() // Avoid nullPointer for debug
                .filter(c -> c.senderProperty().get().equals(fusername))
                .findFirst();
        UUID groupId = getOrCreateGroupId(fusername, getLoggedInUser());
        if (existing.isPresent()) {
            System.out.println("Found existing ChatCell for user " + fusername + "and " + getLoggedInUser() +" with groupId: " + groupId);
            return existing.get();
        } else {
            System.out.println("Created new ChatCell for user " + fusername + "and " + getLoggedInUser() + " with groupId: " + groupId);
            ChatCell cell = new ChatCell(fusername, "", null);
            chatCells.add(cell);
            logUsersInGroup(groupId);
            return cell;
        }
    }

    public ViewFactory getViewFactory() {
        return viewFactory;
    }

    public ObservableList<ChatCell> getChatCells() {
        return chatCells;
    }

    public MessageQueueManager getMessageQueueManager() {
        return messageQueueManager;
    }

    public void logUsersInGroup(UUID groupId) {
        System.out.println("Users in groupId: " + groupId);
        Set<UUID> users = groupIdToUsers.get(groupId);

        if (users != null) {
            for (UUID userId : users) {
                String username = userIdMap.inverse().get(userId);
                System.out.println(" - " + username + ": " + userId);
            }
        }
    }

    public void resetSessionState() {
        chatCells.clear();
        loggedInUser.set(null);
        System.out.println("[Model] Session state reset.");
    }


}
