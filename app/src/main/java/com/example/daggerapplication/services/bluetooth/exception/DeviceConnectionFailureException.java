package com.example.daggerapplication.services.bluetooth.exception;

import android.bluetooth.BluetoothDevice;

public class DeviceConnectionFailureException extends Exception {

    private final String deviceName;
    private final String deviceAddress;

    public DeviceConnectionFailureException(BluetoothDevice device) {
        super("Fails to connect on device : " + device.getName() + "[" + device.getAddress() + "]");
        this.deviceName = device.getName();
        this.deviceAddress = device.getAddress();
    }

    public DeviceConnectionFailureException() {
        this.deviceName = "Unknown";
        this.deviceAddress = "Unknown";
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }
}
