package dev.nextchat.client.controllers;

import dev.nextchat.client.backend.ServerResponseHandler;
import dev.nextchat.client.controllers.auth.LoginController;
import dev.nextchat.client.controllers.auth.SignupController;
import dev.nextchat.client.controllers.chats.NewMsgBoxController;
import dev.nextchat.client.controllers.messages.MessagesController;
import org.json.JSONObject;

public class ResponseRouter implements ServerResponseHandler {
    private LoginController loginCtrl;
    private SignupController signupCtrl;
    private NewMsgBoxController newMessagesBoxController;
    private MessagesController messagesController;

    public void setLoginController(LoginController c)   { this.loginCtrl = c; }
    public void setSignupController(SignupController c) { this.signupCtrl = c; }
    public void setNewMessagesController(NewMsgBoxController newMessagesBoxController) {
        this.newMessagesBoxController = newMessagesBoxController;
    }
    public void setMessagesController(MessagesController c) {
        this.messagesController = c;
    }


    @Override
    public void onServerResponse(JSONObject resp) {
        String type = resp.getString("type");

        if (type.startsWith("login")) {
            if (loginCtrl != null) {
                loginCtrl.onServerResponse(resp);
            } else {
                System.out.println("!!! [ResponseRouter] LOGIN response but loginCtrl is null");
            }
        } else if (type.startsWith("signup")) {
            if (signupCtrl != null) {
                signupCtrl.onServerResponse(resp);
            } else {
                System.out.println("!!! [ResponseRouter] SIGNUP response but signupCtrl is null");
            }
        } else if (type.equals("checkUserExistenceResponse")) {
            if (newMessagesBoxController != null) {
                newMessagesBoxController.onServerResponse(resp);
            } else {
                System.out.println("!!! [ResponseRouter] CHECK_USER response but newMessagesBoxCtrl is null");
            }
        } else if (type.equals("create_group_response") || type.equals("createGroupResponse")) {
            if (messagesController != null) {
                messagesController.handleCreateGroupResponse(resp);
            } else {
                System.out.println("!!! [ResponseRouter] CREATE_GROUP response but MessagesController is null");
            }
        }
    }


}