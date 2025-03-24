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
    public ViewFactory getViewFactory() {
        return viewFactory;
    }

    private void newChatCell(ObservableList<ChatCell> cells) {
        ResultSet resultSet = null;
        try {

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAllChatCells() {
        newChatCell(this.chatCells);
    }

    public ObservableList<ChatCell> getChatCells() {
        return chatCells;
    }
}
