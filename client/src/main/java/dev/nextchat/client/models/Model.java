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
    private final Set<UUID> pendingFetches = Collections.synchronizedSet(new HashSet<>());
    private final Set<UUID> pendingGroupInfoFetches = Collections.synchronizedSet(new HashSet<>());
    private ConnectionManager connMgr;
    private MessageController msgCtrl;
    private ResponseRouter respRouter;
    private UUID pendingOtherId = null;
    private String pendingOtherName = null;
    private String pendingGroupName = null;
    private List<UserDisplay> pendingMembers = new ArrayList<>();

    private Model() {
        this.viewFactory = new ViewFactory();
        this.groupManager = new GroupManager();
        this.respRouter = new ResponseRouter();
        this.connMgr = new ConnectionManager();
        Callback<ChatCell, Observable[]> extractor = cell -> new Observable[]{
                cell.timestampProperty(),
                cell.otherUsernameProperty()
        };
        this.chatCells = FXCollections.observableArrayList(extractor);
    }

    public static synchronized Model getInstance() {
        if (model == null) model = new Model();
        return model;
    }

    public ViewFactory getViewFactory() { return viewFactory; }
    public void initializeUIData() { loadAllChatHistories(); }
    public ObservableList<ChatCell> getChatCells() { return chatCells; }
    public StringProperty loggedInUserProperty() { return loggedInUser; }
    public String getLoggedInUser() { return loggedInUser.get(); }
    public void setLoggedInUser(String username) { this.loggedInUser.set(username); }
    public UUID getLoggedInUserId() { return loggedInUserId; }
    public void setLoggedInUserId(UUID id) { this.loggedInUserId = id; }
    public ConnectionManager getConnectionManager() { return connMgr; }
    public void setConnectionManager(ConnectionManager c) { this.connMgr = c; }
    public MessageController getMsgCtrl() { return msgCtrl; }
    public void setMsgCtrl(MessageController c) { this.msgCtrl = c; }
    public ResponseRouter getResponseRouter() { return respRouter; }
    public void setResponseRouter(ResponseRouter r) { this.respRouter = r; }

    public void initiateNewOneOnOneChat(UUID otherId, String otherName) {
        this.pendingOtherId = otherId;
        this.pendingOtherName = otherName;
        UUID userId = getLoggedInUserId();
        String groupName = "Chat: " + getLoggedInUser() + " & " + otherName;
        JSONObject req = RequestFactory.createNewGroupRequest(userId, groupName, "Direct chat");
        if (msgCtrl != null) msgCtrl.getSendMessageQueue().offer(req);
    }

    private void clearSinglePendingChatContext() {
        this.pendingOtherId = null;
        this.pendingOtherName = null;
    }
    private void clearPendingMultiGroupContext() {
        this.pendingGroupName = null;
        if (this.pendingMembers != null) this.pendingMembers.clear();
    }

    public void prepareGroupCreationWithMembers(String groupNameIgnored, List<UserDisplay> members) {
        clearSinglePendingChatContext();
        this.pendingGroupName = null;
        this.pendingMembers = new ArrayList<>(members);
    }

    public ChatCell findOrCreateChatCell(UUID groupId) {
        return chatCellsByGroup.computeIfAbsent(groupId, gid -> {
            String name;
            GroupInfo group = groupManager.getGroupInfo(gid);
            UUID user = getLoggedInUserId();

            if (group != null) {
                if (group.getMembers() != null && group.getMembers().size() == 2 && user != null) {
                    boolean found = false;
                    for (UUID memberId : group.getMembers()) {
                        if (!memberId.equals(user)) { found = true; break; }
                    }
                    if (found) name = "Loading...";
                    else name = group.getGroupName() != null && !group.getGroupName().isEmpty() ?
                            group.getGroupName() :
                            "Group " + gid.toString().substring(0, 4);
                } else if (group.getGroupName() != null && !group.getGroupName().isEmpty()) {
                    name = group.getGroupName();
                } else {
                    name = "Group " + gid.toString().substring(0, 4);
                }
            } else {
                name = "Loading Group Info...";
            }

            ChatCell cell = new ChatCell(name, gid);
            Platform.runLater(() -> { if (!chatCells.contains(cell)) chatCells.add(cell); });
            return cell;
        });
    }

    public void handleCreateGroupResponse(JSONObject response) {
        try {
            UUID groupId = UUID.fromString(response.getString("groupId"));
            UUID user = getLoggedInUserId();

            boolean isMulti = (this.pendingMembers != null && !this.pendingMembers.isEmpty());
            boolean isSingle = (this.pendingOtherId != null && this.pendingOtherName != null);

            String cellName, mapName;
            List<UUID> inviteList = new ArrayList<>();

            if (isMulti) {
                String srvName = response.optString("groupName", null);
                if (srvName != null && !srvName.isEmpty()) {
                    cellName = srvName; mapName = srvName;
                } else {
                    cellName = "Group " + groupId.toString().substring(0, 4);
                    mapName = cellName;
                }
                this.pendingMembers.forEach(ud -> inviteList.add(ud.userId()));
            } else if (isSingle) {
                cellName = this.pendingOtherName;
                inviteList.add(this.pendingOtherId);
                mapName = "DM_" + user.toString().substring(0, 4)
                        + "_" + this.pendingOtherId.toString().substring(0, 4);
            } else {
                String srvName = response.optString("groupName", null);
                if (srvName != null && !srvName.isEmpty()) {
                    cellName = srvName;
                } else {
                    cellName = "Unnamed Group " + groupId.toString().substring(0, 4);
                }
                mapName = cellName;
            }

            groupManager.addGroupMapping(groupId, user, mapName);
            groupManager.addMemberToGroup(groupId, user);
            for (UUID id : inviteList) groupManager.addMemberToGroup(groupId, id);

            ChatCell cell = chatCellsByGroup.computeIfAbsent(groupId, gid -> {
                ChatCell c = new ChatCell(cellName, gid);
                Platform.runLater(() -> { if (!chatCells.contains(c)) chatCells.add(c); });
                return c;
            });
            if (!cell.getOtherUsername().equals(cellName)) {
                Platform.runLater(() -> cell.setOtherUsername(cellName));
            }
            for (UUID id : inviteList) {
                if (!id.equals(user)) {
                    JSONObject joinReq = RequestFactory.createJoinGroupRequest(id, groupId);
                    if (msgCtrl != null) msgCtrl.getSendMessageQueue().offer(joinReq);
                }
            }
            clearSinglePendingChatContext();
            clearPendingMultiGroupContext();
            final UUID selectId = groupId;
            Platform.runLater(() -> {
                getViewFactory().getClientSelectedChat().set(selectId.toString());
                getViewFactory().getClientSelection().set("Chats");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleIncomingChatMessage(JSONObject response) {
        UUID messageId, senderId, groupId;
        String content, responseType, senderName = null;
        Instant timestamp;
        UUID currId = getLoggedInUserId();

        try {
            responseType = response.getString("type");
            senderId = UUID.fromString(response.getString("senderId"));
            groupId = UUID.fromString(response.getString("groupId"));
            content = response.getString("content");
            timestamp = Instant.parse(response.getString("timestamp"));
            if (response.has("senderUsername")) senderName = response.getString("senderUsername");
            if (response.has("messageId")) messageId = UUID.fromString(response.getString("messageId"));
            else {
                if (responseType.equals("message")) messageId = UUID.randomUUID();
                else return;
            }
            Message msg = new Message(messageId, senderId, senderName, groupId, content, timestamp);
            MessageQueueManager.saveMessage(msg);
            ChatCell cell = findOrCreateChatCell(groupId);
            Platform.runLater(() -> cell.addMessage(msg));
            boolean sentByMe = currId != null && senderId.equals(currId);
            if (!sentByMe && senderName != null && !senderName.isEmpty()) {
                GroupInfo gi = groupManager.getGroupInfo(groupId);
                if (gi != null && gi.getMembers() != null && gi.getMembers().size() == 2) {
                    boolean otherSender = false;
                    for (UUID member : gi.getMembers()) {
                        if (member.equals(senderId) && !member.equals(currId)) { otherSender = true; break; }
                    }
                    if (otherSender) {
                        if (cell.getOtherUsername().startsWith("Loading") ||
                                cell.getOtherUsername().startsWith("User ") ||
                                cell.getOtherUsername().startsWith("Chat ") ||
                                cell.getOtherUsername().startsWith("Group ") ||
                                cell.getOtherUsername().startsWith("Unknown Chat") ||
                                !cell.getOtherUsername().equals(senderName)) {
                            final String finalName = senderName;
                            Platform.runLater(() -> cell.setOtherUsername(finalName));
                        }
                    }
                } else if (gi == null) {
                    if (!pendingFetches.contains(groupId)) {
                        pendingFetches.add(groupId);
                        JSONObject fetchReq = RequestFactory.createFetchGroupInfoRequest(groupId);
                        if (msgCtrl != null && fetchReq != null) {
                            msgCtrl.getSendMessageQueue().offer(fetchReq);
                        } else if (fetchReq == null) {
                            pendingFetches.remove(groupId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAllChatHistories() {
        List<Message> allMsgs = MessageQueueManager.loadMessages();
        if (allMsgs == null || allMsgs.isEmpty()) return;
        UUID currId = getLoggedInUserId();
        String currName = getLoggedInUser();
        if (currId == null || currName == null || currName.isEmpty()) {
            Map<UUID, List<Message>> map = allMsgs.stream()
                    .filter(m -> m.getGroupId() != null)
                    .collect(Collectors.groupingBy(Message::getGroupId));
            map.forEach((gid, msgs) -> {
                ChatCell cell = findOrCreateChatCell(gid);
                Platform.runLater(() -> {
                    if (cell.getMessages().isEmpty()) {
                        msgs.sort(Comparator.comparing(Message::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())));
                        for (Message msg : msgs) cell.addMessage(msg);
                    } else {
                        cell.updateLastMessageDetails();
                    }
                });
            });
            return;
        }
        Map<UUID, List<Message>> map = allMsgs.stream()
                .filter(m -> m.getGroupId() != null)
                .collect(Collectors.groupingBy(Message::getGroupId));
        map.forEach((gid, msgs) -> {
            ChatCell cell = findOrCreateChatCell(gid);
            GroupInfo groupInfo = groupManager.getGroupInfo(gid);
            String finalName = cell.getOtherUsername();
            boolean isOneOnOne = false;
            String otherName = null;
            if (groupInfo != null) {
                if (groupInfo.getMembers() != null && groupInfo.getMembers().size() == 2) {
                    UUID otherUUID = null;
                    for (UUID member : groupInfo.getMembers()) {
                        if (!member.equals(currId)) { otherUUID = member; break; }
                    }
                    if (otherUUID != null) {
                        isOneOnOne = true;
                        final UUID fOtherUUID = otherUUID;
                        otherName = msgs.stream()
                                .filter(m -> fOtherUUID.equals(m.getSenderId()) && m.getSenderUsername() != null && !m.getSenderUsername().isEmpty())
                                .map(Message::getSenderUsername).findFirst().orElse(null);
                    }
                }
                if (!isOneOnOne && groupInfo.getGroupName() != null) {
                    String stored = groupInfo.getGroupName();
                    if (stored.startsWith("Chat: ") && stored.contains(" & ")) {
                        String[] parts = stored.substring("Chat: ".length()).split(" & ", 2);
                        if (parts.length == 2) {
                            isOneOnOne = true;
                            if (parts[0].equalsIgnoreCase(currName)) otherName = parts[1];
                            else if (parts[1].equalsIgnoreCase(currName)) otherName = parts[0];
                            else otherName = stored;
                        }
                    } else if (stored.startsWith("DM_")) {
                        isOneOnOne = true;
                    }
                }
            }
            if (isOneOnOne) {
                if (otherName != null && !otherName.isEmpty()) finalName = otherName;
                else finalName = "Loading...";
            } else {
                finalName = "Group " + gid.toString().substring(0, 4);
            }
            if (!cell.getOtherUsername().equals(finalName)) {
                final String nameForUI = finalName;
                Platform.runLater(() -> cell.setOtherUsername(nameForUI));
            }
            Platform.runLater(() -> {
                if (cell.getMessages().isEmpty()) {
                    msgs.sort(Comparator.comparing(Message::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())));
                    for (Message msg : msgs) cell.addMessage(msg);
                } else {
                    cell.updateLastMessageDetails();
                }
            });
        });
    }

    public record UserDisplay(UUID userId, String username) {}

    public void handleFetchGroupInfoResponse(JSONObject res) {
        try {
            UUID groupId = UUID.fromString(res.getString("groupId"));
            String srvName = res.optString("name", null);
            List<String> members = new ArrayList<>();
            org.json.JSONArray arr = res.getJSONArray("members");
            for (int i = 0; i < arr.length(); i++) members.add(arr.getString(i));
            String mapName = srvName;
            if (mapName == null || mapName.isEmpty()) {
                if (members.size() != 2) mapName = "Group " + groupId.toString().substring(0, 4);
                else mapName = "DM_" + groupId.toString().substring(0, 4);
            }
            UUID pseudoCreator = getLoggedInUserId() != null ? getLoggedInUserId() : UUID.randomUUID();
            groupManager.addGroupMapping(groupId, pseudoCreator, mapName);

            ChatCell cell = chatCellsByGroup.get(groupId);
            if (cell == null) cell = findOrCreateChatCell(groupId);
            final ChatCell fCell = cell;
            if (fCell != null) {
                String newName = null, currUser = getLoggedInUser();
                if (members.size() == 2 && currUser != null) {
                    String other = null;
                    for (String n : members) if (!n.equals(currUser)) { other = n; break; }
                    if (other != null) newName = other;
                    else newName = srvName;
                } else if (srvName != null && !srvName.isEmpty()) newName = srvName;
                else newName = "Group " + groupId.toString().substring(0, 4);
                if (newName != null) {
                    final String setName = newName;
                    Platform.runLater(() -> fCell.setOtherUsername(setName));
                }
            }
            pendingFetches.remove(groupId);
        } catch (Exception e) {
            UUID groupIdToClear = null;
            try { groupIdToClear = UUID.fromString(res.optString("groupId")); } catch (Exception ignored) {}
            if (groupIdToClear != null) pendingFetches.remove(groupIdToClear);
            e.printStackTrace();
        }
    }

    public void initiatePostLoginSync() {
        JSONObject request = RequestFactory.createFetchNewMessageRequest();
        if (msgCtrl != null) msgCtrl.getSendMessageQueue().offer(request);
    }

    private void processReceivedMessageBatch(List<Message> messages, String batchSourceDebugName) {
        if (messages == null || messages.isEmpty()) return;
        Map<UUID, List<Message>> messagesByGroup = messages.stream()
                .filter(Objects::nonNull)
                .filter(m -> m.getGroupId() != null)
                .collect(Collectors.groupingBy(Message::getGroupId));
        messagesByGroup.forEach((groupId, messagesInGroup) -> {
            ChatCell chatCell = findOrCreateChatCell(groupId);
            for (Message msg : messagesInGroup) MessageQueueManager.saveMessage(msg);
            Platform.runLater(() -> {
                messagesInGroup.sort(Comparator.comparing(Message::getTimestamp));
                for (Message msg : messagesInGroup) chatCell.addMessage(msg);
            });
            GroupInfo gi = groupManager.getGroupInfo(groupId);
            if (gi == null && !pendingGroupInfoFetches.contains(groupId)) {
                pendingGroupInfoFetches.add(groupId);
                JSONObject fetchReq = RequestFactory.createFetchGroupInfoRequest(groupId);
                if (msgCtrl != null && fetchReq != null) {
                    msgCtrl.getSendMessageQueue().offer(fetchReq);
                } else {
                    pendingGroupInfoFetches.remove(groupId);
                }
            }
        });
    }

    public void handleNewMessagesBatch(JSONObject serverResponse) {
        try {
            org.json.JSONArray messagesArray = serverResponse.getJSONArray("messages");
            Set<UUID> groupIds = new HashSet<>();
            List<Message> parsedMessages = new ArrayList<>();
            for (int i = 0; i < messagesArray.length(); i++) {
                JSONObject msgJson = messagesArray.getJSONObject(i);
                try {
                    if (!msgJson.has("groupId")) continue;
                    UUID groupId = UUID.fromString(msgJson.getString("groupId"));
                    groupIds.add(groupId);
                    UUID messageId = msgJson.has("id") ?
                            UUID.fromString(msgJson.getString("id")) :
                            UUID.randomUUID();
                    UUID senderId = UUID.fromString(msgJson.getString("senderId"));
                    String senderUsername = msgJson.optString("senderUsername");
                    String content = msgJson.getString("content");
                    Instant timestamp = Instant.parse(msgJson.getString("timestamp"));
                    Message message = new Message(messageId, senderId, senderUsername, groupId, content, timestamp);
                    parsedMessages.add(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            processReceivedMessageBatch(parsedMessages, "NewMessagesBatch");
            int groupCount = groupIds.size();
            int displayCount = 10;
            if (groupCount < displayCount) {
                JSONObject fetchPerGroupRequest = RequestFactory.createFetchLatestMessagesPerGroupRequest(Instant.now());
                if (msgCtrl != null) msgCtrl.getSendMessageQueue().offer(fetchPerGroupRequest);
            }
            loadAllChatHistories();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleFetchPerGroupBatch(JSONObject serverResponse) {
        try {
            org.json.JSONArray messagesArray = serverResponse.getJSONArray("messages");
            List<Message> parsedMessages = new ArrayList<>();
            for (int i = 0; i < messagesArray.length(); i++) {
                JSONObject msgJson = messagesArray.getJSONObject(i);
                try {
                    UUID groupId = UUID.fromString(msgJson.getString("groupId"));
                    UUID messageId = msgJson.has("messageId") ?
                            UUID.fromString(msgJson.getString("messageId")) :
                            UUID.randomUUID();
                    UUID senderId = UUID.fromString(msgJson.getString("senderId"));
                    String senderUsername = msgJson.optString("senderUsername");
                    String content = msgJson.getString("content");
                    Instant timestamp = Instant.parse(msgJson.getString("timestamp"));
                    Message message = new Message(messageId, senderId, senderUsername, groupId, content, timestamp);
                    parsedMessages.add(message);
                } catch (Exception e) {}
            }
            processReceivedMessageBatch(parsedMessages, "FetchPerGroupBatch");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetSessionState() {
        Platform.runLater(() -> chatCells.clear());
        chatCellsByGroup.clear();
        loggedInUser.set(null);
        loggedInUserId = null;
    }
}
