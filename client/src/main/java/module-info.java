module dev.nextchat.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens dev.nextchat.client to javafx.fxml;
}