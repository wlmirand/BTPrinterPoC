package com.example.daggerapplication.services.printer;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.example.daggerapplication.services.bluetooth.BluetoothService;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PrinterService {

    private static final String LOG_TAG = "PrinterService";

    private final BluetoothService bluetoothService;
    private BluetoothSocket currentSocket;
    private Disposable disposable;

    @Inject
    PrinterService(BluetoothService bluetoothService) {
        this.bluetoothService = bluetoothService;
    }

    public void print(BluetoothDevice device) {

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        disposable = bluetoothService.getOutputStreamOn(device)
                .subscribe(
                        outputStream -> {
                            byte[] printout = {0x1B, 0x21, 1};
                            outputStream.write(printout);
                            outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                            outputStream.write("TEST TEST TEST SIMPLE".getBytes());
                            outputStream.flush();
                        },
                        throwable -> {
                            // something went wrong
                        }
                );
    }

}
