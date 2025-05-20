package dev.nextchat.client.views;

import dev.nextchat.client.controllers.ClientController;

import dev.nextchat.client.controllers.messages.MsgBBController;
import dev.nextchat.client.controllers.auth.LoginController;
import dev.nextchat.client.controllers.auth.SignupController;
import dev.nextchat.client.models.Message;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewFactory {
    private final StringProperty clientSelectedChat;
    private AnchorPane chatsView;
    private AnchorPane msgView;
    private AnchorPane newMsgView;
    private AnchorPane newGroupView;
    private final StringProperty clientSelection;
    private LoginController loginController;
    private SignupController signUpController;

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
        signUpController = loader.getController();
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/logoButGreen.png")));
        stage.setTitle("Signup");
        stage.show();
    }

    public SignupController getSignUpController() {
        return signUpController;
    }

    public void showLoginWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Login.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e){
            e.printStackTrace();
        }
        loginController = loader.getController();
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/logoButGreen.png")));
        stage.setTitle("Login");
        stage.show();
    }

    public LoginController getLoginController() {
        return loginController;
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

    public AnchorPane getNewGroupWindow() {
        if (newGroupView == null) {
            try{
                newGroupView = new FXMLLoader(getClass().getResource("/Fxml/NewGroup.fxml")).load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newGroupView;
    }

    public Node getMessageBubble(Message message, boolean isSender) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/MsgBB.fxml"));
            Node bubble = loader.load();

            MsgBBController controller = loader.getController();
            controller.setMessage(message, isSender);

            return bubble;
        } catch (IOException e) {
            e.printStackTrace();
            return new Label("Failed to load message");
        }
    }

    public void closeStage(Stage stage) {
        stage.close();
    }
}
