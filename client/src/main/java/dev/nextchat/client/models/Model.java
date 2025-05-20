package dev.nextchat.client.models;

import java.util.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import dev.nextchat.client.backend.ConnectionManager;
import dev.nextchat.client.backend.MessageController;
import dev.nextchat.client.controllers.ResponseRouter;
import dev.nextchat.client.database.GroupManager;
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
    private UUID loggedInUserId;
    private final ObservableList<Message> messages = FXCollections.observableArrayList();
    BiMap<String, UUID> userIdMap = HashBiMap.create();
    private final MessageQueueManager messageQueueManager = new MessageQueueManager();
    private final GroupManager groupManager = new GroupManager();
    private ConnectionManager connectionManager;
    private MessageController messageController;
    private ResponseRouter responseRouter;

    private Model() {
        this.viewFactory = new ViewFactory();
        this.chatCells = FXCollections.observableArrayList();
        this.connectionManager = new ConnectionManager();
        this.messageController = new MessageController(connectionManager);
        this.responseRouter = new ResponseRouter();
        groupManager.getAllGroups();
    }

    public static synchronized Model getInstance() {
        if (model == null) {
            model = new Model();
        }
        return model;
    }

    public ResponseRouter getResponseRouter() {
        return responseRouter;
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

    public void setLoggedInUserId(UUID id) {
        this.loggedInUserId = id;
    }

    public Map<UUID, String> getUserIdToUsernameMap() {
        return userIdMap.inverse();
    }

    public boolean userExists(String username) {
        return userIdMap.containsKey(username);
    }

    public String resolveRecipientFromGroup(UUID groupId, UUID selfId) {
        UUID otherId = groupManager.getOtherMember(groupId, selfId);
        return otherId == null ? null : userIdMap.inverse().get(otherId);
    }

    public void renderMessage(Message msg, UUID loggedInUserId) {
        UUID groupId = msg.getGroupId();
        UUID senderId = msg.getSenderId();

        if (!groupManager.isUserInGroup(loggedInUserId, groupId)) return;

        if (!senderId.equals(loggedInUserId)) {
            String senderUsername = userIdMap.inverse().get(senderId);
            ChatCell cell = findOrCreateChatCell(senderUsername);
            if (cell != null) cell.addMessage(msg);
        } else {
            String recipientUsername = resolveRecipientFromGroup(groupId, loggedInUserId);
            if (recipientUsername != null) {
                ChatCell cell = findOrCreateChatCell(recipientUsername);
                if (cell != null) cell.addMessage(msg);
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
