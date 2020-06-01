package org.universalpostalunion.printerlibrary.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import org.universalpostalunion.printerlibrary.bluetooth.model.DeviceInformation;
import org.universalpostalunion.printerlibrary.bluetooth.model.DeviceType;

import java.io.IOException;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

@Singleton
class ConnectionManager {

    private static final String LOG_TAG = ConnectionManager.class.getSimpleName();
    private final BluetoothAdapter btAdapter;
    private HashMap<DeviceType, BluetoothSocket> socketByDeviceType = new HashMap<>();
    private final SocketSetConnectionOnSubscribe socketSetPublisher = new SocketSetConnectionOnSubscribe();

    class SocketSetConnectionOnSubscribe implements ObservableOnSubscribe<HashMap<DeviceType, BluetoothSocket>> {
        private ObservableEmitter<HashMap<DeviceType, BluetoothSocket>> emitter;

        void fireUpdateSet() {
            if (emitter != null) {
                this.emitter.onNext(socketByDeviceType);
            }
        }

        @Override
        public void subscribe(ObservableEmitter<HashMap<DeviceType, BluetoothSocket>> emitter) {
            this.emitter = emitter;
            emitter.onNext(socketByDeviceType);
        }
    }

    private class SocketConnectionOnSubscribe implements ObservableOnSubscribe<BluetoothSocket> {
        private final BluetoothDevice device;
        private final DeviceType deviceType;
        private final BluetoothAdapter btAdapter;

        SocketConnectionOnSubscribe(BluetoothAdapter btAdapter, BluetoothDevice device, DeviceType deviceType) {
            this.btAdapter = btAdapter;
            this.device = device;
            this.deviceType = deviceType;
        }

        @Override
        public void subscribe(ObservableEmitter<BluetoothSocket> emitter) {
            btAdapter.cancelDiscovery();
            try {
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
                socket.connect();
                socketByDeviceType.put(deviceType, socket);
                socketSetPublisher.fireUpdateSet();
                emitter.onNext(socket);
                emitter.onComplete();
                Log.i(LOG_TAG, "The device " + device.getName() + "[" + device.getAddress() + "] has been successfully connected for device type: " + deviceType);
            } catch (IOException connectException) {
                Log.e(LOG_TAG, "Unable to connect on device: " + device.getName() + "[" + device.getAddress() + "]");
                emitter.onError(new Throwable("Unable to connect on device: " + device.getName() + "[" + device.getAddress() + "]"));
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    ConnectionManager(BluetoothAdapter btAdapter) {
        this.btAdapter = btAdapter;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void clean() {
        Log.i(LOG_TAG, "Bean cleaned");
    }

    /**
     * Select the requested device (identified by MAD Address) for the device type.<br>
     * A connection is performed on device and stored.
     *
     * @param address    the device MAC address.
     * @param deviceType the device type for which the connection is required.
     * @return the connection status.
     */
    private Observable<BluetoothSocket> select(String address, DeviceType deviceType) {
        try {
            final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            final BluetoothSocket socket = socketByDeviceType.remove(deviceType);

            if (socket != null && socket.isConnected()) {
                Log.i(LOG_TAG, "Socket closed for device: " + address + " of device type: " + deviceType);
                socket.close();
            }
            Log.i(LOG_TAG, "Connect to device: " + address + " for device type:" + deviceType);
            return Observable.create(new SocketConnectionOnSubscribe(btAdapter, device, deviceType));
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to select the device with address: " + address);
            return Observable.error(e);
        }
    }

    /**
     * @param device The device to retrieve socket from.
     * @return a socket for the requested device.
     */
    Observable<BluetoothSocket> getConnectionSocketFor(DeviceInformation device) {
        if (socketByDeviceType.containsKey(device.getDeviceType())) {
            final BluetoothSocket socket = socketByDeviceType.get(device.getDeviceType());
            if (socket != null) {
                final String currentSelectedAddress = socket.getRemoteDevice().getAddress();
                if (currentSelectedAddress.equals(device.getAddress()) && socket.isConnected()) {
                    return Observable.just(socket);
                }
            }
        }
        return select(device.getAddress(), device.getDeviceType());
    }

    Observable<HashMap<DeviceType, BluetoothSocket>> getBluetoothSockets() {
        return Observable.defer(() -> Observable.create(socketSetPublisher));
    }

}
