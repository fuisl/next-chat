package dev.nextchat.client.models;

import dev.nextchat.client.backend.ConnectionManager;
import dev.nextchat.client.backend.MessageController;
import dev.nextchat.client.backend.utils.RequestFactory;
import dev.nextchat.client.controllers.ResponseRouter;
import dev.nextchat.client.database.GroupInfo;
import dev.nextchat.client.database.GroupManager;
import dev.nextchat.client.database.MessageQueueManager;
import dev.nextchat.client.views.ViewFactory;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.JSONObject;

import javafx.util.Callback;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Model {
    private static Model model;
    private final ViewFactory viewFactory;
    private final ObservableList<ChatCell> chatCells;
    private final StringProperty loggedInUser = new SimpleStringProperty();
    private UUID loggedInUserId;

    private final GroupManager groupManager;
    private final Map<UUID, ChatCell> chatCellsByGroup = new HashMap<>();
    private UUID otherUserId = null;
    private ConnectionManager connectionManager;
    private MessageController msgCtrl;
    private ResponseRouter responseRouter;

    private Model() {
        this.viewFactory = new ViewFactory();
        this.groupManager = new GroupManager();
        this.responseRouter = new ResponseRouter();
        this.connectionManager = new ConnectionManager();
        Callback<ChatCell, Observable[]> extractor = cell -> new Observable[]{
                cell.timestampProperty(),
                cell.otherUsernameProperty()
        };
        this.chatCells = FXCollections.observableArrayList(extractor);
    }

    public static synchronized Model getInstance() {
        if (model == null) {
            model = new Model();
        }
        return model;
    }

    // --- Getters and Setters for Core Components ---
    public ViewFactory getViewFactory() {
        return viewFactory;
    }
    public void initializeUIData() {
        loadAllChatHistories();
    }

    public ObservableList<ChatCell> getChatCells() {
        return chatCells;
    }

    public StringProperty loggedInUserProperty() {
        return loggedInUser;
    }

    public String getLoggedInUser() {
        return loggedInUser.get();
    }

    public void setLoggedInUser(String username) {
        this.loggedInUser.set(username);
    }

    public UUID getLoggedInUserId() {
        return loggedInUserId;
    }

    public void setLoggedInUserId(UUID id) {
        this.loggedInUserId = id;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public MessageController getMsgCtrl() {
        return msgCtrl;
    }

    public void setMsgCtrl(MessageController msgCtrl) {
        this.msgCtrl = msgCtrl;
    }

    public ResponseRouter getResponseRouter() {
        return responseRouter;
    }

    public void setResponseRouter(ResponseRouter responseRouter) {
        this.responseRouter = responseRouter;
    }

    // --- User Management (Client-side mapping removed) ---
    // Methods like addKnownUser, getUserId(username), getUsername(UUID) are removed.

    // --- Chat and Group Management ---

    public ChatCell findOrCreateChatCell(UUID groupId) {
        return chatCellsByGroup.computeIfAbsent(groupId, gid -> {
            // Determine initial chat name. This will be updated if a username is fetched.
            String initialChatName = "Group: " + gid.toString().substring(0, 8); // Default name

            GroupInfo groupInfo = groupManager.getGroupInfo(gid);
            UUID otherUserIdForRequest = null;

            if (groupInfo != null && groupInfo.getMembers() != null) {
                if (groupInfo.getGroupName() != null && !groupInfo.getGroupName().isEmpty()) {
                    initialChatName = groupInfo.getGroupName();
                } else if (groupInfo.getMembers().size() == 2) { // Likely a 1-on-1 chat
                    for (UUID memberId : groupInfo.getMembers()) {
                        if (!memberId.equals(getLoggedInUserId())) {
                            otherUserIdForRequest = memberId;
                            initialChatName = "User: " + memberId.toString().substring(0, 8); // Placeholder
                            break;
                        }
                    }
                }
            }

            ChatCell cell = new ChatCell(initialChatName, gid);
            final String finalInitialChatName = initialChatName;

            Platform.runLater(() -> {
                boolean exists = chatCells.stream().anyMatch(c -> c.getGroupId().equals(gid));
                if (!exists) {
                    chatCells.add(cell);
                } else {
                    chatCells.stream().filter(c -> c.getGroupId().equals(gid)).findFirst().ifPresent(existingCell -> {
                        if (!existingCell.getOtherUsername().equals(finalInitialChatName) && finalInitialChatName.startsWith("User:")) {
                            // Only update if current is different and new one is a placeholder (or specific logic)
                            // existingCell.setOtherUsername(finalInitialChatName); // This might overwrite a fetched name if not careful
                        }
                    });
                }
            });

            // If we identified an other user in a 1-on-1, request their username
            if (otherUserIdForRequest != null) {
                requestUsername(otherUserIdForRequest);
            }
            return cell;
        });
    }

    public void requestUsername(UUID userIdToLookup) {
        if (userIdToLookup == null || msgCtrl == null) {
            return;
        }
        // Optional: Check if a chat cell already has a non-placeholder name for this user
        // to avoid redundant requests. This requires more complex tracking.
        JSONObject req = RequestFactory.requestUsername(userIdToLookup);
        msgCtrl.getSendMessageQueue().offer(req);
    }

    public void setPendingInviteForNextGroup(UUID userId) {
        this.otherUserId = userId;
    }

    // --- Server Response Handlers ---

    public void handleCreateGroupResponse(JSONObject response) {
        try {
            UUID groupId = UUID.fromString(response.getString("groupId"));
            String groupName = response.optString("groupName", "Chat " + groupId.toString().substring(0,4));
            UUID currentUser = getLoggedInUserId();

            // This still adds the group with initially only the current user as a member
            groupManager.addGroupMapping(groupId, currentUser, groupName);
            System.out.println("[Model] Group created/known: " + groupId + " with name: " + groupName);

            ChatCell cell = findOrCreateChatCell(groupId);
            final String finalGroupNameForCell = groupName;
            Platform.runLater(() -> {
                cell.setOtherUsername(finalGroupNameForCell);
            });

            if (this.otherUserId != null) {
                UUID userToInvite = this.otherUserId; // This is User B
                this.otherUserId = null;

                boolean otherUserIsAlreadyMember = false;

                GroupInfo currentGroupInfo = groupManager.getGroupInfo(groupId);
                if (currentGroupInfo != null && currentGroupInfo.getMembers() != null && currentGroupInfo.getMembers().contains(userToInvite)) {
                    otherUserIsAlreadyMember = true;
                }

                if (!otherUserIsAlreadyMember && !userToInvite.equals(currentUser)) {
                    JSONObject joinReq = RequestFactory.createJoinGroupRequest(userToInvite, groupId);
                    if (msgCtrl != null) {
                        msgCtrl.getSendMessageQueue().offer(joinReq);

                        groupManager.addMemberToGroup(groupId, userToInvite);
                    }
                }
            }

            final UUID finalSelectGroupId = groupId;
            Platform.runLater(() -> {
                getViewFactory().getClientSelectedChat().set(finalSelectGroupId.toString());
                getViewFactory().getClientSelection().set("Chats");
            });

        } catch (Exception e) {
            System.err.println("[Model] Error in handleCreateGroupResponse: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void handleUsernameResponse(JSONObject response) {
        try {
            UUID userId = UUID.fromString(response.getString("userId"));
            String username = response.getString("username");

            System.out.println("[Model] Received username '" + username + "' for userId '" + userId + "'");

            // Update any ChatCell that might be displaying a placeholder for this user.
            // This is primarily for 1-on-1 chats.
            for (ChatCell cell : chatCells) {
                GroupInfo groupInfo = groupManager.getGroupInfo(cell.getGroupId());
                if (groupInfo != null && groupInfo.getMembers() != null && groupInfo.getMembers().size() == 2) {
                    boolean userInGroup = false;
                    boolean loggedInUserInGroup = false;
                    for(UUID memberId : groupInfo.getMembers()){
                        if(memberId.equals(userId)) userInGroup = true;
                        if(memberId.equals(getLoggedInUserId())) loggedInUserInGroup = true;
                    }

                    if (userInGroup && loggedInUserInGroup) {
                        // This cell represents a 1-on-1 chat with the user whose name we just received.
                        final String finalUsername = username;
                        Platform.runLater(() -> {
                            cell.setOtherUsername(finalUsername); // Update ChatCell's display name
                            System.out.println("[Model] Updated ChatCell for group " + cell.getGroupId() + " to display user: " + finalUsername);
                        });
                        // Assuming one ChatCell per 1-on-1, we can break or continue if multiple groups could involve this user.
                        // For simplicity, updating all relevant 1-on-1 chat cells.
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Model] Error handling username response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleIncomingChatMessage(JSONObject response) {
        UUID messageId;
        UUID senderId;
        UUID groupId;
        String content;
        Instant timestamp;
        String responseType;

        try {
            responseType = response.getString("type");

            senderId = UUID.fromString(response.getString("senderId"));
            groupId = UUID.fromString(response.getString("groupId"));
            content = response.getString("content");
            timestamp = Instant.parse(response.getString("timestamp"));

            if (responseType.equals("send_message_response")) {
                messageId = UUID.fromString(response.getString("messageId"));
            } else if (responseType.equals("message")) {
                if (response.has("messageId")) {
                    messageId = UUID.fromString(response.getString("messageId"));
                } else {
                    messageId = UUID.randomUUID(); // Fallback if missing - NOT IDEAL
                }
            } else {
                // This function should only be called by ResponseRouter for these specific types.
                // If it's called with another type, it's a routing logic error.
                return;
            }

            Message canonicalOrReceivedMessage = new Message(messageId, senderId, groupId, content, timestamp);
            MessageQueueManager.saveMessage(canonicalOrReceivedMessage);

            boolean isThisClientTheOriginalSender = senderId.equals(getLoggedInUserId());

            ChatCell relevantChatCell = findOrCreateChatCell(groupId);
            if (relevantChatCell == null) {
                return;
            }

            Platform.runLater(() -> {
                if (isThisClientTheOriginalSender && responseType.equals("send_message_response")) {
                    relevantChatCell.updateLastMessageDetails();
                } else if (!isThisClientTheOriginalSender && responseType.equals("message")) {
                    // RECEIVER received a "message"
                    relevantChatCell.addMessage(canonicalOrReceivedMessage);
                } else {
                    // Fallback or unexpected combination, potentially add to cell if makes sense
                    // This might be hit if sender gets "message" or receiver gets "send_message_response"
                    // and is NOT the original sender.
                    // Based on your rules, this block implies a server-side type mixup if reached.
                    // However, to be safe and ensure any message routed here attempts UI update:
                    if(!isThisClientTheOriginalSender){ // Only add to UI if not self and not already handled by optimistic.
                        relevantChatCell.addMessage(canonicalOrReceivedMessage);
                    } else if (isThisClientTheOriginalSender && responseType.equals("message")){

                        relevantChatCell.updateLastMessageDetails();
                    }
                }
            });

        } catch (Exception e) {
            // Consider re-enabling printStackTrace during active debugging if issues persist
            // e.printStackTrace();
        }

    }

    // --- Data Persistence ---
    private void loadAllChatHistories() {
        List<Message> allMessages = MessageQueueManager.loadMessages();
        if (allMessages == null || allMessages.isEmpty()) {
            return;
        }
        Map<UUID, List<Message>> messagesByGroup = allMessages.stream()
                .filter(m -> m.getGroupId() != null)
                .collect(Collectors.groupingBy(Message::getGroupId));

        messagesByGroup.forEach((groupId, messagesInGroup) -> {
            if (groupId == null) return; // Should not happen if filtered
            ChatCell chatCell = findOrCreateChatCell(groupId);
            messagesInGroup.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));


            if (chatCell.getOtherUsername().startsWith("User:")) {
                GroupInfo groupInfo = groupManager.getGroupInfo(groupId);
                if (groupInfo != null && groupInfo.getMembers() != null && groupInfo.getMembers().size() == 2) {
                    groupInfo.getMembers().stream()
                            .filter(memberId -> !memberId.equals(getLoggedInUserId()))
                            .findFirst()
                            .ifPresent(this::requestUsername);
                }
            }
            Platform.runLater(() -> {
                chatCell.getMessages().clear();
                for (Message msg : messagesInGroup) {
                    chatCell.addMessage(msg);
                }
            });
        });
    }

    public void resetSessionState() {
        Platform.runLater(() -> chatCells.clear());
        chatCellsByGroup.clear();
        loggedInUser.set(null);
        loggedInUserId = null;
        System.out.println("[Model] Session state reset.");
    }
}