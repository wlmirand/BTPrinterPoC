package org.universalpostalunion.printerlibrary.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.universalpostalunion.printerlibrary.bluetooth.exception.BluetoothException;
import org.universalpostalunion.printerlibrary.bluetooth.mapper.DeviceInformationMapper;
import org.universalpostalunion.printerlibrary.bluetooth.model.BluetoothState;
import org.universalpostalunion.printerlibrary.bluetooth.model.BondState;
import org.universalpostalunion.printerlibrary.bluetooth.model.DeviceInformation;
import org.universalpostalunion.printerlibrary.bluetooth.receiver.BroadcastReceiverObservable;
import org.universalpostalunion.printerlibrary.common.CompositeDisposable;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Singleton
public class BluetoothService {

    private final static String TAG_LOG = BluetoothService.class.getSimpleName();

    private final Context appContext;
    private final BluetoothAdapter btAdapter;
    private final ConnectionManager connectionManager;
    private final DeviceInformationMapper deviceInformationMapper;
    private Disposable activationDisposable;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    BluetoothService(Context context, ConnectionManager connectionManager, BluetoothAdapter btAdapter, DeviceInformationMapper deviceInformationMapper) {
        this.appContext = context;
        this.connectionManager = connectionManager;
        this.deviceInformationMapper = deviceInformationMapper;
        this.btAdapter = btAdapter;
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

    private boolean activate() {
        if (isAvailable() && !isActivated()) {
            if (activationDisposable != null) {
                CompositeDisposable.clearDisposable(activationDisposable);
            }
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            appContext.startActivity(enableBtIntent);
            activationDisposable = BroadcastReceiverObservable.create(appContext, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
                    .filter(intent -> intent.getAction() != null && intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED))
                    .map(intent -> intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR))
                    .map(code -> BluetoothState.fromCode(code))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(bluetoothState -> Log.i(TAG_LOG, "Bluetooth Activated")
                            , throwable -> Log.e(TAG_LOG, "Error on bluetooth activation"));
            CompositeDisposable.add(activationDisposable);
        }
        return isActivated();
    }

    /**
     * @return Observable on bonded Devices Information (disposable)
     */
    public Observable<Set<DeviceInformation>> getDevicesInformation() {

        if (!isAvailable()) {
            return Observable.error(new BluetoothException("Bluetooth is not Available on this device"));
        }

        if (!isActivated() && !activate()) {
            return Observable.error(new BluetoothException("Bluetooth fails to activate"));
        }

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

    public void clean() {
        CompositeDisposable.clear();
        Log.i(TAG_LOG, "Bean cleaned");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// DELEGATED TO CONNECTION MANAGER

    /**
     * Retrieve the current socket for the requested device type.
     *
     * @param device the device.
     * @return the active Socket.
     */
    public Observable<BluetoothSocket> getConnectionSocketFor(DeviceInformation device) {

        if (!isAvailable()) {
            return Observable.error(new BluetoothException("Bluetooth is not Available on this device"));
        }

        if (!isActivated() && !activate()) {
            return Observable.error(new BluetoothException("Bluetooth fails to activate"));
        }

        return connectionManager.getConnectionSocketFor(device);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
