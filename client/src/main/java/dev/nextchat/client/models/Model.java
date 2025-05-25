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


    // In Model.java
    public ChatCell findOrCreateChatCell(UUID groupId) {
        return chatCellsByGroup.computeIfAbsent(groupId, gid -> {
            String initialDisplayName = "Group " + gid.toString().substring(0, Math.min(gid.toString().length(), 4)); // Fallback
            UUID otherUserIdToFetchNameFor = null; // Only for 1-on-1 if name not in GroupInfo
            boolean isTrueOneOnOne = false;

            GroupInfo groupInfo = groupManager.getGroupInfo(gid);
            UUID currentUser = getLoggedInUserId();

            if (groupInfo != null) {
                // Scenario 1: It's a 2-member group, likely a 1-on-1 chat.
                // We prioritize fetching the other user's name.
                if (groupInfo.getMembers() != null && groupInfo.getMembers().size() == 2 && currentUser != null) {
                    isTrueOneOnOne = true;
                    for (UUID memberId : groupInfo.getMembers()) {
                        if (!memberId.equals(currentUser)) {
                            otherUserIdToFetchNameFor = memberId;
                            initialDisplayName = "Loading..."; // Placeholder for the other user's name
                            break;
                        }
                    }
                }
                // Scenario 2: The group has an explicit name (could be a named 1-on-1 or a multi-user group)
                // If it's not a 1-on-1 identified above, or if the 1-on-1 logic didn't set a name yet,
                // use the group's stored name.
                if (!isTrueOneOnOne && groupInfo.getGroupName() != null && !groupInfo.getGroupName().isEmpty()) {
                    initialDisplayName = groupInfo.getGroupName();
                }
                // If it is a 1-on-1, initialDisplayName is already "Loading..."
                // If it's a multi-member group without a specific name, it will use the fallback.
            } else {
                // No GroupInfo found, use fallback. Could indicate a new group just created
                // for which local GroupInfo isn't fully populated yet from the server.
                System.err.println("[Model.findOrCreateChatCell] No GroupInfo for group " + gid + ". Using default: " + initialDisplayName);
            }

            ChatCell cell = new ChatCell(initialDisplayName, gid);
            Platform.runLater(() -> { if (!chatCells.contains(cell)) chatCells.add(cell); });

            // If it was identified as a 1-on-1 and we need to fetch the name
            if (isTrueOneOnOne && otherUserIdToFetchNameFor != null && initialDisplayName.equals("Loading...")) {
                // As per your request, we are NOT calling RequestFactory.checkIfUserIdExist() here.
                // The name will be filled by incoming messages or history load.
                System.out.println("[Model.findOrCreateChatCell] Group " + gid + " is 1-on-1 with otherUserId " +
                        otherUserIdToFetchNameFor + ". Set to 'Loading...'; name expected from messages/history.");
            }
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

    public void handleIncomingChatMessage(JSONObject response) {
        UUID messageId;
        UUID senderId;
        UUID groupId;
        String content;
        Instant timestamp;
        String responseType;
        String senderUsername = null;

        UUID loggedInUserIdForLog = getLoggedInUserId();
        try {
            responseType = response.getString("type");
            senderId = UUID.fromString(response.getString("senderId"));
            groupId = UUID.fromString(response.getString("groupId"));
            content = response.getString("content");
            timestamp = Instant.parse(response.getString("timestamp"));

            if (response.has("senderUsername")) {
                senderUsername = response.getString("senderUsername");
            }

            if (response.has("messageId")) {
                messageId = UUID.fromString(response.getString("messageId"));
            } else {
                if (responseType.equals("message")) { // Only generate for "message" type if absolutely missing from server
                    messageId = UUID.randomUUID();
                    System.err.println("[Model.handleIncomingChatMessage] CRITICAL: 'messageId' MISSING for type 'message' from server. Generated local ID: " + messageId + " for group " + groupId);
                } else if (responseType.equals("send_message_response")) {
                    System.err.println("[Model.handleIncomingChatMessage] CRITICAL: 'messageId' MISSING for type 'send_message_response'. Cannot process ACK properly.");
                    return; // Cannot proceed without messageId for an ACK
                } else {
                    System.err.println("[Model.handleIncomingChatMessage] 'messageId' MISSING for unknown type '" + responseType + "'.");
                    return;
                }
            }

            Message receivedMessage = new Message(messageId, senderId, senderUsername, groupId, content, timestamp);
            MessageQueueManager.saveMessage(receivedMessage); // Save the message with all details

            boolean isThisClientTheOriginalSender = loggedInUserIdForLog != null && senderId.equals(loggedInUserIdForLog);

            ChatCell relevantChatCell = findOrCreateChatCell(groupId);

            if (!isThisClientTheOriginalSender && senderUsername != null && !senderUsername.isEmpty()) {
                GroupInfo gi = groupManager.getGroupInfo(relevantChatCell.getGroupId());
                if (gi != null && gi.getMembers() != null && gi.getMembers().size() == 2) {
                    boolean otherUserIsSender = false;
                    for(UUID member : gi.getMembers()){
                        if(member.equals(senderId) && !member.equals(loggedInUserIdForLog)){
                            otherUserIsSender = true;
                            break;
                        }
                    }

                    if(otherUserIsSender) {
                        // If cell name is still a placeholder or different, update it
                        if (relevantChatCell.getOtherUsername().startsWith("Loading") ||
                                relevantChatCell.getOtherUsername().startsWith("User ") ||
                                relevantChatCell.getOtherUsername().startsWith("Chat ") ||
                                relevantChatCell.getOtherUsername().startsWith("Group ") ||
                                relevantChatCell.getOtherUsername().startsWith("Unknown Chat") || // Catch other fallbacks
                                !relevantChatCell.getOtherUsername().equals(senderUsername)) {

                            final String finalSenderName = senderUsername;
                            Platform.runLater(() -> relevantChatCell.setOtherUsername(finalSenderName));
                        }
                    }
                }
            }
            Platform.runLater(() -> {
                relevantChatCell.addMessage(receivedMessage);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAllChatHistories() {
        List<Message> allMessages = MessageQueueManager.loadMessages();
        if (allMessages == null || allMessages.isEmpty()) {
            // System.out.println("[Model.loadAllChatHistories] No chat history found.");
            return;
        }
        UUID currentUser = getLoggedInUserId();

        Map<UUID, List<Message>> messagesByGroup = allMessages.stream()
                .filter(m -> m.getGroupId() != null)
                .collect(Collectors.groupingBy(Message::getGroupId));

        messagesByGroup.forEach((groupId, messagesInGroup) -> {
            ChatCell chatCell = findOrCreateChatCell(groupId);
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
                                .filter(m -> m.getSenderId() != null && m.getSenderId().equals(finalOtherUserInHistory) &&
                                        m.getSenderUsername() != null && !m.getSenderUsername().isEmpty())
                                .findFirst()
                                .ifPresent(msgWithOtherUsername -> {
                                    final String nameFromHistory = msgWithOtherUsername.getSenderUsername();
                                    Platform.runLater(() -> {
                                        if (chatCell.getOtherUsername().startsWith("Loading...")) {
                                            chatCell.setOtherUsername(nameFromHistory);
                                        }
                                    });
                                });
                    }
                }
            }
            Platform.runLater(() -> {
                if(chatCell.getMessages().isEmpty()){ // Only add history if cell is currently empty
                    messagesInGroup.sort(Comparator.comparing(Message::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())));
                    for (Message msg : messagesInGroup) {
                        chatCell.addMessage(msg);
                    }
                } else {
                    chatCell.updateLastMessageDetails();
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