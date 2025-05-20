package dev.nextchat.client.controllers.chats;

import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ChatCellController implements Initializable {
    public Button chat_msg_btn;
    public Label fusername;
    public Label txt_msg;
    public Label txt_date;

    private final ChatCell cell;

    public ChatCellController(ChatCell cell) {
        this.cell = cell;
    }

    private String formatInstant(java.time.Instant instant, DateTimeFormatter formatter) {
        return java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).format(formatter);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fusername.textProperty().bind(cell.senderProperty());
        txt_msg.textProperty().bind(cell.txtMsgProperty());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

        if (cell.timestampProperty().get() != null) {
            String formattedTime = formatInstant(cell.timestampProperty().get(), formatter);
            txt_date.setText(formattedTime);
        } else {
            txt_date.setText(""); // or "—"
        }

        // Optional: listen for changes and update time dynamically
        cell.timestampProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String formattedTime = formatInstant(newVal, formatter);
                txt_date.setText(formattedTime);
            }
        });
    }
    @FXML
    public void onChatButtonClick() {
        String selectedUser = cell.senderProperty().get();
        System.out.println("Selected chat with: " + selectedUser);
        Model.getInstance().getViewFactory().getClientSelectedChat().set(selectedUser);
    }
}
