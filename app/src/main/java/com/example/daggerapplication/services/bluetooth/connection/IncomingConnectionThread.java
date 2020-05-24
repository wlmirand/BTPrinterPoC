package com.example.daggerapplication.services.bluetooth.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.daggerapplication.services.bluetooth.model.ConnectionStatus;

import java.io.IOException;

class IncomingConnectionThread {

    private static final String SERVER_SOCKET_NAME = "APP_SERVER_SOCKET_NAME";
    private static final String LOG_TAG = "BT_INCOMING_CNX";
    private final BluetoothServerSocket mmServerSocket;
    private final ConnectionManager connectionManager;


    IncomingConnectionThread(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        try (BluetoothServerSocket tmpServerSocket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(SERVER_SOCKET_NAME, connectionManager.getUUID())) {
            // MY_UUID is the app's UUID string, also used by the client code.
            mmServerSocket = tmpServerSocket;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to initialize connection listener", e);
            throw new RuntimeException("IncomingConnectionThread fails to initialize for incoming request managing");
        }
    }

    void run() {
        while (true) {
            try (BluetoothSocket socket = mmServerSocket.accept()) {
                final BluetoothDevice device = socket.getRemoteDevice();
                final SocketConnectionWithStatus socketWithConnectionStatus =
                        SocketConnectionWithStatus.builder()
                                .deviceName(device.getName())
                                .deviceAddress(device.getAddress())
                                .status(ConnectionStatus.SUCCESS)
                                .socket(socket)
                                .toKeep(true)
                                .build();

                connectionManager.manageConnection(socketWithConnectionStatus);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Socket's accept() method failed", e);
                break;
            }
        }
    }

    void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not close the connect socket", e);
        }
    }
}
