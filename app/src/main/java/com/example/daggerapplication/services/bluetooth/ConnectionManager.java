package com.example.daggerapplication.services.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;

class ConnectionManager {

    private static final String LOG_TAG = ConnectionManager.class.getSimpleName();

    private class SocketConnectionThread extends Thread implements Runnable {
        private final BluetoothDevice device;
        private final ObservableEmitter<BluetoothSocket> emitter;
        private BluetoothSocket socket;

        SocketConnectionThread(BluetoothDevice device, ObservableEmitter<BluetoothSocket> emitter) {
            this.device = device;
            this.emitter = emitter;
        }

        BluetoothSocket getSocket() {
            return socket;
        }

        public void run() {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            try {
                socket = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
                socket.connect();
                emitter.onNext(socket);
                emitter.onComplete();
            } catch (IOException connectException) {
                Log.e(LOG_TAG, "Unable to connect on device: " + device.getName() + "[" + device.getAddress() + "]");
                emitter.onError(new Throwable("Unable to connect on device"));
            }
        }
    }

    private class SocketConnectionOnSubscribe implements ObservableOnSubscribe<BluetoothSocket> {
        private final BluetoothDevice device;
        private SocketConnectionThread socketConnectionThread;

        SocketConnectionOnSubscribe(BluetoothDevice device) {
            this.device = device;
        }

        @Override
        public void subscribe(ObservableEmitter<BluetoothSocket> emitter) {
            socketConnectionThread = new SocketConnectionThread(device, emitter);
            socketConnectionThread.start();

            emitter.setDisposable(Disposables.fromRunnable(() -> {
                if (socketConnectionThread != null
                        && socketConnectionThread.getSocket() != null
                        && socketConnectionThread.getSocket().isConnected()) {
                    try {
                        socketConnectionThread.getSocket().close();
                    } catch (IOException e) {
                        // TODO JFVI
                    }
                }
                socketConnectionThread = null;
            }));
        }
    }

    @Inject
    ConnectionManager() {
    }


    Observable<OutputStream> getOutputStreamOn(BluetoothDevice device) {
        return Observable.defer(() -> Observable.create(new SocketConnectionOnSubscribe(device))
                .subscribeOn(Schedulers.io())
                .map(bluetoothSocket -> bluetoothSocket.getOutputStream()));
    }


}
