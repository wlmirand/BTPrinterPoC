package org.universalpostalunion.printerlibrary.bluetooth.mapper;


import android.bluetooth.BluetoothDevice;

import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
class BluetoothDeviceWrapper {
    private BluetoothDevice device;
}