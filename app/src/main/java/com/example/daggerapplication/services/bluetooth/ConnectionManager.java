package com.example.daggerapplication.services.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Observable;

import javax.inject.Inject;

public class ConnectionManager extends Observable {

    private static final String LOG_TAG = ConnectionManager.class.getSimpleName();
    private final Context appContext;
    // private final HashMap<BluetoothDevice, BluetoothSocket> connectedDevices = new HashMap<>();


    class ReceiveThread extends Thread implements Runnable {

        private final BluetoothSocket socket;

        ReceiveThread(BluetoothSocket socket) {
            this.socket = socket;
        }

        public void run() {
            String msg;
            try {
                BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while ((msg = inputStream.readLine()) != null) {
                    final String message = msg;
                    new android.os.Handler(Looper.getMainLooper()).post(() -> {
                        setChanged();
                        notifyObservers(ConnectionNotification.builder()
                                .message(message)
                                .socket(socket)
                                .notification(ConnectionNotification.Notification.RECEIVE_MESSAGE)
                                .build());
                    });
                }
            } catch (IOException e) {
                new android.os.Handler(Looper.getMainLooper()).post(() -> {
                    setChanged();
                    notifyObservers(ConnectionNotification.builder()
                            .message("The connection has been closed")
                            .socket(socket)
                            .notification(ConnectionNotification.Notification.DISCONNECTED)
                            .build());
                });
            }
        }
    }

    private class SocketConnectionThread extends Thread implements Runnable {
        private final BluetoothDevice device;
        private BluetoothSocket socket;

        SocketConnectionThread(BluetoothDevice device)  {
            this.device = device;
        }

        public void run() {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            try {
                socket = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
                socket.connect();
                new ReceiveThread(socket).start();
                new android.os.Handler(Looper.getMainLooper()).post(() -> {
                    setChanged();
                    notifyObservers(ConnectionNotification.builder()
                            .notification(ConnectionNotification.Notification.CONNECTED)
                            .message("Successfully connected on " + device.getName() + "[" + device.getAddress() + "]")
                            .socket(socket)
                            .build());
                });
            } catch (IOException connectException) {
                Log.e(LOG_TAG, "Unable to connect on device: " + device.getName() + "[" + device.getAddress() + "]");
                new android.os.Handler(Looper.getMainLooper()).post(() -> {
                    setChanged();
                    notifyObservers(ConnectionNotification.builder()
                            .notification(ConnectionNotification.Notification.CONNECTION_ERROR)
                            .message("Successfully connected on " + device.getName() + "[" + device.getAddress() + "]")
                            .socket(socket)
                            .build());
                });
            }
        }

    }

    @Inject
    ConnectionManager(Context appContext) {
        this.appContext = appContext;
    }


    void connect(BluetoothDevice device) {
        SocketConnectionThread connectionThread = new SocketConnectionThread(device);
        connectionThread.start();
    }


}
