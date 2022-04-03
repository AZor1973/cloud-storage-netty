package com.geekbrains.client.listener;

public interface Event {
    void registerListener(Listener Listener);

    void notifyListener();
}
