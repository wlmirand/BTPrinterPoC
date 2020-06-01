package org.universalpostalunion.printerlibrary.dagger;

import android.bluetooth.BluetoothAdapter;

import org.mapstruct.factory.Mappers;
import org.universalpostalunion.printerlibrary.bluetooth.mapper.DeviceInformationMapper;

import dagger.Module;
import dagger.Provides;

@Module
public class BluetoothModule {

    @Provides
    static BluetoothAdapter provideBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    @Provides
    static DeviceInformationMapper provideDeviceInformationMapper() {
        return Mappers.getMapper(DeviceInformationMapper.class);
    }
}
