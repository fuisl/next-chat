package dev.nextchat.client.controllers.chats;

import dev.nextchat.client.models.Model;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.function.Consumer;

public class MembersGroupController {
    public Label username;
    public Button remove_btn;
    private Model.UserDisplay currentUserDisplay;
    private Consumer<Model.UserDisplay> removeActionCallback; // Callback to notify NewGroupController

    public void setData(Model.UserDisplay userDisplay, Consumer<Model.UserDisplay> onRemove) {
        this.currentUserDisplay = userDisplay;
        this.removeActionCallback = onRemove;

        if (userDisplay != null) {
            username.setText(userDisplay.username());
        } else {
            username.setText(""); // Or some placeholder if userDisplay can be null
        }
    }


    @FXML
    private void handleRemoveMember() {
        if (removeActionCallback != null && currentUserDisplay != null) {
            removeActionCallback.accept(currentUserDisplay); // Execute the remove logic passed from the factory/NewGroupController
        }
    }

}
