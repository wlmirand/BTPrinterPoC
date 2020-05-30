package com.example.daggerapplication.services.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.example.daggerapplication.services.bluetooth.mapper.DeviceInformationMapper;
import com.example.daggerapplication.services.bluetooth.model.BluetoothState;
import com.example.daggerapplication.services.bluetooth.model.BondState;
import com.example.daggerapplication.services.bluetooth.model.DeviceInformation;
import com.example.daggerapplication.services.bluetooth.model.DeviceType;
import com.example.daggerapplication.services.bluetooth.receiver.BroadcastReceiverObservable;

import org.mapstruct.factory.Mappers;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

@Singleton
public class BluetoothService {

    private final Context appContext;
    private final BluetoothAdapter btAdapter;
    private final ConnectionManager connectionManager;
    private final DeviceInformationMapper deviceInformationMapper;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    BluetoothService(Context context, ConnectionManager connectionManager) {
        this.appContext = context;
        this.connectionManager = connectionManager;
        this.deviceInformationMapper = Mappers.getMapper(DeviceInformationMapper.class);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////// RELATES TO BLUETOOTH

    /**
     * @return true if the bluetooth is available on the current device.
     */
    public boolean isAvailable() {
        return btAdapter != null;
    }

    /**
     * @return true if the bluetooth is activated.
     */
    public boolean isActivated() {
        return isAvailable() && btAdapter.isEnabled();
    }

    /**
     * Activate the bluetooth.<br>
     *
     * @return Observable on Bluetooth State (disposable).
     */
    public Observable<BluetoothState> activate() {
        if (isAvailable() && !isActivated()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            appContext.startActivity(enableBtIntent);
            return BroadcastReceiverObservable.create(appContext, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
                    .filter(intent -> intent.getAction() != null && intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED))
                    .map(intent -> intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR))
                    .map(code -> BluetoothState.fromCode(code));
        }
        return Observable.just(BluetoothState.ON).subscribeOn(Schedulers.io());
    }

    /**
     * @return Observable on bonded Devices Information (disposable)
     */
    public Observable<Set<DeviceInformation>> getDevicesInformation() {
        final Observable<Set<DeviceInformation>> observerCurrentSockets = connectionManager.getBluetoothSockets()
                .map(bluetoothSockets -> {
                    final Set<BluetoothDevice> bondedDevices = btAdapter.getBondedDevices();
                    deviceInformationMapper.setCurrentSockets(bluetoothSockets);
                    return deviceInformationMapper.mapSet(bondedDevices);
                });

        final IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        final Observable<Intent> observerExternalEvents =
                BroadcastReceiverObservable.create(appContext, intentFilter);

        return Observable.combineLatest(observerCurrentSockets, observerExternalEvents,
                (devicesInfo, intent) -> {
                    // inspect what appears externally
                    final String action = intent.getAction();
                    if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        final BondState state = BondState.fromCode(intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1));
                        switch (state) {
                            case BOND_BONDED:
                                final DeviceInformation newBonded = deviceInformationMapper.map(device);
                                devicesInfo.add(newBonded);
                                break;
                            case BOND_NONE:
                                final DeviceInformation newNotBonded = deviceInformationMapper.map(device);
                                devicesInfo.remove(newNotBonded);
                        }
                    }
                    return devicesInfo;
                }).subscribeOn(Schedulers.io());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// DELEGATED TO CONNECTION MANAGER

    /**
     * Select a device for a type of device.
     *
     * @param device     the device on select and connect / unselect and eventually disconnect.
     * @param deviceType the device type.
     * @param isToSelect true to select device / false to unselect.
     * @return the connection information.
     */
    public Observable<Boolean> selectUnselectAndConnect(DeviceInformation device, DeviceType deviceType, boolean isToSelect) {
        return connectionManager.selectUnselectDevice(device.getAddress(), deviceType, isToSelect);
    }

    /**
     * Retrieve the current socket for the requested device type.
     *
     * @param deviceType A device Type.
     * @return the active Socket.
     */
    public Observable<BluetoothSocket> getConnectionSocketFor(DeviceType deviceType) {
        return connectionManager.getConnectionSocketFor(deviceType);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
