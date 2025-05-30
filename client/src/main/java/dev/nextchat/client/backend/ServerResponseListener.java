package dev.nextchat.client.backend;

import javafx.application.Platform;
import org.json.JSONObject;

// 2. Create a Runnable that polls the queue and hands every message
//    off to your handler (on the JavaFX thread):
public class ServerResponseListener implements Runnable {
    private final MessageController msgCtrl;
    private final ServerResponseHandler handler;

    public ServerResponseListener(MessageController msgCtrl,
                                  ServerResponseHandler handler) {
        this.msgCtrl  = msgCtrl;
        this.handler  = handler;
    }

    @Override
    public void run() {
        System.out.println(">>> [ServerResponseListener] thread started, waiting for messages...");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // 1) Pull the next JSON from the received queue
                JSONObject resp = msgCtrl.getReceivedMessageQueue().take();

                // 2) Log it to the console
                System.out.println("<<< [ServerResponseListener] Received from server: "
                        + resp.toString());

                // 3) Dispatch to your handler on the FX thread
                Platform.runLater(() -> handler.onServerResponse(resp));
            }
        } catch (InterruptedException e) {
            System.out.println(">>> [ServerResponseListener] interrupted, exiting");
            Thread.currentThread().interrupt();
        }
    }

}

