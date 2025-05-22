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
        // 1) Show the login scene — this will load login.fxml and create LoginController
        Model.getInstance().getViewFactory().showLoginWindow();
        Model.getInstance().initializeUIData();
        MessagesController msgsCtrl = new MessagesController();
        LoginController loginCtrl = Model.getInstance().getViewFactory().getLoginController();

        ResponseRouter router = Model.getInstance().getResponseRouter();
        router.setLoginController(loginCtrl);
        router.setMessagesController(msgsCtrl);
        // 4) Start the one listener thread to dispatch *all* incoming server JSON
        MessageController mc = Model.getInstance().getMsgCtrl();
        Thread listener = new Thread(
                new ServerResponseListener(mc, router),
                "ServerResponseListener"
        );
        listener.setDaemon(true);   // won't block JVM exit
        System.out.println(">>> [StageInit] Starting ServerResponseListener thread");
        listener.start();
    }
}

