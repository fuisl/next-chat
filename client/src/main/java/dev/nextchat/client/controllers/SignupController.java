package dev.nextchat.client.controllers;

import dev.nextchat.client.models.Model;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SignupController implements Initializable {
    public TextField username;
    public PasswordField password;
    public PasswordField passwordConfirm;
    public Button signin_btn;
    public Label error_lbl;
    public Button login_btn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        signin_btn.setOnAction(e -> signingin());
        login_btn.setOnAction(e -> login());
    }

    private void signingin() {
        String user = username.getText().trim();
        String pass = password.getText().trim();
        String confirm = passwordConfirm.getText().trim();

        if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            error_lbl.setText("Please fill all fields.");
            return;
        }

        if (!pass.equals(confirm)) {
            error_lbl.setText("Passwords don't match.");
            return;
        }

        boolean success = Model.getInstance().registerUser(user, pass);

        if (success) {
            Stage stage = (Stage) signin_btn.getScene().getWindow();
            Model.getInstance().getViewFactory().closeStage(stage);
            Model.getInstance().getViewFactory().showClientWindow();
        } else {
            error_lbl.setText("Username already taken or error saving.");
        }
    }

    private void login() {
        Stage stage = (Stage) signin_btn.getScene().getWindow();
        Model.getInstance().getViewFactory().closeStage(stage);
        Model.getInstance().getViewFactory().showLoginWindow();
    }
}
