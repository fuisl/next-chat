package dev.nextchat.client.views;

import dev.nextchat.client.controllers.ClientController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewFactory {
    private AnchorPane chatsView;

    public ViewFactory() {}

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
        stage.setTitle("Chats");
        stage.show();
    }
    public void closeStage(Stage stage) {
        stage.close();
    }
}
