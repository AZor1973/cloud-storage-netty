package com.geekbrains.client;

import com.geekbrains.client.dialogs.Dialogs;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import java.io.IOException;

public class AuthController {
    @FXML
    public Button authButton;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;

    @FXML
    public void executeAuth() throws IOException {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            Dialogs.AuthError.EMPTY_CREDENTIALS.show();
            return;
        }
        if (!Network.getInstance().isConnected()) {
            Dialogs.NetworkError.SERVER_CONNECT.show();
            return;
        }
            Network.getInstance().sendAuthMessage(login, password);
    }

    public void submitLogin() {
        passwordField.requestFocus();
    }

    public void goToPassword(KeyEvent keyEvent) {
        if (keyEvent.getCode().isArrowKey()){
            passwordField.requestFocus();
        }
    }

    public void goToLogin(KeyEvent keyEvent) {
        if (keyEvent.getCode().isArrowKey()){
            loginField.requestFocus();
        }
    }
}
