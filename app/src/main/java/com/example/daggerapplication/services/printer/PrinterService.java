package com.example.daggerapplication.services.printer;

import android.bluetooth.BluetoothSocket;

import com.example.daggerapplication.services.bluetooth.BluetoothService;
import com.example.daggerapplication.services.bluetooth.exception.DeviceAlreadyConnectedException;
import com.example.daggerapplication.services.bluetooth.exception.DeviceConnectionFailureException;
import com.example.daggerapplication.services.bluetooth.exception.DeviceNotSelectedForTypeException;
import com.example.daggerapplication.services.bluetooth.model.DeviceType;

import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

public class PrinterService {

    private final BluetoothService bluetoothService;

    @Inject
    public PrinterService(BluetoothService bluetoothService) {
        this.bluetoothService = bluetoothService;
    }

    public void print() {
        try {
            final BluetoothSocket socket = bluetoothService.getSocketForDevice(DeviceType.PRINTER);
            OutputStream outputStream = socket.getOutputStream();

            byte[] printformat = { 0x1B, 0*21, 1 };
            outputStream.write(printformat);
            outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
            outputStream.write("TEST TEST TEST SIMPLE".getBytes());
            outputStream.flush();
        } catch (DeviceAlreadyConnectedException e) {
            e.printStackTrace();
        } catch (DeviceNotSelectedForTypeException e) {
            e.printStackTrace();
        } catch (DeviceConnectionFailureException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
