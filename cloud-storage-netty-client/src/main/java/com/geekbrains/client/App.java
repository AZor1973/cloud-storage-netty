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
    private static final String MAIN_FXML = "main.fxml";
    private static final String AUTH_FXML = "auth.fxml";
    private static final String REG_FXML = "reg.fxml";
    private Stage primaryStage;
    private Stage authStage;
    private Stage regStage;
    private FXMLLoader mainLoader;  // для создания getMainController(ниже)
    private FXMLLoader authLoader;  // --
    private FXMLLoader regLoader;  // --

    @Override
    public void init() {
        INSTANCE = this;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        initViews();
        authStage.show();
    }

    private void initViews() throws IOException {
        initMainWindow();
        initAuthWindow();
        initRegWindow();
    }

    private void initMainWindow() throws IOException {
        mainLoader = new FXMLLoader();
        mainLoader.setLocation(getClass().getResource(MAIN_FXML));
        Parent root = mainLoader.load();
        this.primaryStage.setScene(new Scene(root));
        primaryStage.getScene().getStylesheets().add("com/geekbrains/client/sky.css");
        primaryStage.setOnCloseRequest(we -> Network.getInstance().close());
    }

    void initAuthWindow() throws IOException {
        authLoader = new FXMLLoader();
        authLoader.setLocation(getClass().getResource(AUTH_FXML));
        Parent root = authLoader.load();
        authStage = new Stage();
        authStage.initOwner(primaryStage);
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.setScene(new Scene(root));
        authStage.getScene().getStylesheets().add("com/geekbrains/client/sky.css");
        authStage.setResizable(false);
        getAuthController().loginFocus();  // фокус на поле логина (для удобства)
        authStage.setOnCloseRequest(we -> Network.getInstance().close());
    }

    private void initRegWindow() throws IOException {
        regLoader = new FXMLLoader();
        regLoader.setLocation(getClass().getResource(REG_FXML));
        Parent root = regLoader.load();
        regStage = new Stage();
        regStage.initOwner(authStage);
        regStage.initModality(Modality.WINDOW_MODAL);
        regStage.setScene(new Scene(root));
        regStage.getScene().getStylesheets().add("com/geekbrains/client/sky.css");
        regStage.setResizable(false);
        getRegController().nickFocus(); // фокус на поле имени (для удобства)
    }

    public void switchToMainWindow(String username) {
        primaryStage.show();
        primaryStage.setTitle(username);
        authStage.close();
    }

    // для доступа к контроллеру из класса Network
    public MainController getMainController() {
        return mainLoader.getController();
    }

    private AuthController getAuthController() {
        return authLoader.getController();
    }

    public RegController getRegController() {
        return regLoader.getController();
    }

    public Stage getAuthStage() {
        return authStage;
    }

    public Stage getRegStage() {
        return regStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}