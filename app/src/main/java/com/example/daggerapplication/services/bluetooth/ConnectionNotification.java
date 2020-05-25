package com.example.daggerapplication.services.bluetooth;

import android.bluetooth.BluetoothSocket;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ConnectionNotification {

    public enum Notification {
        RECEIVE_MESSAGE,
        CONNECTED,
        DISCONNECTED,
        CONNECTION_ERROR
    }

    private Notification notification;
    private String message;
    private BluetoothSocket socket;
}
