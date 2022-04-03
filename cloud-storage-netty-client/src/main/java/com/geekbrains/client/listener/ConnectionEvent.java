package com.geekbrains.client.listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionEvent implements Event {
    private final List<Listener> listeners;
    private boolean isConnect;

    public ConnectionEvent() {
        listeners = new CopyOnWriteArrayList<>();
    }

    public synchronized void setConnection(boolean isConnect) {
        this.isConnect = isConnect;
        notifyListener();
    }

    @Override
    public synchronized void registerListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public synchronized void notifyListener() {
        for (Listener listener : listeners)
            listener.update(isConnect);
    }
}
