package dev.nextchat.client.controllers.messages;

import dev.nextchat.client.backend.MessageController;
import dev.nextchat.client.backend.utils.RequestFactory;
import dev.nextchat.client.database.MessageQueueManager;
import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import dev.nextchat.client.models.Message;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.beans.value.ChangeListener;

import org.json.JSONObject;

import java.net.URL;
import java.time.Instant;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public class MessagesController implements Initializable {
    public Label fid;
    public Button search_msg;
    public Button menu;
    public Button more_btn;
    public TextField msg_inp;
    public Button send_btn;
    public ListView<Message> msgListView;
    public MenuItem rename;
    public MenuItem leave;
    public MenuItem invite;
    private ChangeListener<String> currentChatNameListener;
    private UUID pendingInviteGroupId;
    private UUID pendingInviteGroupIdForUserCheck;
    private String pendingInviteUsernameForUserCheck;
    private ChatCell currChatCell;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Model.getInstance().getViewFactory().getClientSelectedChat().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                try {
                    UUID groupId = UUID.fromString(newVal);
                    loadChatByGroupId(groupId);
                } catch (IllegalArgumentException e) {
                    if (currChatCell != null) currChatCell.getMessages().clear();
                    currChatCell = null;
                }
            } else {
                if (currChatCell != null) currChatCell.getMessages().clear();
                currChatCell = null;
                msgListView.setItems(null);
            }
        });

        String initialChatId = Model.getInstance().getViewFactory().getClientSelectedChat().get();
        if (initialChatId != null && !initialChatId.isBlank()) {
            try {
                UUID groupId = UUID.fromString(initialChatId);
                loadChatByGroupId(groupId);
            } catch (IllegalArgumentException e) {
                fid.setText("Error: Invalid Chat ID");
            }
        } else {
            fid.setText("No Chat Selected");
        }

        send_btn.setOnAction(e -> {
            String content = msg_inp.getText().trim();
            if (content.isEmpty() || currChatCell == null || currChatCell.getGroupId() == null) return;
            UUID msgId = UUID.randomUUID();
            UUID senderId = Model.getInstance().getLoggedInUserId();
            String senderName = Model.getInstance().getLoggedInUser();
            UUID groupId = currChatCell.getGroupId();
            Instant ts = Instant.now();
            Message msg = new Message(msgId, senderId, senderName, groupId, content, ts);
            MessageController backend = Model.getInstance().getMsgCtrl();
            if (backend != null) {
                JSONObject msgJson = RequestFactory.createMessageRequest(msg);
                backend.getSendMessageQueue().offer(msgJson);
            } else {
                return;
            }
            MessageQueueManager.saveMessage(msg);
            currChatCell.addMessage(msg);
            msgListView.scrollTo(currChatCell.getMessages().size() - 1);
            msg_inp.clear();
        });

        msgListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null || msg.getSenderId() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    boolean isSender = msg.getSenderId().equals(Model.getInstance().getLoggedInUserId());
                    Node bubble = Model.getInstance().getViewFactory().getMessageBubble(msg, isSender);
                    setGraphic(bubble);
                }
            }
        });

        rename.setOnAction(e -> handleRenameGroup());
        leave.setOnAction(e -> handleLeaveGroup());
        invite.setOnAction(e -> handleInviteUser());
    }

    private void loadChatByGroupId(UUID groupId) {
        if (this.currChatCell != null && this.currentChatNameListener != null) {
            this.currChatCell.otherUsernameProperty().removeListener(this.currentChatNameListener);
            this.currentChatNameListener = null;
        }
        if (groupId == null) {
            fid.setText("Error: Invalid Chat");
            clearChatViewInternals();
            updateMenuItemsState(false);
            return;
        }
        currChatCell = Model.getInstance().findOrCreateChatCell(groupId);
        if (currChatCell != null) {
            fid.setText(currChatCell.getOtherUsername());
            msgListView.setItems(currChatCell.getMessages());
            if (!currChatCell.getMessages().isEmpty()) {
                Platform.runLater(() -> msgListView.scrollTo(currChatCell.getMessages().size() - 1));
            }
            this.currentChatNameListener = (observable, oldValue, newValue) -> fid.setText(newValue);
            this.currChatCell.otherUsernameProperty().addListener(this.currentChatNameListener);
            updateMenuItemsState(true);
        } else {
            fid.setText("Chat not found");
            msgListView.setItems(null);
        }
    }

    private void clearChatViewInternals() {
        currChatCell = null;
        msgListView.setItems(null);
    }

    private void updateMenuItemsState(boolean chatSelected) {
        rename.setDisable(!chatSelected);
        leave.setDisable(!chatSelected);
    }

    private void handleRenameGroup() {
        if (currChatCell == null || currChatCell.getGroupId() == null) return;

        TextInputDialog dialog = new TextInputDialog(currChatCell.getOtherUsername());
        dialog.setTitle("Rename Group");
        dialog.setHeaderText("Enter the new name for the group:");
        dialog.setContentText("Name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty() && !newName.equals(currChatCell.getOtherUsername())) {
                UUID groupId = currChatCell.getGroupId();
                JSONObject renameRequest = RequestFactory.createRenameGroupRequest(groupId, newName.trim());
                Model.getInstance().getMsgCtrl().getSendMessageQueue().offer(renameRequest);
            }
        });
    }

    private void handleLeaveGroup() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Leave Group");
        alert.setHeaderText("Are you sure you want to leave the group \"" + currChatCell.getOtherUsername() + "\"?");
        alert.setContentText("This action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            UUID groupId = currChatCell.getGroupId();
            JSONObject leaveRequest = RequestFactory.createLeaveGroupRequest(groupId);
            Model.getInstance().getMsgCtrl().getSendMessageQueue().offer(leaveRequest);
        }
    }

    private void handleInviteUser() {
        if (currChatCell == null || currChatCell.getGroupId() == null) {
            new Alert(Alert.AlertType.ERROR, "Please select a chat to invite a user to.", ButtonType.OK).showAndWait();
            return;
        }

        this.pendingInviteGroupIdForUserCheck = currChatCell.getGroupId();

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Invite User to Group");
        dialog.setHeaderText("Enter the username of the person you want to invite to '" + currChatCell.getOtherUsername() + "'.");
        dialog.setContentText("Username:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(username -> {
            if (!username.trim().isEmpty()) {
                this.pendingInviteUsernameForUserCheck = username.trim();
                JSONObject checkUserRequest = RequestFactory.checkIfUserExist(this.pendingInviteUsernameForUserCheck);

                MessageController backend = Model.getInstance().getMsgCtrl();
                if (backend != null) {
                    backend.getSendMessageQueue().offer(checkUserRequest);
                } else {
                    this.pendingInviteGroupIdForUserCheck = null;
                    this.pendingInviteUsernameForUserCheck = null;
                    new Alert(Alert.AlertType.ERROR, "System error: Unable to connect to the server.", ButtonType.OK).showAndWait();
                }
            }
        });
    }

    public void handleUserExistenceResponseForInvite(JSONObject responseJson) {
        if (this.pendingInviteGroupIdForUserCheck == null || this.pendingInviteUsernameForUserCheck == null) return;

        Platform.runLater(() -> {
            if (responseJson == null) {
                new Alert(Alert.AlertType.ERROR, "Received an empty response from server for user check.", ButtonType.OK).showAndWait();
                clearPendingInviteState();
                return;
            }

            String respondedUsername = responseJson.optString("username");
            boolean exists = responseJson.optBoolean("exists", false);

            if (responseJson.has("error")) {
                new Alert(Alert.AlertType.ERROR, "Server could not verify user: " + responseJson.optString("error", "Unknown error"), ButtonType.OK).showAndWait();
                clearPendingInviteState();
                return;
            }

            if (exists) {
                String userIdString = responseJson.optString("userId");
                if (userIdString == null || userIdString.isEmpty()) {
                    new Alert(Alert.AlertType.ERROR, "User exists but server did not provide User ID.", ButtonType.OK).showAndWait();
                    clearPendingInviteState();
                    return;
                }

                try {
                    UUID userIdToInvite = UUID.fromString(userIdString);
                    if (userIdToInvite.equals(Model.getInstance().getLoggedInUserId())) {
                        new Alert(Alert.AlertType.WARNING, "Cannot invite yourself to the group.", ButtonType.OK).showAndWait();
                        clearPendingInviteState();
                        return;
                    }

                    JSONObject joinGroupRequest = RequestFactory.createJoinGroupRequest(userIdToInvite, this.pendingInviteGroupIdForUserCheck);
                    MessageController backend = Model.getInstance().getMsgCtrl();
                    if (backend != null) {
                        backend.getSendMessageQueue().offer(joinGroupRequest);
                        new Alert(Alert.AlertType.INFORMATION, "Invitation sent for user '" + respondedUsername + "'.", ButtonType.OK).showAndWait();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "System error: Unable to connect to the server for step 2.", ButtonType.OK).showAndWait();
                    }
                } catch (IllegalArgumentException e) {
                    new Alert(Alert.AlertType.ERROR, "Server provided an invalid User ID format for '" + respondedUsername + "'.", ButtonType.OK).showAndWait();
                }
            } else {
                new Alert(Alert.AlertType.INFORMATION, "User '" + respondedUsername + "' not found.", ButtonType.OK).showAndWait();
            }
            clearPendingInviteState();
        });
    }

    private void clearPendingInviteState() {
        this.pendingInviteGroupIdForUserCheck = null;
        this.pendingInviteUsernameForUserCheck = null;
    }
}
