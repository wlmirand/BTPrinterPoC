package org.universalpostalunion.printerlibrary.bluetooth.model;

import android.bluetooth.BluetoothDevice;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(doNotUseGetters = true)
public class DeviceInformation {

    interface IncludedInformation {
        String getName();
        String getAddress();
    }

    @EqualsAndHashCode.Exclude
    @Setter
    private boolean isConnected;

    @EqualsAndHashCode.Exclude
    @Setter
    private DeviceType deviceType;


    @Getter(AccessLevel.NONE)
    @Setter
    @Delegate(types = IncludedInformation.class)
    private BluetoothDevice device;


}
