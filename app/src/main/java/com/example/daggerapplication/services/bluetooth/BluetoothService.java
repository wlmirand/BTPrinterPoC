package com.example.daggerapplication.services.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.example.daggerapplication.services.bluetooth.broadcast.RxBroadcastReceiver;
import com.example.daggerapplication.services.bluetooth.connection.ConnectionManager;
import com.example.daggerapplication.services.bluetooth.connection.SocketConnectionWithStatus;
import com.example.daggerapplication.services.bluetooth.exception.DeviceAlreadyConnectedException;
import com.example.daggerapplication.services.bluetooth.exception.DeviceConnectionFailureException;
import com.example.daggerapplication.services.bluetooth.exception.DeviceNotFoundException;
import com.example.daggerapplication.services.bluetooth.exception.DeviceNotSelectedForTypeException;
import com.example.daggerapplication.services.bluetooth.model.BondedState;
import com.example.daggerapplication.services.bluetooth.model.ConnectionStatus;
import com.example.daggerapplication.services.bluetooth.model.DeviceConnectionResult;
import com.example.daggerapplication.services.bluetooth.model.DeviceInformation;
import com.example.daggerapplication.services.bluetooth.model.DeviceType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;

@Singleton
public class BluetoothService {

    private final Context appContext;
    private final BluetoothAdapter btAdapter;
    private final HashMap<String, BluetoothDevice> devicesMap = new HashMap<>();
    private final HashMap<DeviceType, BluetoothDevice> selectedDevicesByType = new HashMap<>();
    private HashMap<BluetoothDevice, SocketConnectionWithStatus> mostRecentConnectionByDevice = new HashMap<>();
    private final ConnectionManager connectionManager;


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    BluetoothService(Context context, ConnectionManager connectionManager) {
        this.appContext = context;
        this.connectionManager = connectionManager;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @return true if the bluetooth is available on the current device.
     */
    public Boolean isAvailable() {
        return btAdapter != null;
    }

    /**
     * @return true if the bluetooth is activated.
     */
    public Boolean isActivated() {
        return isAvailable() && btAdapter.isEnabled();
    }

    /**
     * Activate the bluetooth.<br>
     * <i>Once done, the observers of the current service will be notified (Notifications.STATUS)</i>
     */
    public boolean activate() {
        if (isAvailable() && !isActivated()) {
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            final Observable<Boolean> stateObservable = RxBroadcastReceiver.create(appContext, filter)
                    .map(intent -> {
                        final String action = intent.getAction();
                        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                            final int status = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                            return Boolean.valueOf(BluetoothAdapter.STATE_ON == status || BluetoothAdapter.STATE_TURNING_ON == status);
                        }
                        return null;
                    }).filter(aBoolean -> aBoolean != null);
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            appContext.startActivity(enableBtIntent);
            return stateObservable.blockingFirst();
        }
        return false;
    }

    /**
     * Search devices.<br>
     * <ul>Retrieves first already bounded devices</ul>
     * <ul>Lauch an asynchronous scanning for device discovery</ul>
     * <i>Once done, the observers of the current service will be notified. (Notifications.DEVICES)</i>
     *
     * @return
     */
    public Single<HashSet<DeviceInformation>> getDevicesInformation() {
        final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);

        // first, retrieves the already bounded devices
        final Observable<DeviceInformation> bondedDeviceObservable = Observable.fromIterable(btAdapter.getBondedDevices())
                .map(bluetoothDevice -> {
                    final String deviceKey = computeDeviceKey(bluetoothDevice);
                    devicesMap.put(deviceKey, bluetoothDevice);
                    return DeviceInformation.builder()
                            .key(deviceKey)
                            .name(bluetoothDevice.getName())
                            .address(bluetoothDevice.getAddress())
                            .boundedStatus(BondedState.fromCode(bluetoothDevice.getBondState()))
                            .selectedForDeviceType(getSelectedForDeviceType(bluetoothDevice))
                            .build();
                });

