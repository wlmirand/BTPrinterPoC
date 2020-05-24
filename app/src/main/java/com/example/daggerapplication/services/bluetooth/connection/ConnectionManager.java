package com.example.daggerapplication.services.bluetooth.connection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import com.example.daggerapplication.initialization.AppIdentifierUtil;
import com.example.daggerapplication.services.bluetooth.exception.DeviceAlreadyConnectedException;
import com.example.daggerapplication.services.bluetooth.model.ConnectionStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.UUID;

import javax.inject.Inject;

public class ConnectionManager extends Observable {

    private static final String LOG_TAG = "ConnectionManager";
    private final Context context;
    private final Map<String, BluetoothSocket> socketsMap = new HashMap<>();
    private final ArrayList<SocketConnectionWithStatus> returnedResult = new ArrayList<>();

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public ConnectionManager(Context context) {
        this.context = context;
        IncomingConnectionThread incomingConnectionThread = new IncomingConnectionThread(this);
        incomingConnectionThread.run();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public synchronized SocketConnectionWithStatus connect(BluetoothDevice device, boolean keep) throws DeviceAlreadyConnectedException {
        final String deviceKey = device.getName()+device.getAddress();
        if (socketsMap.containsKey(deviceKey)) {
            throw new DeviceAlreadyConnectedException(device);
        }
        OutgoingConnectionThread connectionThread = new OutgoingConnectionThread(this, device, keep);
        connectionThread.run();

        while (returnedResult.isEmpty());

        return returnedResult.remove(0);
    }

    public BluetoothSocket getSocketForDevice(BluetoothDevice device) throws DeviceAlreadyConnectedException {
        final String deviceKey = device.getName()+device.getAddress();
        if (socketsMap.containsKey(deviceKey)) {
            final BluetoothSocket socket = socketsMap.get(deviceKey);
            if (socket.isConnected()) {
                return socket;
            }
            socketsMap.remove(socket);
        }
        return null;
    }

    UUID getUUID() {
        return AppIdentifierUtil.id(context);
    }


    void manageConnection(SocketConnectionWithStatus connectionWithStatus) {
        if (connectionWithStatus.getStatus() == ConnectionStatus.SUCCESS) {
            final BluetoothSocket socket = connectionWithStatus.getSocket();
            final BluetoothDevice device = socket.getRemoteDevice();
            final String deviceKey = device.getName()+device.getAddress();
            if (connectionWithStatus.isToKeep()) {
                if (socketsMap.containsKey(deviceKey)) {
                    try(BluetoothSocket oldSocket = socketsMap.get(deviceKey)){

                        if (oldSocket.isConnected()) {
                            oldSocket.close();
                        }
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Unable to close safely the connection on : " + socket.getRemoteDevice().getName() + "[" + socket.getRemoteDevice().getAddress() + "]");
                    }
                }
                socketsMap.put(deviceKey, socket);
            } else {
                // close directly the socket
                try {
                    socket.close();
                } catch(Exception e) {
                    Log.e(LOG_TAG, "Unable to close safely the connection on : " + socket.getRemoteDevice().getName() + "[" + socket.getRemoteDevice().getAddress() + "]");
                }
            }
        }
        returnedResult.add(connectionWithStatus);
    }

}
