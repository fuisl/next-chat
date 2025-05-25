package dev.nextchat.client.controllers.chats;

import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.ResourceBundle;

public class ChatCellController implements Initializable {
    @FXML public Button chat_msg_btn;
    @FXML public Label fusername;
    @FXML public Label txt_msg;
    @FXML public Label txt_date;

    private final ChatCell cell;

    public ChatCellController(ChatCell cell) {
        this.cell = cell;
    }

    private String formatInstant(java.time.Instant instant, DateTimeFormatter formatter) {
        if (instant == null) return "";
        return java.time.LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (cell != null) {
            fusername.textProperty().bind(cell.otherUsernameProperty());
            txt_msg.textProperty().bind(cell.lastMessageProperty());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

            if (cell.timestampProperty().get() != null) {
                txt_date.setText(formatInstant(cell.timestampProperty().get(), formatter));
            } else {
                txt_date.setText("");
            }

            cell.timestampProperty().addListener((obs, oldVal, newVal) -> {
                txt_date.setText(formatInstant(newVal, formatter));
            });
        }
    }

    @FXML
    public void onChatButtonClick() {
        if (cell != null && cell.getGroupId() != null) {
            String groupIdStr = cell.getGroupId().toString();
            System.out.println("[ChatCellController] Selected chat with Group ID: " + groupIdStr + " (Name: " + cell.getOtherUsername() +")");
            Model.getInstance().getViewFactory().getClientSelectedChat().set(groupIdStr);
            Model.getInstance().fetchOlderMessagesForGroup(cell.getGroupId());
        } else {
            System.err.println("[ChatCellController] Cannot select chat: cell or groupId is null.");
        }
    }
}