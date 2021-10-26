package com.geekbrains.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController {
    @FXML
    public Button authButton;
    @FXML
    public Button regButton;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;

    @FXML
    public void executeAuth() throws IOException {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            Network.getInstance().show("Fields must be filled");
            return;
        }
        if (!Network.getInstance().isConnected()) {
            Network.getInstance().show("Network error");
            return;
        }
        Network.getInstance().sendAuthMessage(login, password);
    }

    public void submitLogin() {
        passwordField.requestFocus();
    }

    public void goToPassword(KeyEvent keyEvent) {
        if (keyEvent.getCode().isArrowKey()) {
            passwordField.requestFocus();
        }
    }

    public void goToLogin(KeyEvent keyEvent) {
        if (keyEvent.getCode().isArrowKey()) {
            loginField.requestFocus();
        }
    }

    public void registration() throws IOException {
        App.INSTANCE.initRegWindow();
    }

    public void loginFocus() {
        loginField.requestFocus();
    }
}
