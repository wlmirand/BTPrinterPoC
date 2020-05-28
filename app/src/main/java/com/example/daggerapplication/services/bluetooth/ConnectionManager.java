package com.example.daggerapplication.services.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.daggerapplication.services.bluetooth.model.DeviceType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

@Singleton
class ConnectionManager {

    private static final String LOG_TAG = ConnectionManager.class.getSimpleName();
    private HashMap<DeviceType, BluetoothSocket> socketByDeviceType = new HashMap<>();
    private final SocketSetConnectionOnSubscribe socketSetPublisher = new SocketSetConnectionOnSubscribe();

    private final
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Threads and Observable declaration

    class SocketStateRunnable implements Runnable {
        @Override
        public void run() {
            final ArrayList<BluetoothSocket> socketSet = new ArrayList(socketByDeviceType.values());

            for (BluetoothSocket inspected : socketSet) {
                if (!inspected.isConnected()) {
                    // remove
                    socketSet.remove(inspected);
                    if (socketSetPublisher.isSubscribed()) {
                        socketSetPublisher.fireUpdateSet();
                    }
                }
            }
        }
    }

    class SocketSetConnectionOnSubscribe implements ObservableOnSubscribe<HashMap<DeviceType, BluetoothSocket>> {
        private ObservableEmitter<HashMap<DeviceType, BluetoothSocket>> emitter;

        void fireUpdateSet() {
            if (emitter != null) {
                this.emitter.onNext(socketByDeviceType);
            }
        }

        public boolean isSubscribed() {
            return emitter != null;
        }

        @Override
        public void subscribe(ObservableEmitter<HashMap<DeviceType, BluetoothSocket>> emitter) throws Exception {
            this.emitter = emitter;
            emitter.onNext(socketByDeviceType);
        }
    }

    private class SocketConnectionThread extends Thread implements Runnable {

        private final BluetoothDevice device;
        private final ObservableEmitter<BluetoothSocket> emitter;
        private final DeviceType deviceType;
        private BluetoothSocket socket;

        SocketConnectionThread(BluetoothDevice device, DeviceType deviceType, ObservableEmitter<BluetoothSocket> emitter) {
            this.device = device;
            this.deviceType = deviceType;
            this.emitter = emitter;
        }

        public void run() {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            try {
                socket = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
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

    private class SocketConnectionOnSubscribe implements ObservableOnSubscribe<BluetoothSocket> {
        private final BluetoothDevice device;
        private final DeviceType deviceType;

        SocketConnectionOnSubscribe(BluetoothDevice device, DeviceType deviceType) {
            this.device = device;
            this.deviceType = deviceType;
        }

        @Override
        public void subscribe(ObservableEmitter<BluetoothSocket> emitter) {
            SocketConnectionThread socketConnectionThread = new SocketConnectionThread(device, deviceType, emitter);
            socketConnectionThread.start();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    ConnectionManager() {
        final ScheduledExecutorService executor = Executors
                .newScheduledThreadPool(2);
        final Runnable socketStateCheck = new SocketStateRunnable();

        executor.scheduleAtFixedRate(socketStateCheck, 10, 10, TimeUnit.SECONDS);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Select/Unselect the requested device (identified by MAD Address) for the device type.<br>
     * A connection is performed on device and stored.
     *
     * @param address    the device MAC address.
     * @param deviceType the device type for which the connection is required.
     * @param isToSelect true to select a device, false to remove it.
     * @return the connection status.
     */
    Observable<Boolean> selectUnselectDevice(String address, DeviceType deviceType, boolean isToSelect) {
        try {
            final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            final BluetoothSocket socket = socketByDeviceType.remove(deviceType);

            if (socket != null && socket.isConnected()) {
                Log.i(LOG_TAG, "Socket closed for device: " + address + " of device type: " + deviceType);
                socket.close();
            }

            if (isToSelect) {
                Log.i(LOG_TAG, "Connect to device: " + address + "for device type:" + deviceType);
                return Observable.defer(() -> Observable.create(new SocketConnectionOnSubscribe(device, deviceType))
                        .subscribeOn(Schedulers.io()))
                        .map(bluetoothSocket -> bluetoothSocket.isConnected());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to select/unselect the device with address: " + address);
            return Observable.error(e);
        }

        return Observable.just(Boolean.FALSE);
    }

    /**
     * @param deviceType The device type.
     * @return A one element observable socket
     */
    Observable<BluetoothSocket> getConnectionSocketFor(DeviceType deviceType) {
        if (socketByDeviceType.containsKey(deviceType)) {
            final BluetoothSocket socket = socketByDeviceType.get(deviceType);
            if (socket != null && socket.isConnected()) {
                return Observable.just(socket);
            } else {
                Log.i(LOG_TAG, "Socket not connected - Retry for device " + socket.getRemoteDevice().getName() + "[" + socket.getRemoteDevice().getAddress() + "]");
                return Observable.defer(() -> Observable.create(new SocketConnectionOnSubscribe(socket.getRemoteDevice(), deviceType))).retry(3);
            }
        } else {
            Log.e(LOG_TAG, "No Device Selected for type : " + deviceType);
            return Observable.error(new Throwable("No device selected for type: " + deviceType));
        }
    }

    Observable<HashMap<DeviceType, BluetoothSocket>> getBluetoothSockets() {
        return Observable.defer(() -> Observable.create(socketSetPublisher));
    }

}
