package com.geekbrains.client.listener;

import com.geekbrains.client.App;
import com.geekbrains.client.MainController;
import com.geekbrains.client.Network;
import javafx.application.Platform;

public class ConnectionListener implements Listener{

    public ConnectionListener(Event event) {
        event.registerListener(this);
    }

    public synchronized void update(boolean isConnect) {
        if (isConnect) {
            Platform.runLater(() -> App.INSTANCE.getMainController().setConnectLabel(Network.CONNECTION_ESTABLISHED_STRING));
        } else {
            Platform.runLater(() -> App.INSTANCE.getMainController().setConnectLabel(MainController.CONNECTION_DISABLED_STRING));
        }
    }
}