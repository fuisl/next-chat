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
import java.util.*;
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
    private UUID newChat_pendingOtherUserId = null;
    private String newChat_pendingOtherUsername = null;
    private String pendingGroupNameForNewGroup = null;
    private List<UserDisplay> pendingMembersForNewGroup = new ArrayList<>();

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
    public void initiateNewOneOnOneChat(UUID otherUserId, String otherUsername) {
        this.newChat_pendingOtherUserId = otherUserId;
        this.newChat_pendingOtherUsername = otherUsername; // Store the already fetched name

        UUID currentLoggedInUserId = getLoggedInUserId();

        String groupNameForRequest = "Chat: " + getLoggedInUser() + " & " + otherUsername;
        String groupDescription = "Direct chat";

        JSONObject createGroupReq = RequestFactory.createNewGroupRequest(currentLoggedInUserId, groupNameForRequest, groupDescription);

        if (msgCtrl != null) {
            msgCtrl.getSendMessageQueue().offer(createGroupReq);
        }
    }

    private void clearSinglePendingChatContext() {
        this.newChat_pendingOtherUserId = null;
        this.newChat_pendingOtherUsername = null;
    }
    private void clearPendingMultiMemberGroupContext() {
        this.pendingGroupNameForNewGroup = null;
        if (this.pendingMembersForNewGroup != null) {
            this.pendingMembersForNewGroup.clear();
        }
    }

    public void prepareGroupCreationWithMembers(String groupNameIgnored, List<UserDisplay> membersToInvite) {
        clearSinglePendingChatContext();
        // We don't store the groupName here anymore, as it will be derived from groupId.
        this.pendingGroupNameForNewGroup = null; // Explicitly null
        this.pendingMembersForNewGroup = new ArrayList<>(membersToInvite);
        // System.out.println("[Model.prepareGroupCreation] Prepared with " + membersToInvite.size() + " members to invite. Name will be ID-based.");
    }


    public ChatCell findOrCreateChatCell(UUID groupId) {
        return chatCellsByGroup.computeIfAbsent(groupId, gid -> {
            String initialDisplayName = "Loading..."; // Default placeholder
            UUID currentUser = getLoggedInUserId();

            GroupInfo groupInfo = groupManager.getGroupInfo(gid);
            if (groupInfo != null && groupInfo.getMembers() != null) {
                if (groupInfo.getMembers().size() == 2 && currentUser != null) {
                    // It's a 1-on-1 chat, name will be "Loading..." initially
                    // unless set by handleIncomingChatMessage or loadAllChatHistories enhancement.
                } else if (groupInfo.getGroupName() != null && !groupInfo.getGroupName().isEmpty()) {
                    initialDisplayName = groupInfo.getGroupName(); // Use predefined group name
                } else { // Not 1-on-1 or no specific group name
                    initialDisplayName = "Group " + gid.toString().substring(0, 4);
                }
            } else { // No GroupInfo, might be a very new group, or error.
                initialDisplayName = "Unknown Chat " + gid.toString().substring(0, 4);
            }

            ChatCell cell = new ChatCell(initialDisplayName, gid);
            Platform.runLater(() -> { if (!chatCells.contains(cell)) chatCells.add(cell); });
            // NO LONGER SENDS checkIfUserIdExist from here.
            return cell;
        });
    }

    // --- Server Response Handlers ---

    public void handleCreateGroupResponse(JSONObject response) {
        try {
            UUID groupId = UUID.fromString(response.getString("groupId"));
            UUID currentUser = getLoggedInUserId();

            // Determine if this group creation was for a 1-on-1 chat or a multi-member group
            boolean isFromMultiMemberSetupViaPendingList = (this.pendingMembersForNewGroup != null && !this.pendingMembersForNewGroup.isEmpty());
            boolean isFromSingleChatSetupViaOldPendingFields = (this.newChat_pendingOtherUserId != null && this.newChat_pendingOtherUsername != null);

            String finalCellDisplayName;
            List<UUID> membersToActuallyInviteAndRecord = new ArrayList<>();

            if (isFromMultiMemberSetupViaPendingList) {
                // Name will be derived from groupId for multi-member groups created this way
                finalCellDisplayName = "Group " + groupId.toString().substring(0, Math.min(groupId.toString().length(), 4));
                this.pendingMembersForNewGroup.forEach(ud -> membersToActuallyInviteAndRecord.add(ud.userId()));
                groupManager.addGroupMapping(groupId, currentUser, finalCellDisplayName); // Use the ID-based name
            } else if (isFromSingleChatSetupViaOldPendingFields) {
                finalCellDisplayName = this.newChat_pendingOtherUsername;
                membersToActuallyInviteAndRecord.add(this.newChat_pendingOtherUserId);
                String internalGroupName = "DM_" + currentUser.toString().substring(0,Math.min(4,currentUser.toString().length())) + "_" + this.newChat_pendingOtherUserId.toString().substring(0,Math.min(4,this.newChat_pendingOtherUserId.toString().length()));
                groupManager.addGroupMapping(groupId, currentUser, internalGroupName);
            } else {
                finalCellDisplayName = response.optString("groupName", "Chat " + groupId.toString().substring(0,Math.min(4,groupId.toString().length())));
                groupManager.addGroupMapping(groupId, currentUser, finalCellDisplayName);
            }

            groupManager.addMemberToGroup(groupId, currentUser); // Ensure creator is a member
            for (UUID memberIdToInvite : membersToActuallyInviteAndRecord) {
                groupManager.addMemberToGroup(groupId, memberIdToInvite); // Optimistic local add
            }

            ChatCell cell = chatCellsByGroup.computeIfAbsent(groupId, gid -> {
                ChatCell newCell = new ChatCell(finalCellDisplayName, gid);
                Platform.runLater(() -> { if (!chatCells.contains(newCell)) chatCells.add(newCell); });
                return newCell;
            });
            if (!cell.getOtherUsername().equals(finalCellDisplayName)) {
                Platform.runLater(() -> cell.setOtherUsername(finalCellDisplayName));
            }

            for (UUID memberIdToInvite : membersToActuallyInviteAndRecord) {
                if (!memberIdToInvite.equals(currentUser)) {
                    JSONObject joinReq = RequestFactory.createJoinGroupRequest(memberIdToInvite, groupId);
                    if (msgCtrl != null) msgCtrl.getSendMessageQueue().offer(joinReq);
                }
            }

            clearSinglePendingChatContext();
            clearPendingMultiMemberGroupContext(); // Clears pendingMembersForNewGroup

            final UUID finalSelectGroupId = groupId;
            Platform.runLater(() -> {
                getViewFactory().getClientSelectedChat().set(finalSelectGroupId.toString());
                getViewFactory().getClientSelection().set("Chats");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // In Model.java
    public void handleIncomingChatMessage(JSONObject response) {
        UUID messageId = null;
        UUID senderId = null;
        UUID groupId = null;
        String content = null;
        Instant timestamp = null;
        String responseType = null;
        String senderUsername = null;

        UUID loggedInUserIdForLog = getLoggedInUserId();
        String currentUserForLog = (loggedInUserIdForLog != null ? loggedInUserIdForLog.toString().substring(0,Math.min(4, loggedInUserIdForLog.toString().length())) : "LOGGED_OUT_OR_NULL");

        // System.out.println("[" + currentUserForLog + " - handleIncomingChatMessage] Processing: " + response.toString());

        try {
            responseType = response.getString("type");
            senderId = UUID.fromString(response.getString("senderId"));
            groupId = UUID.fromString(response.getString("groupId"));
            content = response.getString("content");
            timestamp = Instant.parse(response.getString("timestamp"));

            if (response.has("senderUsername")) {
                senderUsername = response.getString("senderUsername");
            } else {
                System.err.println("[" + currentUserForLog + " - handleIncomingChatMessage] 'senderUsername' MISSING in " + responseType + " payload for group " + groupId);
                // Name update from this message will not be possible.
            }

            if (response.has("messageId")) {
                messageId = UUID.fromString(response.getString("messageId"));
            } else {
                if (responseType.equals("message")) {
                    messageId = UUID.randomUUID();
                    System.err.println("[" + currentUserForLog + " - handleIncomingChatMessage] 'messageId' MISSING for type 'message'. Generated local ID: " + messageId);
                } else {
                    System.err.println("[" + currentUserForLog + " - handleIncomingChatMessage] 'messageId' MISSING for type '" + responseType + "'. Cannot proceed without messageId for ACK.");
                    return;
                }
            }

            Message receivedMessage = new Message(messageId, senderId, senderUsername, groupId, content, timestamp);
            MessageQueueManager.saveMessage(receivedMessage);

            boolean isThisClientTheOriginalSender = loggedInUserIdForLog != null && senderId.equals(loggedInUserIdForLog);

            ChatCell relevantChatCell = findOrCreateChatCell(groupId); // Creates cell with placeholder if new
            if (relevantChatCell == null) {
                System.err.println("[" + currentUserForLog + " - handleIncomingChatMessage] CRITICAL: relevantChatCell is NULL for groupId: " + groupId);
                return;
            }

            // --- Attempt to update name and GroupInfo based on incoming message ---
            if (!isThisClientTheOriginalSender && senderId != null && senderUsername != null && !senderUsername.isEmpty()) {
                // This is Duong (receiver) getting a message from An (sender)
                GroupInfo gi = groupManager.getGroupInfo(groupId);

                // Check if GroupManager already knows this is a 2-member group with the sender
                boolean needsGroupInfoUpdate = true;
                if (gi != null && gi.getMembers() != null && gi.getMembers().size() == 2 &&
                        gi.getMembers().contains(senderId) && gi.getMembers().contains(loggedInUserIdForLog)) {
                    needsGroupInfoUpdate = false; // GroupManager is already up-to-date for this 1-on-1
                }

                if (needsGroupInfoUpdate && loggedInUserIdForLog != null) {
                    // GroupManager doesn't have full info, or it's not a 2-member group yet.
                    // Let's optimistically assume this is a 1-on-1 chat with the sender and update GroupManager.
                    System.out.println("[" + currentUserForLog + " - handleIncomingChatMessage] GroupInfo for " + groupId +
                            " is missing or not 2 members. Optimistically updating GroupManager with sender " +
                            senderId + " and self " + loggedInUserIdForLog);

                    // Ensure the group mapping exists with a default name if needed
                    if (gi == null) {
                        groupManager.addGroupMapping(groupId, loggedInUserIdForLog, "Chat with " + senderUsername); // Add current user
                    }
                    // Ensure both sender and current user (receiver) are members
                    groupManager.addMemberToGroup(groupId, senderId);
                    groupManager.addMemberToGroup(groupId, loggedInUserIdForLog); // Ensure self is also there

                    // Re-fetch GroupInfo after update for the name setting logic below
                    gi = groupManager.getGroupInfo(groupId);
                }

                // Now, attempt to set the ChatCell name using senderUsername
                if (gi != null && gi.getMembers() != null && gi.getMembers().size() == 2) {
                    // Check if the current cell name is a placeholder or different
                    if (relevantChatCell.getOtherUsername().startsWith("Loading") ||
                            relevantChatCell.getOtherUsername().startsWith("User ") ||
                            relevantChatCell.getOtherUsername().startsWith("Chat ") || // Catch generic placeholders
                            relevantChatCell.getOtherUsername().startsWith("Group ") ||
                            relevantChatCell.getOtherUsername().startsWith("Unknown Chat") || // Catch other fallbacks
                            !relevantChatCell.getOtherUsername().equals(senderUsername)) {

                        final String finalSenderName = senderUsername; // This should be "An"
                        Platform.runLater(() -> {
                            relevantChatCell.setOtherUsername(finalSenderName);
                        });
                    }
                }
            }

            // Add the message to the ChatCell's list for UI display
            Platform.runLater(() -> {
                relevantChatCell.addMessage(receivedMessage);
            });

        } catch (Exception e) {
            System.err.println("[" + currentUserForLog + " - handleIncomingChatMessage] CRITICAL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Data Persistence ---
    private void loadAllChatHistories() {
        List<Message> allMessages = MessageQueueManager.loadMessages();
        if (allMessages == null || allMessages.isEmpty()) return;
        UUID currentUser = getLoggedInUserId();
        if (currentUser == null) return;

        Map<UUID, List<Message>> messagesByGroup = allMessages.stream()
                .filter(m -> m.getGroupId() != null)
                .collect(Collectors.groupingBy(Message::getGroupId));

        messagesByGroup.forEach((groupId, messagesInGroup) -> {
            // findOrCreateChatCell will set "Loading..." if it's a 1-on-1 and name isn't known
            ChatCell chatCell = findOrCreateChatCell(groupId);

            // Try to set name from historical messages if cell is still "Loading..."
            if (chatCell.getOtherUsername().startsWith("Loading...")) {
                GroupInfo groupInfo = groupManager.getGroupInfo(groupId);
                if (groupInfo != null && groupInfo.getMembers() != null && groupInfo.getMembers().size() == 2) {
                    UUID otherUserInHistory = null;
                    for (UUID member : groupInfo.getMembers()) {
                        if (!member.equals(currentUser)) {
                            otherUserInHistory = member;
                            break;
                        }
                    }
                    if (otherUserInHistory != null) {
                        final UUID finalOtherUserInHistory = otherUserInHistory;
                        messagesInGroup.stream()
                                .filter(m -> m.getSenderId().equals(finalOtherUserInHistory) && m.getSenderUsername() != null && !m.getSenderUsername().isEmpty())
                                .findFirst() // Get username from the first available message from the other user in history
                                .ifPresent(msgWithOtherUsername -> {
                                    final String nameFromHistory = msgWithOtherUsername.getSenderUsername();
                                    Platform.runLater(() -> {
                                        // Only update if still loading, to respect any later, more definitive name
                                        if (chatCell.getOtherUsername().startsWith("Loading...")) {
                                            chatCell.setOtherUsername(nameFromHistory);
                                        }
                                    });
                                });
                    }
                }
            }

            // Add messages to the cell
            Platform.runLater(() -> {
                if(chatCell.getMessages().isEmpty()){
                    messagesInGroup.sort(Comparator.comparing(Message::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())));
                    for (Message msg : messagesInGroup) {
                        chatCell.addMessage(msg);
                    }
                } else {
                    chatCell.updateLastMessageDetails(); // Ensure last message details are up to date
                }
            });
        });
    }

    public record UserDisplay(UUID userId, String username) {}

    public void resetSessionState() {
        Platform.runLater(() -> chatCells.clear());
        chatCellsByGroup.clear();
        loggedInUser.set(null);
        loggedInUserId = null;
        System.out.println("[Model] Session state reset.");
    }
}