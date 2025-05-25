package dev.nextchat.client;

import dev.nextchat.client.App.StageReadyEvent;

import dev.nextchat.client.backend.MessageController;
import dev.nextchat.client.backend.ServerResponseListener;
import dev.nextchat.client.controllers.auth.LoginController;
import dev.nextchat.client.controllers.ResponseRouter;
import dev.nextchat.client.controllers.messages.MessagesController;
import dev.nextchat.client.models.Model;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StageInit implements ApplicationListener<StageReadyEvent> {

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Model.getInstance().getViewFactory().showLoginWindow();
        MessagesController msgsCtrl = new MessagesController();
        LoginController loginCtrl = Model.getInstance().getViewFactory().getLoginController();

        ResponseRouter router = Model.getInstance().getResponseRouter();
        router.setLoginController(loginCtrl);
        router.setMessagesController(msgsCtrl);
        MessageController mc = Model.getInstance().getMsgCtrl();
        Thread listener = new Thread(
                new ServerResponseListener(mc, router),
                "ServerResponseListener"
        );
        listener.setDaemon(true);
        System.out.println(">>> [StageInit] Starting ServerResponseListener thread");
        listener.start();
    }
}

