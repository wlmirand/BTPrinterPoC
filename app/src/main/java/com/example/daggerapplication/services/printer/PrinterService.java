package com.example.daggerapplication.services.printer;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.daggerapplication.services.bluetooth.BluetoothService;
import com.example.daggerapplication.services.bluetooth.ConnectionNotification;
import com.example.daggerapplication.services.bluetooth.ConnectionObserver;
import com.example.daggerapplication.services.bluetooth.DeviceType;

import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

public class PrinterService extends ConnectionObserver {

    private static final String LOG_TAG = "PrinterService";

    private final BluetoothService bluetoothService;
    private BluetoothSocket currentSocket;
    private OutputStream outputStream;

    class ThreadPrinter extends Thread implements Runnable {

        private final OutputStream outputStream;

        ThreadPrinter(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        public void run() {
            try {
                byte[] printout = {0x1B, 0 , 1};
                outputStream.write(printout);
                outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                outputStream.write("TEST TEST TEST SIMPLE".getBytes());
                outputStream.flush();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Exception on printing", e);
            }
        }
    }

    @Inject
    PrinterService(BluetoothService bluetoothService) {
        this.bluetoothService = bluetoothService;
        this.bluetoothService.registerOnConnectionNotifications(this);
    }

    public void print() {
        try {
            if (currentSocket != null && currentSocket.isConnected()) {
                if (outputStream == null) {
                    outputStream = currentSocket.getOutputStream();
                }
                new ThreadPrinter(outputStream).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(ConnectionNotification connectionNotification) {

        switch (connectionNotification.getNotification()) {
            case CONNECTED:
                this.currentSocket = connectionNotification.getSocket();
                break;
            case DISCONNECTED:
                if (currentSocket != null && currentSocket.equals(connectionNotification.getSocket())) {
                    // Reconnect
                    bluetoothService.connect(currentSocket.getRemoteDevice().getAddress(), DeviceType.PRINTER);
                }
                break;
            case CONNECTION_ERROR:
                if (currentSocket != null && currentSocket.equals(connectionNotification.getSocket())) {
                    Log.e(LOG_TAG,connectionNotification.getMessage());
                }
            case RECEIVE_MESSAGE:
                Log.e(LOG_TAG, connectionNotification.getMessage());
        }

    }
}
