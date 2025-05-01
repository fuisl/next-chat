package dev.nextchat.client.controllers;

import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Message;
import dev.nextchat.client.models.Model;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class LoginController implements Initializable {
    public TextField username;
    public PasswordField password;
    public Label error_lbl;
    public Button login_btn;
    public Button SignUp_btn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        login_btn.setOnAction(e -> login());
        SignUp_btn.setOnAction(e -> signup());
    }

    private void signup() {
        Stage stage = (Stage) login_btn.getScene().getWindow();
        Model.getInstance().getViewFactory().closeStage(stage);
        Model.getInstance().getViewFactory().showSignupWindow();
    }


    public void login() {
        String user = username.getText().trim();
        String pass = password.getText().trim();

        boolean success = Model.getInstance().login(user, pass);

        if (success) {
            UUID userId = Model.getInstance().getLoggedInUserId();
            System.out.println("Login with userid " + userId);
            Model.getInstance().setLoggedInUser(user);
            Model.getInstance().loadMessagesForUser(userId);
            Model.getInstance().getViewFactory().closeStage((Stage) login_btn.getScene().getWindow());
            Model.getInstance().getViewFactory().showClientWindow();
        } else {
            error_lbl.setText("Invalid username or password");
        }
    }

}
