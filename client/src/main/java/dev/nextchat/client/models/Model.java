package dev.nextchat.client.models;

import java.util.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import dev.nextchat.client.backend.ConnectionManager;
import dev.nextchat.client.backend.MessageController;
import dev.nextchat.client.backend.utils.RequestFactory;
import dev.nextchat.client.controllers.ResponseRouter;
import dev.nextchat.client.database.GroupManager;
import dev.nextchat.client.views.ViewFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import dev.nextchat.client.database.MessageQueueManager;
import org.json.JSONObject;

public class Model {
    private  static Model model;
    private final ViewFactory viewFactory;
    private final ObservableList<ChatCell> chatCells;
    private final StringProperty loggedInUser = new SimpleStringProperty();
    private UUID loggedInUserId;
    private final ObservableList<Message> messages = FXCollections.observableArrayList();
    BiMap<String, UUID> userIdMap = HashBiMap.create();
    private final GroupManager groupManager = new GroupManager();
    private ConnectionManager connectionManager;
    private MessageController msgCtrl;
    private ResponseRouter responseRouter;

    private Model() {
        this.viewFactory = new ViewFactory();
        this.chatCells = FXCollections.observableArrayList();
        this.connectionManager = new ConnectionManager();
        this.msgCtrl = new MessageController(connectionManager);
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

    public void setMsgCtrl(MessageController msgCtrl) {
        this.msgCtrl = msgCtrl;
    }

    public MessageController getMsgCtrl() {
        return msgCtrl;
    }

    public StringProperty loggedInUserProperty() {
        return loggedInUser;
    }

    public String getLoggedInUser() {
        return loggedInUser.get();
    }

    public UUID getLoggedInUserId() {
        return loggedInUserId;
    }

    public void setLoggedInUser(String username) {
        this.loggedInUser.set(username);
    }

    public void setLoggedInUserId(UUID id) {
        this.loggedInUserId = id;
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

    public void addKnownUser(String username, UUID id) {
        userIdMap.put(username, id);
    }

    public ChatCell findOrCreateChatCell(String otherUsername) {
        UUID me      = getLoggedInUserId();
        UUID otherId = getUserId(otherUsername);
        if (otherId == null) {
            throw new IllegalStateException(
                    "User ID for '" + otherUsername + "' unknown; validate existence first.");
        }

        // 0) Reuse fully-established chats
        for (ChatCell cell : chatCells) {
            if (otherUsername.equals(cell.getOtherUsername())
                    && cell.getGroupId() != null) {
                return cell;
            }
        }

        // 1) Check persisted group mapping
        UUID storedGroupId = groupManager.getExistingGroupId(me, otherId);
        if (storedGroupId != null) {
            ChatCell cell = new ChatCell(otherUsername, storedGroupId);
            chatCells.add(cell);
            return cell;
        }

        // 2) Reuse pending creation (no groupId yet)
        for (ChatCell cell : chatCells) {
            if (otherUsername.equals(cell.getOtherUsername())
                    && cell.getGroupId() == null) {
                return cell;
            }
        }

        // 3) No existing or pending chat: create and ask server
        ChatCell cell = new ChatCell(otherUsername, null);
        chatCells.add(cell);

        JSONObject createReq = RequestFactory.createNewGroupRequest(me, otherUsername, "");
        msgCtrl.getSendMessageQueue().offer(createReq);

        return cell;
    }

    public void handleCreateGroupResponse(JSONObject response) {
        UUID   groupId       = UUID.fromString(response.getString("groupId"));
        String otherUsername = response.getString("username");  // make sure your server is returning this!
        UUID   me            = getLoggedInUserId();
        UUID   otherId       = getUserId(otherUsername);

        // 1) Persist server‐returned groupId
        groupManager.addGroupMapping(groupId, me, otherId);
        System.out.println("[Model] Persisted groupId " + groupId + " for users " + me + " & " + otherId);

        // 2) Update the ChatCell
        ChatCell cell = findChatCellByUsername(otherUsername);
        cell.setGroupId(groupId);
        System.out.println("[Model] Attached groupId to ChatCell for " + otherUsername);

        // 3) Build and send the join‐group request
        JSONObject joinReq = RequestFactory.createJoinGroupRequest(otherId, groupId);
        System.out.println("[Model] Sending joinGroupRequest: " + joinReq);
        msgCtrl.getSendMessageQueue().offer(joinReq);
    }


    public UUID getUserId(String username) {
        return userIdMap.get(username);
    }

    private ChatCell findChatCellByUsername(String username) {
        return chatCells.stream()
                .filter(c -> username.equals(c.getOtherUsername()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("ChatCell not found for " + username));
    }

    public void persistGroupMapping(UUID userA, UUID userB, UUID groupId) {
        // delegate to the GroupManager’s JSON store
        groupManager.addGroupMapping(groupId, userA, userB);
    }

    public ViewFactory getViewFactory() {
        return viewFactory;
    }

    public ObservableList<ChatCell> getChatCells() {
        return chatCells;
    }


    public void resetSessionState() {
        chatCells.clear();
        loggedInUser.set(null);
        System.out.println("[Model] Session state reset.");
    }
}
