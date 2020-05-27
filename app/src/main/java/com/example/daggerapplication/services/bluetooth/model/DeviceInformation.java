package com.example.daggerapplication.services.bluetooth.model;

import android.bluetooth.BluetoothDevice;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DeviceInformation {

    interface IncludedInformation {
        String getName();
        String getAddress();
    }

    @Setter
    private boolean isConnected;

    @Setter
    private DeviceType selectedDevice;

    @Getter(AccessLevel.NONE)
    @Setter
    @Delegate(types = IncludedInformation.class)
    private BluetoothDevice device;


    public BondState getBondState() {
        return BondState.fromCode(device.getBondState());
    }

}
