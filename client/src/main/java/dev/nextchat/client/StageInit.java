package dev.nextchat.client;

import dev.nextchat.client.App.StageReadyEvent;

import dev.nextchat.client.models.Model;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.net.URL;


@Component
public class StageInit implements ApplicationListener<StageReadyEvent> {
    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Model.getInstance().getViewFactory().showLoginWindow();

    }
}
