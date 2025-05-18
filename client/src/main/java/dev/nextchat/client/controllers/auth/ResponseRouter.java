package dev.nextchat.client.controllers.auth;

import dev.nextchat.client.backend.ServerResponseHandler;
import org.json.JSONObject;

public class ResponseRouter implements ServerResponseHandler {
    private LoginController  loginCtrl;
    private SignupController signupCtrl;

    public void setLoginController(LoginController c)   { this.loginCtrl = c; }
    public void setSignupController(SignupController c) { this.signupCtrl = c; }

    @Override
    public void onServerResponse(JSONObject resp) {
        String type = resp.getString("type");
        if (type.startsWith("login")) {
            if (loginCtrl != null) {
                loginCtrl.onServerResponse(resp);
            } else {
                System.out.println("!!! [ResponseRouter] LOGIN response but loginCtrl is null");
            }
        }
        else if (type.startsWith("signup")) {
            if (signupCtrl != null) {
                signupCtrl.onServerResponse(resp);
            } else {
                System.out.println("!!! [ResponseRouter] SIGNUP response but signupCtrl is null");
            }
        }
    }

}