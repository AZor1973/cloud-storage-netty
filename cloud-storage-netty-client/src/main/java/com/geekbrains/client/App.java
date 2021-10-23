package com.geekbrains.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {
    public static App INSTANCE;
    private static final String MAIN_WINDOW_FXML = "main.fxml";
    private static final String AUTH_FXML = "auth.fxml";
    private Stage primaryStage;
    private Stage authStage;
    private FXMLLoader mainLoader;
    private FXMLLoader authLoader;

    @Override
    public void init() {
        INSTANCE = this;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        initViews();
        getPrimaryStage().show();
        getAuthStage().show();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Stage getAuthStage() {
        return authStage;
    }

    private AuthController getAuthController() {
        return authLoader.getController();
    }

    public MainController getMainController() {
        return mainLoader.getController();
    }

    private void initViews() throws IOException {
        initMainWindow();
        initAuthWindow();
    }

    private void initMainWindow() throws IOException {
        mainLoader = new FXMLLoader();
        mainLoader.setLocation(App.class.getResource(MAIN_WINDOW_FXML));
        Parent root = mainLoader.load();
        this.primaryStage.setScene(new Scene(root));
    }


    private void initAuthWindow() throws IOException {
        authLoader = new FXMLLoader();
        authLoader.setLocation(App.class.getResource(AUTH_FXML));
        Parent authDialogPanel = authLoader.load();
        authStage = new Stage();
        authStage.initOwner(primaryStage);
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.setScene(new Scene(authDialogPanel));
        authStage.setOnCloseRequest(we -> primaryStage.close());
    }

    public void switchToMainChatWindow(String username) {
        getPrimaryStage().setTitle(username);
        getAuthStage().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}