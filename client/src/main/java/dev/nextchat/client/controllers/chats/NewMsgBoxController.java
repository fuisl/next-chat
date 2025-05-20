package dev.nextchat.client.controllers.chats;

import dev.nextchat.client.models.Model;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class NewMsgBoxController implements Initializable {
    public Button return_btn;
    public TextField fusername;
    public Button new_grp_btn;
    public Button chat_btn;
    public Label Uid;
    public Button self_chat;
    public Label error_lbl;

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

            Model.getInstance().findOrCreateChatCell(enteredUsername);
            Model.getInstance().getViewFactory().getClientSelectedChat().set(enteredUsername);
            Model.getInstance().getViewFactory().getClientSelection().set("Chats");
            fusername.clear();
        });
        new_grp_btn.setOnAction(e -> {
            Model.getInstance().getViewFactory().getClientSelection().set("Group");
        });
    }



}
