package com.example.daggerapplication.ui.home;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.ViewModel;

import com.example.daggerapplication.services.bluetooth.BluetoothService;
import com.example.daggerapplication.services.bluetooth.DeviceType;
import com.example.daggerapplication.services.printer.PrinterService;

import java.util.HashMap;
import java.util.Set;

import javax.inject.Inject;

public class HomeViewModel extends ViewModel {

    private final PrinterService printerService;
    private BluetoothService btService;
    private HashMap<DeviceType, BluetoothDevice> deviceByType = new HashMap<>();

    @Inject
    HomeViewModel(BluetoothService btService, PrinterService printerService) {
        this.btService = btService;
        this.printerService = printerService;
    }

    void activate() {
        btService.activate();
    }

    Set<BluetoothDevice> getDevicesInformation() {
        return btService.getBondedDevices();
    }

    boolean isBTAvailable() {
        return btService.isAvailable();
    }

    boolean isBTActivated() {
        return btService.isActivated();
    }


    void print() throws Exception {
        if (deviceByType.containsKey(DeviceType.PRINTER)) {
            printerService.print(deviceByType.get(DeviceType.PRINTER));
        } else {
            throw new Exception("No Device Selected");
        }
    }

    void selectDevice(BluetoothDevice bluetoothDevice, DeviceType deviceType, boolean isChecked) {
        if (isChecked) {
            deviceByType.put(deviceType, bluetoothDevice);
        } else {
            deviceByType.remove(deviceType);
        }
    }
}
