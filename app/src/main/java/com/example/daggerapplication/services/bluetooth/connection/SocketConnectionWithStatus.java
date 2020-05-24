package com.example.daggerapplication.services.bluetooth.connection;

import android.bluetooth.BluetoothSocket;

import com.example.daggerapplication.services.bluetooth.model.ConnectionStatus;

import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
public class SocketConnectionWithStatus {
    private ConnectionStatus status;
    private String deviceName;
    private String deviceAddress;
    private BluetoothSocket socket;
    private boolean toKeep;
}
