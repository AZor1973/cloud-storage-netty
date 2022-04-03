package com.geekbrains.client.listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionEvent implements Event {
    private final List<ConnectionListener> connectionListeners;
    private boolean isConnect;

    public ConnectionEvent() {
        connectionListeners = new CopyOnWriteArrayList<>();
    }

    public synchronized void setConnection(boolean isConnect) {
        this.isConnect = isConnect;
        notifyConnectionListener();
    }

    @Override
    public synchronized void registerConnectionListener(ConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    @Override
    public synchronized void notifyConnectionListener() {
        for (ConnectionListener connectionListener : connectionListeners)
            connectionListener.update(isConnect);
    }
}
