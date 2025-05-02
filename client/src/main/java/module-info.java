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
    requires de.jensd.fx.glyphs.fontawesome;
    requires com.google.common;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;
    requires org.json;

    opens dev.nextchat.client to javafx.fxml;
    opens dev.nextchat.client.controllers to javafx.fxml;
    opens dev.nextchat.client.models to com.fasterxml.jackson.databind;

    exports dev.nextchat.client;
    exports dev.nextchat.client.backend.model;
    exports dev.nextchat.client.backend.utils;

    opens dev.nextchat.client.backend.model to com.fasterxml.jackson.databind;
    opens dev.nextchat.client.backend.utils to org.json;

    exports dev.nextchat.client.controllers to javafx.fxml;
    exports dev.nextchat.client.models to javafx.fxml;

}
