package com.example.daggerapplication.services.bluetooth.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.daggerapplication.services.bluetooth.model.ConnectionStatus;

import java.io.IOException;
import java.util.UUID;

class OutgoingConnectionThread {
    private static final String LOG_TAG = "BT_OUTGOING_CNX";
    private final SocketConnectionWithStatus socketConnectionWithStatus;
    private final ConnectionManager connectionManager;
    private final BluetoothDevice device;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    OutgoingConnectionThread(ConnectionManager connectionManager, BluetoothDevice device, boolean keep) {
        this.connectionManager = connectionManager;
        this.device = device;
        this.socketConnectionWithStatus = SocketConnectionWithStatus.builder()
                .deviceName(device.getName())
                .deviceAddress(device.getAddress())
                .toKeep(keep)
                .build();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    void run() {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        BluetoothSocket socket = null;
        try {
            socket = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
            socket.connect();
        } catch (IOException connectException) {
            Log.e(LOG_TAG, "Error", connectException);
            try {
                if (socket!=null) socket.close();
            } catch (IOException closeException) {
                Log.e(LOG_TAG, "Could not close the client socket", closeException);
            }
        }

        final SocketConnectionWithStatus updatedSocketConnectionUpdated =
                socketConnectionWithStatus.toBuilder()
                        .socket(socket)
                        .status(socket == null || !socket.isConnected() ? ConnectionStatus.FAILURE : ConnectionStatus.SUCCESS)
                        .build();

        this.connectionManager.manageConnection(updatedSocketConnectionUpdated);

    }

}
