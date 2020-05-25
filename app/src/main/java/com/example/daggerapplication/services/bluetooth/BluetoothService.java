package com.example.daggerapplication.services.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BluetoothService {

    private final Context appContext;
    private final BluetoothAdapter btAdapter;
    private final ConnectionManager connectionManager;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    BluetoothService(Context context, ConnectionManager connectionManager) {
        this.appContext = context;
        this.connectionManager = connectionManager;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @return true if the bluetooth is available on the current device.
     */
    public boolean isAvailable() {
        return btAdapter != null;
    }

    /**
     * @return true if the bluetooth is activated.
     */
    public boolean isActivated() {
        return isAvailable() && btAdapter.isEnabled();
    }

    /**
     * Activate the bluetooth.<br>
     * <i>Once done, the observers of the current service will be notified (Notifications.STATUS)</i>
     */
    public void activate() {
        if (isAvailable() && !isActivated()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            appContext.startActivity(enableBtIntent);
        }
    }

    /**
     * @return Devices Information
     */
    public Set<BluetoothDevice> getBondedDevices() {
        return btAdapter.getBondedDevices();
    }

    /**
     * Connect to Device
     * @param device the device on which to connect.
     * @param deviceType for near future use.
     */
    public void connect(BluetoothDevice device, DeviceType deviceType) {
        connectionManager.connect(device);
    }

    /**
     * Connect to device
     * @param address The device MAC Address
     * @param deviceType for near future use.
     */
    public void connect(String address, DeviceType deviceType) {
        connectionManager.connect(btAdapter.getRemoteDevice(address.getBytes()));
    }

    public void registerOnConnectionNotifications(ConnectionObserver observer) {
        connectionManager.addObserver(observer);
    }
}
