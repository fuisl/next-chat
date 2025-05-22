package dev.nextchat.client.controllers.chats;

import dev.nextchat.client.backend.MessageController;
import dev.nextchat.client.backend.ServerResponseHandler;
import dev.nextchat.client.backend.utils.RequestFactory;
import dev.nextchat.client.models.Model;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.json.JSONObject;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

public class NewMsgBoxController implements Initializable, ServerResponseHandler {
    public Button return_btn;
    public TextField fusername;
    public Button new_grp_btn;
    public Button chat_btn;
    public Label Uid;
    public Button self_chat;
    public Label error_lbl;
    private String pendingUsername;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addListeners();
        String currentUser = Model.getInstance().getLoggedInUser();
        if (currentUser != null) {
            Uid.setText(currentUser + " (You)");
        }
        Model.getInstance().loggedInUserProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Uid.setText(newVal + " (You)");
            }
        });
    }
    private void addListeners() {
        return_btn.setOnAction(e -> {
            Model.getInstance().getViewFactory().getClientSelection().set("Chats");
            fusername.clear();
        });
        self_chat.setOnAction(e -> {

            String enteredUsername = Model.getInstance().getLoggedInUser();
            if (!enteredUsername.isEmpty()) {
               // Model.getInstance().findOrCreateChatCell(enteredUsername);
                Model.getInstance().getViewFactory().getClientSelectedChat().set(enteredUsername);
                Model.getInstance().getViewFactory().getClientSelection().set("Chats");
            }
            fusername.clear();
        });

        chat_btn.setOnAction(e -> {
            String enteredUsername = fusername.getText().trim(); // from input field
            pendingUsername = enteredUsername;
            JSONObject request = new JSONObject();
            request.put("type", "check_user_existence");
            request.put("username", enteredUsername);

            MessageController msgCtrl = Model.getInstance().getMsgCtrl();
            msgCtrl.getSendMessageQueue().offer(request);

            fusername.clear();
        });
        new_grp_btn.setOnAction(e -> {
            Model.getInstance().getViewFactory().getClientSelection().set("Group");
        });
    }


    @Override
    public void onServerResponse(JSONObject response) {
        String type = response.optString("type");
        if (!"checkUserExistenceResponse".equals(type)) return;

        boolean exists = response.optBoolean("exists", false);

        Platform.runLater(() -> {
            if (exists) {
                String userIdStr = response.getString("userId"); // This is User B's ID
                UUID otherUserId = UUID.fromString(userIdStr);

                error_lbl.setText("User '" + pendingUsername + "' found. Creating chat session...");

                UUID loggedInUserId = Model.getInstance().getLoggedInUserId(); // This is User A's ID

                if (loggedInUserId != null) {
                    Model.getInstance().setPendingInviteForNextGroup(otherUserId);

                    // Create a generic group name for 1-on-1. Server might override or manage this.
                    String groupNameFor1on1 = "Chat: " + Model.getInstance().getLoggedInUser() + " & " + pendingUsername;
                    String groupDescription = "Direct chat";

                    JSONObject createGroupReq = RequestFactory.createNewGroupRequest(
                            loggedInUserId, // The user creating the group
                            groupNameFor1on1,
                            groupDescription
                    );
                    Model.getInstance().getMsgCtrl().getSendMessageQueue().offer(createGroupReq);
                    // Flow continues in Model.handleCreateGroupResponse once server replies
                } else {
                    error_lbl.setText("Error: Could not initiate chat. Session issue.");
                    Model.getInstance().setPendingInviteForNextGroup(null); // Ensure state is cleared if pre-check fails
                }
            } else {
                error_lbl.setText("Username '" + pendingUsername + "' not found.");
            }
            fusername.clear();
        });
    }

}
