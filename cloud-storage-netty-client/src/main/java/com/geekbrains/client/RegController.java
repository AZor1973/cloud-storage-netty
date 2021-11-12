package com.geekbrains.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class RegController implements Initializable {
    @FXML
    public Button submitRegButton;
    @FXML
    public TextField loginFieldReg;
    @FXML
    public PasswordField passwordFieldReg;
    @FXML
    public TextField nickField;
    @FXML
    public CheckBox rememberMeReg;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rememberMeReg.setOnAction(event -> {
            App.INSTANCE.getAuthController().rememberMe.setSelected(rememberMeReg.isSelected());
            App.INSTANCE.getMainController().rememberMeMenuItem.setSelected(rememberMeReg.isSelected());
        });
    }

    public void submitRegistration() {
        String login = loginFieldReg.getText();
        char[] password = passwordFieldReg.getText().toCharArray();
        String username = nickField.getText();
        if (login == null || login.isBlank() || password.length == 0) {
            App.INSTANCE.getMainController().showAlert("Fields must be filled", Alert.AlertType.ERROR);
            return;
        }
        if (rememberMeReg.isSelected()) {
            App.INSTANCE.getAuthController().setLoginField(login);
            App.INSTANCE.getAuthController().setPasswordField(new String((password)));
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
