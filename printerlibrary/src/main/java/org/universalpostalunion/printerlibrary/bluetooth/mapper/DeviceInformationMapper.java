package org.universalpostalunion.printerlibrary.bluetooth.mapper;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.universalpostalunion.printerlibrary.bluetooth.model.DeviceInformation;
import org.universalpostalunion.printerlibrary.bluetooth.model.DeviceType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mapper
public abstract class DeviceInformationMapper {

    private HashMap<DeviceType, BluetoothSocket> socketMap;

    public void setCurrentSockets(HashMap<DeviceType, BluetoothSocket> bluetoothSockets) {
        this.socketMap = bluetoothSockets;
    }

    public abstract Set<DeviceInformation> mapSet(Set<BluetoothDevice> devices);

    public DeviceInformation map(BluetoothDevice device) {
        return map(BluetoothDeviceWrapper.builder().device(device).build());
    }

    protected DeviceType mapToType(BluetoothClass bluetoothClass) {
        return DeviceType.fromCode(bluetoothClass.getDeviceClass());
    }

    @Mapping(target = "device", source = "device")
    @Mapping(target = "connected", ignore = true)
    @Mapping(target = "deviceType", source = "device.bluetoothClass")
    abstract DeviceInformation map(BluetoothDeviceWrapper deviceWrapper);

    @AfterMapping
    protected void afterMapping(@MappingTarget DeviceInformation target, BluetoothDeviceWrapper bluetoothDevice) {
        if (socketMap != null && socketMap.size() > 0) {
            for (Map.Entry<DeviceType, BluetoothSocket> currentConsideredEntry : socketMap.entrySet()) {
                final BluetoothSocket socket = currentConsideredEntry.getValue();

                if (socket.getRemoteDevice().getAddress().equals(target.getAddress())) {
                    target.setConnected(socket.isConnected());
                } else {
                    target.setConnected(Boolean.FALSE);
                }
            }
        }
    }

}
