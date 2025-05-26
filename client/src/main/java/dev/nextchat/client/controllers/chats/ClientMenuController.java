package dev.nextchat.client.controllers.chats;

import dev.nextchat.client.backend.utils.RequestFactory;
import dev.nextchat.client.models.Model;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientMenuController implements Initializable {
    public Button chats_btn;
    public Button contacts_btn;
    public Button logout_btn;
    public Button delete_btn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logout_btn.setOnAction(e -> logout());
        delete_btn.setOnAction(e-> deleteUser());
    }

    private void logout() {
        Model.getInstance().setLoggedInUser(null);
        Stage currentStage = (Stage) logout_btn.getScene().getWindow();
        Model.getInstance().getViewFactory().closeStage(currentStage);
        Model.getInstance().getViewFactory().showLoginWindow();
        Model.getInstance().getViewFactory().getClientSelectedChat().set(null);
        Model.getInstance().resetSessionState();
    }

    private void deleteUser() {
        JSONObject request = RequestFactory.createDeleteUserRequest();
        Model.getInstance().getMsgCtrl().getSendMessageQueue().offer(request);
        logout();
    }
}
