package com.example.daggerapplication.services.bluetooth.exception;

public class DeviceNotFoundException extends Exception {

    private String deviceKey;

    public DeviceNotFoundException(String deviceKey) {
        super("The Device " + deviceKey + " has not been found");
    }

    public String getDeviceKey() {
        return deviceKey;
    }
}
