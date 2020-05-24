package com.example.daggerapplication.services.bluetooth.exception;

import android.bluetooth.BluetoothDevice;

public class DeviceAlreadyConnectedException extends Exception {

    private final String deviceName;
    private final String deviceAddess;

    public DeviceAlreadyConnectedException(BluetoothDevice device) {
        super("The Device " + device.getName() + "[" + device.getAddress() + "] is already connected");
        this.deviceName = device.getName();
        this.deviceAddess = device.getAddress();
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceAddess() {
        return deviceAddess;
    }
}
