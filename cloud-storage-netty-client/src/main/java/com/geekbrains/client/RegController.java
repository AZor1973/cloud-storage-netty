package com.geekbrains.client;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;

public class RegController {
    @FXML
    public Button submitRegButton;
    @FXML
    public TextField loginFieldReg;
    @FXML
    public PasswordField passwordFieldReg;
    @FXML
    public TextField nickField;

    public void submitRegistration() throws IOException {
        String login = loginFieldReg.getText();
        String password = passwordFieldReg.getText();
        String username = nickField.getText();
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            showAlert("Fields must be filled");
            return;
        }
        Network.getInstance().sendRegMessage(username, login, password);
        App.INSTANCE.getRegStage().close();
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

    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void nickFocus(){
        nickField.requestFocus();
    }
}
