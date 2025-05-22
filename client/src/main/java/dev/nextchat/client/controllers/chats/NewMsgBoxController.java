package dev.nextchat.client.controllers.chats;

import dev.nextchat.client.backend.MessageController;
import dev.nextchat.client.backend.ServerResponseHandler;
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
                Model.getInstance().findOrCreateChatCell(enteredUsername);
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
                // 1) Extract and store the other user's UUID
                String userIdStr = response.getString("userId");
                UUID otherId = UUID.fromString(userIdStr);
                Model.getInstance().addKnownUser(pendingUsername, otherId);

                // 3) Switch UI to the new chat
                Model.getInstance().getViewFactory()
                        .getClientSelectedChat()
                        .set(pendingUsername);
                Model.getInstance().getViewFactory()
                        .getClientSelection()
                        .set("Chats");
            } else {
                error_lbl.setText("Username not found");
            }
            fusername.clear();
        });
    }

}
