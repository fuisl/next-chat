package dev.nextchat.client.models;

import java.io.IOException;
import java.util.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import dev.nextchat.client.backend.ConnectionManager;
import dev.nextchat.client.backend.MessageController;
import dev.nextchat.client.database.GroupManager;
import dev.nextchat.client.database.UserDatabase;
import dev.nextchat.client.views.ViewFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import dev.nextchat.client.database.MessageQueueManager;
import java.util.stream.Collectors;

public class Model {
    private  static Model model;
    private final ViewFactory viewFactory;
    private final ObservableList<ChatCell> chatCells;
    private final StringProperty loggedInUser = new SimpleStringProperty();
    private final ObservableList<Message> messages = FXCollections.observableArrayList();
    BiMap<String, UUID> userIdMap = HashBiMap.create();
    private final MessageQueueManager messageQueueManager = new MessageQueueManager();
    private final GroupManager groupManager = new GroupManager();
    private ConnectionManager connectionManager;
    private MessageController messageController;

    private Model() {
        this.viewFactory = new ViewFactory();
        this.chatCells = FXCollections.observableArrayList();
        this.connectionManager = new ConnectionManager();
        this.messageController = new MessageController(connectionManager);
        groupManager.getAllGroups();
    }

    public static synchronized Model getInstance() {
        if (model == null) {
            model = new Model();
        }
        return model;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void setMessageController(MessageController messageController) {
        this.messageController = messageController;
    }

    public MessageController getMessageController() {
        return messageController;
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

    public String resolveRecipientFromGroup(UUID groupId, UUID selfId) {
        UUID otherId = groupManager.getOtherMember(groupId, selfId);
        return otherId == null ? null : userIdMap.inverse().get(otherId);
    }

    public void loadMessagesForUser(UUID loggedInUserId) {
        List<Message> allMessages = messageQueueManager.loadMessages();
        System.out.println("🔍 Loading messages for: " + userIdMap.inverse().get(loggedInUserId));

        Set<UUID> groupIds = allMessages.stream()
                .map(Message::getGroupId)
                .collect(Collectors.toSet());
        System.out.println("GroupIds: " + groupIds);
        for (Message msg : allMessages) {
            UUID groupId = msg.getGroupId();
            UUID senderId = msg.getSenderId();

            System.out.println("Checking message:");
            System.out.println("  - groupId: " + msg.getGroupId());
            System.out.println("  - senderId: " + msg.getSenderId());
            System.out.println("  - userInGroup? " + groupManager.isUserInGroup(loggedInUserId, groupId));

            if (!groupManager.isUserInGroup(loggedInUserId, groupId)) continue;

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

    public UUID getOrCreateGroupId(String userAName, String userBName) {
        UUID userAId = userIdMap.get(userAName);
        UUID userBId = userIdMap.get(userBName);

        if (userAId == null) {
            throw new IllegalArgumentException("Unknown user: " + userAName);
        }
        if (userBId == null) {
            throw new IllegalArgumentException("Unknown user: " + userBName);
        }

        // Delegate to GroupManager’s UUID-based method
        return groupManager.getOrCreateGroupId(userAId, userBId);
    }

    public ChatCell findOrCreateChatCell(String otherUsername) {
        // 1) Resolve UUIDs
        UUID me      = getLoggedInUserId();
        UUID otherId = userIdMap.get(otherUsername);
        if (otherId == null) {
            throw new IllegalArgumentException("Unknown user: " + otherUsername);
        }

        // 2) Ask GroupManager for a groupId for this pair (creates if missing)
        UUID groupId = groupManager.getOrCreateGroupId(me, otherId);

        // 3) Try to find an existing ChatCell for that conversation
        return chatCells.stream()
                .filter(c -> c.getGroupId().equals(groupId))
                .findFirst()
                // 4) Or create one if none exists
                .orElseGet(() -> {
                    ChatCell cell = new ChatCell(otherUsername, groupId);
                    chatCells.add(cell);
                    return cell;
                });
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

    public void resetSessionState() {
        chatCells.clear();
        loggedInUser.set(null);
        System.out.println("[Model] Session state reset.");
    }

}
