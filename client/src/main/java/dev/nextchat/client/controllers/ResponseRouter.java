package dev.nextchat.client.controllers;

import dev.nextchat.client.backend.ServerResponseHandler;
import dev.nextchat.client.controllers.auth.LoginController;
import dev.nextchat.client.controllers.auth.SignupController;
import dev.nextchat.client.controllers.chats.NewGroupController;
import dev.nextchat.client.controllers.chats.NewMsgBoxController;
import dev.nextchat.client.controllers.messages.MessagesController;
import dev.nextchat.client.models.Model;
import org.json.JSONObject;

public class ResponseRouter implements ServerResponseHandler {
    private LoginController loginCtrl;
    private SignupController signupCtrl;
    private NewMsgBoxController newMessagesBoxController;
    private MessagesController messagesController;
    private NewGroupController newGroupController;

    public void setLoginController(LoginController c)   { this.loginCtrl = c; }
    public void setSignupController(SignupController c) { this.signupCtrl = c; }
    public void setNewMessagesController(NewMsgBoxController newMessagesBoxController) {
        this.newMessagesBoxController = newMessagesBoxController;
    }
    public void setMessagesController(MessagesController c) {
        this.messagesController = c;
    }
    public void setNewGroupController(NewGroupController newGroupController) {
        this.newGroupController = newGroupController;
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
            }
            if (newGroupController != null) {
                newGroupController.onServerResponse(resp);
            }
            if (messagesController != null) {
                messagesController.handleUserExistenceResponseForInvite(resp);
            }
        } else if (type.equals("create_group_response") || type.equals("createGroupResponse")) {
            Model.getInstance().handleCreateGroupResponse(resp);
        } else if (type.equals("message")) {
            Model.getInstance().handleIncomingChatMessage(resp); // Sender also uses this
        } else if (type.equals("fetch_group_info_response")) {
            Model.getInstance().handleFetchGroupInfoResponse(resp);
        } else if (type.equals("fetch_new_message_response")) {
            Model.getInstance().handleNewMessagesBatch(resp);
        } else if (type.equals("fetch_per_group_response")) {
            Model.getInstance().handleFetchPerGroupBatch(resp);
        } else if (type.equals("fetch_message_response")) {
            Model.getInstance().handleOlderMessagesBatch(resp);
        } else if (type.equals("search_conversation_response")) {
            Model.getInstance().handleSearchConversationResponse(resp);
        } else if (type.equals("fetch_group_with_user_response")) {
            Model.getInstance().handleFetchGroupWithUserResponse(resp);
        } else if (type.equals("leave_group_response")) {
            Model.getInstance().handleLeaveGroupResponse(resp);
        } else if (type.equals("rename_group_response")) {
            Model.getInstance().handleRenameGroupResponse(resp);
        } else if (type.equals("change_group_name")) {
            Model.getInstance().handleGroupRenameBroadcast(resp);
        }
    }
}