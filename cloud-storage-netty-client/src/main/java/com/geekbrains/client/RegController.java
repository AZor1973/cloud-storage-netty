package com.geekbrains.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class RegController {
    @FXML
    public Button submitRegButton;
    @FXML
    public TextField loginFieldReg;
    @FXML
    public PasswordField passwordFieldReg;
    @FXML
    public TextField nickField;

    public void submitRegistration() {
        String login = loginFieldReg.getText();
        char[] password = passwordFieldReg.getText().toCharArray();
        String username = nickField.getText();
        if (login == null || login.isBlank() || password.length == 0) {
            App.INSTANCE.getMainController().showAlert("Fields must be filled", Alert.AlertType.ERROR);
            return;
        }
        Network.getInstance().sendRegMessage(username, login, password);
        App.INSTANCE.getRegStage().close();
    }

    // Вспомогательные методы для удобства навигации между полями

    public void nickFocus() {
        nickField.requestFocus();
    }

    public void submitNick() {
        loginFieldReg.requestFocus();
    }

    public void submitLoginReg() {
        passwordFieldReg.requestFocus();
    }

    public void goFromNick(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.UP) {
            passwordFieldReg.requestFocus();
        } else if (keyEvent.getCode() == KeyCode.DOWN) {
            loginFieldReg.requestFocus();
        }
    }

    public void goFromLogin(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.UP) {
            nickField.requestFocus();
        } else if (keyEvent.getCode() == KeyCode.DOWN) {
            passwordFieldReg.requestFocus();
        }
    }

    public void goFromPassword(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.UP) {
            loginFieldReg.requestFocus();
        } else if (keyEvent.getCode() == KeyCode.DOWN) {
            nickField.requestFocus();
        }
    }
}
