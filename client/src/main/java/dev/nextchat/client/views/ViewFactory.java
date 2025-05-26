package dev.nextchat.client.views;

import dev.nextchat.client.controllers.ClientController;

import dev.nextchat.client.controllers.chats.NewGroupController;
import dev.nextchat.client.controllers.chats.NewMsgBoxController;
import dev.nextchat.client.controllers.messages.MessagesController;
import dev.nextchat.client.controllers.messages.MsgBBController;
import dev.nextchat.client.controllers.auth.LoginController;
import dev.nextchat.client.controllers.auth.SignupController;
import dev.nextchat.client.models.Message;
import dev.nextchat.client.models.Model;
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
import java.util.UUID;

public class ViewFactory {
    private final StringProperty clientSelectedChat;
    private AnchorPane chatsView;
    private AnchorPane msgView;
    private AnchorPane newMsgView;
    private AnchorPane newGroupView;
    private final StringProperty clientSelection;
    private LoginController loginController;
    private SignupController signUpController;
    private NewMsgBoxController newMsgController;
    private MessagesController messageController;
    private NewGroupController newGroupViewController;

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
        Model.getInstance().getResponseRouter().setSignupController(signUpController);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/logoButGreen.png")));
        stage.setTitle("Signup");
        stage.setResizable(false);
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
        Model.getInstance().getResponseRouter().setLoginController(loginController); // Set it
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/Images/logoButGreen.png")));
        stage.setTitle("Login");
        stage.setResizable(false);
        stage.show();
    }

    public LoginController getLoginController() {
        return loginController;
    }

    public void showClientWindow() {
        UUID id = Model.getInstance().getLoggedInUserId();
        Model.getInstance().setLoggedInUserId(id);
        Model.getInstance().getResponseRouter().setNewMessagesController(null);
        Model.getInstance().getResponseRouter().setNewGroupController(null);
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
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> {
            Model.getInstance().resetSessionState();
        });
        stage.show();
    }

    public AnchorPane getNewMsgWindow() {
        if (newMsgView == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/NewMsgBox.fxml"));
                newMsgView = loader.load();
                newMsgController = loader.getController();
                Model.getInstance().getResponseRouter().setNewMessagesController(newMsgController); // Set it
                Model.getInstance().getResponseRouter().setNewGroupController(null); // Clear the other
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newMsgView;
    }

    public NewMsgBoxController getNewMsgController() {
        return newMsgController;
    }

    public AnchorPane getNewGroupWindow() {
        if (newGroupView == null) {
            try{
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/NewGroup.fxml"));
                newGroupView = loader.load();
                newGroupViewController = loader.getController();
                Model.getInstance().getResponseRouter().setNewGroupController(newGroupViewController); // Set it
                Model.getInstance().getResponseRouter().setNewMessagesController(null); // Clear the other
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
