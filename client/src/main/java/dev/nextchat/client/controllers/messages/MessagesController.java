package dev.nextchat.client.controllers.messages;

import dev.nextchat.client.backend.MessageController; // Backend MessageController
import dev.nextchat.client.backend.utils.RequestFactory;
// Removed: import dev.nextchat.client.controllers.ResponseRouter; // Not directly used here now for responses
import dev.nextchat.client.database.MessageQueueManager;
import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import dev.nextchat.client.models.Message; // Ensure this is your dev.nextchat.client.models.Message

import javafx.application.Platform;
// Removed: @FXML (not used if you're not injecting FXML elements directly by that annotation, common in Spring-managed controllers)
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import org.json.JSONObject;

import java.net.URL;
import java.time.Instant;
// Removed: import java.util.LinkedList; // pendingMessages not used
// Removed: import java.util.Queue; // pendingMessages not used
import java.util.ResourceBundle;
import java.util.UUID;
// Removed: import java.util.concurrent.Executors; // Not used
// Removed: import java.util.concurrent.LinkedBlockingQueue; // Not used
// Removed: import java.util.concurrent.ScheduledExecutorService; // Not used
// Removed: import java.util.concurrent.TimeUnit; // Not used


public class MessagesController implements Initializable {
    public Label fid; // Displays the name of the current chat/friend
    public Button search_msg;
    public Button menu;
    public Button more_btn;
    public TextField msg_inp; // Input field for typing messages
    public Button send_btn;
    public ListView<Message> msgListView; // ListView to display messages

    private ChatCell currentChatCell; // The ChatCell for the currently open chat
    // Removed: private final Queue<String> pendingMessages = new LinkedList<>();
    // Removed: private final MessageController msgCtrl = Model.getInstance().getMsgCtrl(); // Get fresh instance if needed or ensure Model's is set
    // Removed: private ResponseRouter router = Model.getInstance().getResponseRouter(); // Not directly used for responses here

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Listen for changes in the selected chat from the ViewFactory
        // This will trigger loadChatByGroupId when a chat is selected
        Model.getInstance().getViewFactory().getClientSelectedChat().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                try {
                    // newVal is now expected to be the groupId as a String
                    UUID groupId = UUID.fromString(newVal);
                    loadChatByGroupId(groupId);
                } catch (IllegalArgumentException e) {
                    System.err.println("[MessagesController] Selected chat identifier is not a valid UUID: " + newVal);
                    fid.setText("Error: Invalid Chat ID");
                    if (currentChatCell != null) {
                        currentChatCell.getMessages().clear(); // Clear messages from previous chat
                    }
                    currentChatCell = null;
                }
            } else {
                // No chat selected, clear the view
                fid.setText("No Chat Selected");
                if (currentChatCell != null) {
                    currentChatCell.getMessages().clear();
                }
                currentChatCell = null;
                msgListView.setItems(null);
            }
        });

        // Attempt to load initially selected chat, if any
        String initialSelectedChatId = Model.getInstance().getViewFactory().getClientSelectedChat().get();
        if (initialSelectedChatId != null && !initialSelectedChatId.isBlank()) {
            try {
                UUID initialGroupId = UUID.fromString(initialSelectedChatId);
                loadChatByGroupId(initialGroupId);
            } catch (IllegalArgumentException e) {
                System.err.println("[MessagesController] Initial selected chat identifier is not a valid UUID: " + initialSelectedChatId);
            }
        } else {
            fid.setText("No Chat Selected");
        }

        // Send button action
        send_btn.setOnAction(e -> {
            String content = msg_inp.getText().trim();
            if (content.isEmpty() || currentChatCell == null || currentChatCell.getGroupId() == null) {
                System.out.println("[MessagesController] Cannot send message: Content is empty or no chat/groupId selected.");
                return;
            }

            UUID messageId = UUID.randomUUID(); // Generate unique ID for the message
            UUID senderId  = Model.getInstance().getLoggedInUserId();
            String senderUsername = Model.getInstance().getLoggedInUser();
            UUID groupId   = currentChatCell.getGroupId();
            Instant timestamp = Instant.now();
            Message newMessage = new Message(messageId, senderId, senderUsername, groupId, content, timestamp);
            MessageController backendMsgCtrl = Model.getInstance().getMsgCtrl(); // Get the backend MessageController
            if (backendMsgCtrl != null) {
                JSONObject messageJson = RequestFactory.createMessageRequest(newMessage);
                backendMsgCtrl.getSendMessageQueue().offer(messageJson);
                System.out.println("[MessagesController] Queued message to server: " + messageJson.toString());
            } else {
                System.err.println("[MessagesController] Backend MessageController is null. Cannot send message.");
                return;
            }

            MessageQueueManager.saveMessage(newMessage);
            System.out.println("[MessagesController] Saved message locally: " + newMessage.getId());

            currentChatCell.addMessage(newMessage);
            msgListView.scrollTo(currentChatCell.getMessages().size() - 1); // Scroll to the new message

            // 5. Clear input
            msg_inp.clear();
        });

        // Custom cell factory for rendering message bubbles
        msgListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null || msg.getSenderId() == null) { // Added null check for senderId
                    setText(null);
                    setGraphic(null);
                } else {
                    boolean isSender = msg.getSenderId().equals(Model.getInstance().getLoggedInUserId());
                    // Ensure ViewFactory and getMessageBubble are working as expected
                    Node bubbleNode = Model.getInstance().getViewFactory().getMessageBubble(msg, isSender);
                    setGraphic(bubbleNode);
                }
            }
        });
    }

    /**
     * Loads chat messages and updates the UI for the given groupId.
     * @param groupId The UUID of the group/chat to load.
     */
    private void loadChatByGroupId(UUID groupId) {
        if (groupId == null) {
            System.err.println("[MessagesController] Attempted to load chat with null groupId.");
            fid.setText("Error: Invalid Chat");
            if (currentChatCell != null) currentChatCell.getMessages().clear();
            currentChatCell = null;
            msgListView.setItems(null);
            return;
        }

        System.out.println("[MessagesController] Loading chat for group ID: " + groupId);
        currentChatCell = Model.getInstance().findOrCreateChatCell(groupId);

        if (currentChatCell != null) {
            fid.setText(currentChatCell.getOtherUsername()); // Set the display name of the chat/user
            msgListView.setItems(currentChatCell.getMessages()); // Bind ListView to the messages
            // Scroll to the bottom if there are messages
            if (!currentChatCell.getMessages().isEmpty()) {
                Platform.runLater(() -> msgListView.scrollTo(currentChatCell.getMessages().size() - 1));
            }
            System.out.println("[MessagesController] Displaying " + currentChatCell.getMessages().size() + " messages for " + currentChatCell.getOtherUsername());
        } else {
            System.err.println("[MessagesController] Failed to find or create ChatCell for group ID: " + groupId);
            fid.setText("Chat not found");
            msgListView.setItems(null); // Clear list view
        }
    }
}