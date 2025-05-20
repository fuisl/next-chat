package dev.nextchat.client;

import dev.nextchat.client.App.StageReadyEvent;

import dev.nextchat.client.backend.MessageController;
import dev.nextchat.client.backend.ServerResponseListener;
import dev.nextchat.client.controllers.auth.LoginController;
import dev.nextchat.client.controllers.ResponseRouter;
import dev.nextchat.client.models.Model;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StageInit implements ApplicationListener<StageReadyEvent> {

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        // 1) Show the login scene — this will load login.fxml and create LoginController
        System.out.println(">>> [StageInit] Stage is ready — showing Login Window");
        Model.getInstance().getViewFactory().showLoginWindow();

        // 2) Grab the LoginController out of your ViewFactory
        LoginController loginCtrl =
                Model.getInstance().getViewFactory().getLoginController();

        // 3) Create & wire up your ResponseRouter
        ResponseRouter router = Model.getInstance().getResponseRouter();
        router.setLoginController(loginCtrl);
        // (later, when you load sign-up, you can do router.setSignupController(...))

        // 4) Start the one listener thread to dispatch *all* incoming server JSON
        MessageController mc = Model.getInstance().getMessageController();
        Thread listener = new Thread(
                new ServerResponseListener(mc, router),
                "ServerResponseListener"
        );
        listener.setDaemon(true);   // won't block JVM exit
        System.out.println(">>> [StageInit] Starting ServerResponseListener thread");
        listener.start();
    }
}

