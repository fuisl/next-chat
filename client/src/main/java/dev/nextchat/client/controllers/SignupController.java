package dev.nextchat.client.controllers;

import dev.nextchat.client.models.Model;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SignupController implements Initializable {
    public TextField username;
    public TextField password;
    public TextField passwordConfirm;
    public Button signin_btn;
    public Label error_lbl;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        signin_btn.setOnAction(e -> signingin());
    }

    private void signingin() {
        Stage stage = (Stage) signin_btn.getScene().getWindow();
        Model.getInstance().getViewFactory().closeStage(stage);
        Model.getInstance().getViewFactory().showClientWindow();
    }
}
