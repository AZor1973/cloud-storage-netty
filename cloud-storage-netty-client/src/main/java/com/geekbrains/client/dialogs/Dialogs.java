package com.geekbrains.client.dialogs;

import com.geekbrains.client.App;
import javafx.scene.control.Alert;

public class Dialogs {

    public enum AuthError {
        EMPTY_CREDENTIALS("Login and password must be specified!"),
        INVALID_CREDENTIALS("Incorrect login or password!"),
        USERNAME_BUSY("This user is already signed in!")
        ;

        private static final String TITLE = "Authentication error";
        private static final String TYPE = TITLE;
        private final String message;

        AuthError(String message) {
            this.message = message;
        }

        public void show() {
            showDialog(TITLE, TYPE, message);
        }
    }

    public enum NetworkError {
        SERVER_CONNECT("Failed to establish a connection to the server!")
        ;

        private static final String TITLE = "Network error";
        private static final String TYPE = "Network communication error";
        private final String message;

        NetworkError(String message) {
            this.message = message;
        }
        public void show() {
            showDialog(TITLE, TYPE, message);
        }

    }

    private static void showDialog(String title, String type, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(App.INSTANCE.getPrimaryStage());
        alert.setTitle(title);
        alert.setHeaderText(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

}


