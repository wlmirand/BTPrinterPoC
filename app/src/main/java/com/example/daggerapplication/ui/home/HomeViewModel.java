package com.example.daggerapplication.ui.home;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.ViewModel;

import com.example.daggerapplication.services.bluetooth.BluetoothService;
import com.example.daggerapplication.services.bluetooth.ConnectionObserver;
import com.example.daggerapplication.services.bluetooth.DeviceType;
import com.example.daggerapplication.services.printer.PrinterService;
import java.util.Observer;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;

public class HomeViewModel extends ViewModel {

    private final PrinterService printerService;
    private BluetoothService btService;
    private Set<String> devicesNames;
    private Consumer<String> consumer;
    private Observer fragmentObserver;

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

    void connect(BluetoothDevice device, DeviceType deviceType) {
       btService.connect(device, deviceType);
    }

    void register(ConnectionObserver observer) {
        btService.registerOnConnectionNotifications(observer);
    }

    void print() {
        printerService.print();
    }
}
