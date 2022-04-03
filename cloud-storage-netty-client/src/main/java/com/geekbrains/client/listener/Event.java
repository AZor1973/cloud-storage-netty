package com.geekbrains.client.listener;

public interface Event {
    void registerConnectionListener(ConnectionListener connectionListener);

    void notifyConnectionListener();
}
