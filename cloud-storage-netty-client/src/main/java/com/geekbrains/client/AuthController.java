package com.geekbrains.client;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

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
    public void executeAuth() {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            showAlert("Fields must be filled");
            return;
        }
        Network.getInstance().sendAuthMessage(login, password);
    }

    public void registration() {
        App.INSTANCE.getRegStage().show();
    }

    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Вспомогательные методы для удобства навигации между полями
    public void loginFocus() {
        loginField.requestFocus();
    }

    public void submitLogin() {
        passwordField.requestFocus();
    }

    public void goFromLogin(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case UP -> regButton.requestFocus();
            case DOWN -> passwordField.requestFocus();
        }
    }

    public void goFromPassword(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case UP -> loginField.requestFocus();
            case DOWN -> authButton.requestFocus();
        }
    }

    public void goFromEnter(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case UP -> passwordField.requestFocus();
            case DOWN, LEFT -> regButton.requestFocus();
        }
    }

    public void goFromReg(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case UP -> passwordField.requestFocus();
            case DOWN -> loginField.requestFocus();
            case RIGHT -> authButton.requestFocus();
        }
    }
}
