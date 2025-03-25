package dev.nextchat.client.views;

import dev.nextchat.client.controllers.ClientController;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewFactory {
    private final StringProperty clientSelectedChat;
    private AnchorPane chatsView;
    private AnchorPane msgView;
    private AnchorPane newMsgView;
    private final StringProperty clientSelection;

    public ViewFactory() {
        this.clientSelectedChat = new SimpleStringProperty(" ");
        this.clientSelection = new SimpleStringProperty(" ");
    }

    public StringProperty getClientSelectedChat() {
        return clientSelectedChat;
    }

    public StringProperty getClientSelection(){
        return clientSelection;
    }

    public AnchorPane getChatsView() {
        if (chatsView == null) {
            try {
                chatsView = new FXMLLoader(getClass().getResource("/Fxml/Chats.fxml")).load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return chatsView;
    }

    public AnchorPane getMsgView() {
        if (msgView == null) {
            try{
                msgView = new FXMLLoader(getClass().getResource("/Fxml/Messages.fxml")).load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return msgView;

    }
    public void showSignupWindow(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Signup.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e){
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/logoButGreen.png")));
        stage.setTitle("Signup");
        stage.show();
    }


    public void showLoginWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Login.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e){
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/logoButGreen.png")));
        stage.setTitle("Login");
        stage.show();
    }

    public void showClientWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client.fxml"));
        ClientController clientController = new ClientController();
        loader.setController(clientController);
        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e){
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/logoButGreen.png")));
        stage.setTitle("Chats");
        stage.show();
    }

    public AnchorPane getNewMsgWindow() {
        if (newMsgView == null) {
            try{
                newMsgView = new FXMLLoader(getClass().getResource("/Fxml/NewMsgBox.fxml")).load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newMsgView;
    }

    public void closeStage(Stage stage) {
        stage.close();
    }
}
