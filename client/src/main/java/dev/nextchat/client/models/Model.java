package dev.nextchat.client.models;

import dev.nextchat.client.views.ViewFactory;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.ResultSet;

public class Model {
    private  static Model model;
    private final ViewFactory viewFactory;
    private final ObservableList<ChatCell> chatCells;

    private Model() {
        this.viewFactory = new ViewFactory();
        this.chatCells = FXCollections.observableArrayList();
    }

    public static synchronized Model getInstance() {
        if (model == null) {
            model = new Model();
        }
        return model;
    }

    public ChatCell findOrCreateChatCell(String receiver) {
        for (ChatCell cell : chatCells) {
            if (cell.senderProperty().get().equals(receiver)) {
                return cell;
            }
        }

        ChatCell newCell = new ChatCell(receiver, "", null);
        chatCells.add(newCell);
        System.out.println("New ChatCell created for: " + receiver);
        return newCell;
    }

    public ViewFactory getViewFactory() {
        return viewFactory;
    }

    public ObservableList<ChatCell> getChatCells() {
        return chatCells;
    }

    public void newChatCell(String fusername) {
        if (fusername != null && !fusername.trim().isEmpty()) {
            ChatCell newCell = new ChatCell(fusername, "", null);
            chatCells.add(newCell);
            System.out.println("New ChatCell added for user: " + fusername);
        }
    }




}
