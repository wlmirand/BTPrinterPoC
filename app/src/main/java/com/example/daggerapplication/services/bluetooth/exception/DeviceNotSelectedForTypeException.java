package com.example.daggerapplication.services.bluetooth.exception;

import com.example.daggerapplication.services.bluetooth.model.DeviceType;

public class DeviceNotSelectedForTypeException extends Exception {

    private final DeviceType deviceType;

    public DeviceNotSelectedForTypeException(DeviceType deviceType) {
        super("No Device Selected for Type: " + deviceType);
        this.deviceType = deviceType;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }
}
