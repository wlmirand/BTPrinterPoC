package com.example.daggerapplication.services.bluetooth;

import java.util.Observable;
import java.util.Observer;

public abstract class ConnectionObserver implements Observer {

    @Override
    public void update(Observable emitter, Object arguments) {
        if (emitter instanceof ConnectionManager && arguments instanceof ConnectionNotification) {
            update((ConnectionNotification)arguments);
        }
    }

    public abstract void update(ConnectionNotification connectionNotification);

}
