package dev.nextchat.client.controllers.auth;

import dev.nextchat.client.backend.MessageController;
import dev.nextchat.client.backend.ServerResponseHandler;
import dev.nextchat.client.backend.utils.RequestFactory;
import dev.nextchat.client.controllers.ResponseRouter;
import dev.nextchat.client.models.Model;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

public class LoginController implements Initializable, ServerResponseHandler {
    public TextField username;
    public PasswordField password;
    public Label error_lbl;
    public Button login_btn;
    public Button SignUp_btn;
    private String pendingUsername;

    private final MessageController msgCtrl = Model.getInstance().getMsgCtrl();
    private ResponseRouter router = Model.getInstance().getResponseRouter();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        login_btn.setOnAction(e -> login());
        SignUp_btn.setOnAction(e -> signup());
    }

    @Override
    public void onServerResponse(JSONObject resp) {
        switch (resp.getString("status")) {
            case "ok" -> Platform.runLater(() ->{
                Stage stage = (Stage) login_btn.getScene().getWindow();
                Model.getInstance().getViewFactory().closeStage(stage);
                Model.getInstance().getViewFactory().showClientWindow();
                String user = pendingUsername;
                UUID id   = UUID.fromString(resp.getString("userId"));

                // 2) store it in the Model
                Model.getInstance().setLoggedInUser(user);
                Model.getInstance().setLoggedInUserId(id);
                Model.getInstance().initializeUIData();

                // 3) now you can safely call getLoggedInUser() elsewhere
                System.out.println(">>> Logged in as: " + Model.getInstance().getLoggedInUserId());
                Model.getInstance().initiatePostLoginSync();
            });
            case "fail" -> Platform.runLater(() ->{
               error_lbl.setText(resp.getString("message"));
            });
        }
    }

    private void signup() {
        Stage stage = (Stage) login_btn.getScene().getWindow();
        Model.getInstance().getViewFactory().closeStage(stage);
        Model.getInstance().getViewFactory().showSignupWindow();
        router.setSignupController(Model.getInstance().getViewFactory().getSignUpController());
    }


    private void login() {
        String user = username.getText();
        String pass = password.getText();
        // build a JSON login request, e.g.:
        // { "type":"login", "username":"…", "password":"…" }
        pendingUsername = user;
        JSONObject req = RequestFactory.createLoginRequest(user, pass);
        msgCtrl.getSendMessageQueue().offer(req);
    }
}
