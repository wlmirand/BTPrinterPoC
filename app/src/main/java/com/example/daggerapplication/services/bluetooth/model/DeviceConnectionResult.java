package com.example.daggerapplication.services.bluetooth.model;

import android.bluetooth.BluetoothSocket;

import com.example.daggerapplication.services.bluetooth.connection.SocketConnectionWithStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Delegate;

@Getter
@Builder
public class DeviceConnectionResult {

    private interface SocketConnectionWithStatusToExclude {
        String getDeviceName();
        String getDeviceAddress();
        BluetoothSocket getSocket();
        boolean isToKeep();
    }

    @Delegate
    private DeviceInformation deviceInformation;

    @Delegate(excludes = SocketConnectionWithStatusToExclude.class)
    private SocketConnectionWithStatus connectionStatus;

}
