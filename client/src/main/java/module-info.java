module dev.nextchat.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires jakarta.annotation;
    requires spring.core;
    requires java.sql;
    requires de.jensd.fx.glyphs.fontawesome;


    opens dev.nextchat.client to javafx.fxml;
    opens dev.nextchat.client.controllers to javafx.fxml;

    exports dev.nextchat.client;
    exports dev.nextchat.client.controllers to javafx.fxml;
    exports dev.nextchat.client.models to javafx.fxml;
}