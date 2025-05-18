package dev.nextchat.client.controllers.auth;

import dev.nextchat.client.backend.MessageController;
import dev.nextchat.client.backend.ServerResponseHandler;
import dev.nextchat.client.backend.utils.RequestFactory;
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

public class SignupController implements Initializable, ServerResponseHandler {
    public TextField username;
    public PasswordField password;
    public PasswordField passwordConfirm;
    public Button signin_btn;
    public Label error_lbl;
    public Button login_btn;
    
    private final MessageController msgCtrl = Model.getInstance().getMessageController();
     
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        signin_btn.setOnAction(e -> signup());
        login_btn.setOnAction(e -> login());
    }

    @Override
    public void onServerResponse(JSONObject resp) {
        switch (resp.getString("status")) {
            case "ok" -> Platform.runLater(() ->{
                Stage stage = (Stage) login_btn.getScene().getWindow();
                Model.getInstance().getViewFactory().closeStage(stage);
                Model.getInstance().getViewFactory().showClientWindow();
            });
            case "fail" -> Platform.runLater(() ->{
                error_lbl.setText(resp.getString("message"));
            });
        }
    }

    private void signup() {
        String user = username.getText();
        String pass = password.getText();

        // build a JSON signup request, e.g.:
        // { "type":"signup", "username":"…", "password":"…" }
        JSONObject req = RequestFactory.createSignupRequest(user, pass);
        msgCtrl.getSendMessageQueue().offer(req);
    }

    private void login() {
        Stage stage = (Stage) signin_btn.getScene().getWindow();
        Model.getInstance().getViewFactory().closeStage(stage);
        Model.getInstance().getViewFactory().showLoginWindow();
    }
}
