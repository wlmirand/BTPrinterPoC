package com.example.daggerapplication.services.bluetooth.mapper;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.example.daggerapplication.services.bluetooth.model.DeviceInformation;
import com.example.daggerapplication.services.bluetooth.model.DeviceType;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mapper
public abstract class DeviceInformationMapper {

    private HashMap<DeviceType, BluetoothSocket> socketMap;

    public void setCurrentSockets(HashMap<DeviceType,BluetoothSocket> bluetoothSockets) {
        this.socketMap = bluetoothSockets;
    }

    public abstract Set<DeviceInformation> mapSet(Set<BluetoothDevice> devices);

    public DeviceInformation map(BluetoothDevice device) {
        return map(BluetoothDeviceWrapper.builder().device(device).build());
    }

    @Mapping(target = "device", source = "device")
    @Mapping(target = "connected", ignore = true)
    @Mapping(target = "selectedDevice", ignore = true)
    abstract DeviceInformation map(BluetoothDeviceWrapper deviceWrapper);

    @AfterMapping
    protected void afterMapping(@MappingTarget DeviceInformation target, BluetoothDeviceWrapper bluetoothDevice) {
        if (socketMap != null && socketMap.size() > 0) {
            for (Map.Entry<DeviceType, BluetoothSocket> currentConsideredEntry : socketMap.entrySet()) {
                final BluetoothSocket socket = currentConsideredEntry.getValue();
                final DeviceType deviceType = currentConsideredEntry.getKey();

                if (socket.getRemoteDevice().getAddress().equals(target.getAddress())) {
                    target.setConnected(socket.isConnected());
                    target.setSelectedDevice(deviceType);
                } else {
                    target.setConnected(Boolean.FALSE);
                    target.setSelectedDevice(DeviceType.NONE);
                }
            }
        }
    }

}