        final Single<HashSet<DeviceInformation>> notBondedDevicesObservable = RxBroadcastReceiver.create(appContext, filter, BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                .map(intent -> {
                    final String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action) || BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device == null) {
                            return null;
                        }
                        final String deviceKey = computeDeviceKey(device);
                        devicesMap.put(deviceKey, device);
                        return DeviceInformation.builder()
                                .key(deviceKey)
                                .name(device.getName())
                                .address(device.getAddress())
                                .boundedStatus(BondedState.fromCode(device.getBondState()))
                                .selectedForDeviceType(getSelectedForDeviceType(device))
                                .build();
                    }
                    return null;
                })
                .filter(deviceInformation -> deviceInformation != null)
                .concatWith(bondedDeviceObservable)
                .collect(HashSet::new, HashSet::add);
        // Discover not bounded
        btAdapter.startDiscovery();

        return notBondedDevicesObservable;
    }

    /**
     * Select a particular Device for a type as (PRINTER, BAR_CODE ....)<br>
     * When a
     *
     * @param deviceKey
     * @param deviceType
     * @throws DeviceNotFoundException
     */
    public DeviceConnectionResult selectForType(String deviceKey, DeviceType deviceType) throws DeviceNotFoundException, DeviceConnectionFailureException {
        if (devicesMap.containsKey(deviceKey)) {
            final BluetoothDevice device = devicesMap.get(deviceKey);

            try {
                SocketConnectionWithStatus status = connectionManager.connect(device, false);

                if (status.getStatus() == ConnectionStatus.SUCCESS) {
                    selectedDevicesByType.put(deviceType, device);
                }

                final DeviceInformation deviceInformation = DeviceInformation.builder()
                        .key(deviceKey)
                        .name(device.getName())
                        .address(device.getAddress())
                        .boundedStatus(BondedState.fromCode(device.getBondState()))
                        .selectedForDeviceType(getSelectedForDeviceType(device))
                        .build();

                return DeviceConnectionResult.builder()
                        .connectionStatus(status)
                        .deviceInformation(deviceInformation)
                        .build();
            } catch (DeviceAlreadyConnectedException e) {
                // Should Never Appear
                e.printStackTrace();
            }
        } else {
            throw new DeviceNotFoundException(deviceKey);
        }
        throw new RuntimeException();
    }

    /**
     * Pair devices
     *
     * @param deviceKey
     */
    public Single<DeviceInformation> pair(String deviceKey) throws DeviceConnectionFailureException {
        try {
            if (devicesMap.containsKey(deviceKey)) {
                final BluetoothDevice device = devicesMap.get(deviceKey);
                if (device != null && BondedState.fromCode(device.getBondState()) == BondedState.NONE) {
                    Method method = device.getClass().getMethod("createBond", (Class[]) null);
                    method.invoke(device, (Object[]) null);
                }

                return getDevicesInformation().map(deviceInformations -> {
                    final ArrayList<DeviceInformation> deviceInformationList = new ArrayList<>(deviceInformations);
                    for (DeviceInformation item : deviceInformationList) {
                        if (keyFromNameAndAddress(item.getName(), item.getAddress()).equals(deviceKey)) {
                            return item;
                        }
                    }
                    return null;
                });
            }
            throw new DeviceNotFoundException(deviceKey);
        } catch (final Exception e) {
            throw new DeviceConnectionFailureException();
        }
    }

    /**
     * Unpair devices.
     *
     * @param deviceKey
     */
    public Single<DeviceInformation> unPair(String deviceKey) throws DeviceConnectionFailureException {
        try {
            if (devicesMap.containsKey(deviceKey)) {
                final BluetoothDevice device = devicesMap.get(deviceKey);
                if (device != null && BondedState.fromCode(device.getBondState()) == BondedState.NONE) {
                    Method method = device.getClass().getMethod("removeBond", (Class[]) null);
                    method.invoke(device, (Object[]) null);
                }

                return getDevicesInformation().map(deviceInformations -> {
                    final ArrayList<DeviceInformation> deviceInformationList = new ArrayList<>(deviceInformations);
                    final int deviceIndex = deviceInformationList.indexOf(device);
                    return deviceInformationList.get(deviceIndex);
                });
            }
            throw new DeviceNotFoundException(deviceKey);
        } catch (final Exception e) {
            throw new DeviceConnectionFailureException();
        }
    }

    public BluetoothSocket getSocketForDevice(DeviceType deviceType) throws DeviceAlreadyConnectedException, DeviceNotSelectedForTypeException, DeviceConnectionFailureException {

        if (!selectedDevicesByType.containsKey(deviceType)) {
            throw new DeviceNotSelectedForTypeException(deviceType);
        }

        final BluetoothSocket socket = connectionManager.getSocketForDevice(selectedDevicesByType.get(deviceType));

        if (socket == null) {
            SocketConnectionWithStatus connectionResult = connect(deviceType);

            if (connectionResult.getStatus() == ConnectionStatus.SUCCESS) {
                return connectionResult.getSocket();
            } else {
                throw new DeviceConnectionFailureException(selectedDevicesByType.get(deviceType));
            }
        } else {
            return socket;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private BluetoothDevice getSelected(DeviceType deviceType) throws DeviceNotSelectedForTypeException {
        if (selectedDevicesByType.containsKey(deviceType)) {
            return selectedDevicesByType.get(deviceType);
        } else {
            throw new DeviceNotSelectedForTypeException(deviceType);
        }
    }

    private SocketConnectionWithStatus connect(DeviceType deviceType) throws DeviceConnectionFailureException, DeviceNotSelectedForTypeException, DeviceAlreadyConnectedException {
        final BluetoothDevice selectedDevice = getSelected(deviceType);
        return connectionManager.connect(selectedDevice, true);
    }

    private String computeDeviceKey(BluetoothDevice device) {
        return keyFromNameAndAddress(device.getName(), device.getAddress());
    }

    private String keyFromNameAndAddress(String name, String address) {
        return name + address;
    }

    private DeviceType getSelectedForDeviceType(BluetoothDevice device) {
        for (Map.Entry<DeviceType, BluetoothDevice> entry : selectedDevicesByType.entrySet()) {
            if (device.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return DeviceType.NONE;
    }
}
