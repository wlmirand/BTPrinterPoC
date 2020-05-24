package com.example.daggerapplication.ui.home;

import androidx.lifecycle.ViewModel;

import com.example.daggerapplication.services.bluetooth.BluetoothService;
import com.example.daggerapplication.services.bluetooth.exception.DeviceConnectionFailureException;
import com.example.daggerapplication.services.bluetooth.exception.DeviceNotFoundException;
import com.example.daggerapplication.services.bluetooth.model.DeviceConnectionResult;
import com.example.daggerapplication.services.bluetooth.model.DeviceInformation;
import com.example.daggerapplication.services.bluetooth.model.DeviceType;
import com.example.daggerapplication.services.printer.PrinterService;

import java.util.HashSet;
import java.util.Observer;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;

import io.reactivex.Single;

public class HomeViewModel extends ViewModel {

    private final PrinterService printerService;
    private BluetoothService btService;
    private Set<String> devicesNames;
    private Consumer<String> consumer;
    private Observer fragmentObserver;

    @Inject
    public HomeViewModel(BluetoothService btService, PrinterService printerService) {
        this.btService = btService;
        this.printerService = printerService;
    }

    public void activate() {
        btService.activate();
    }

    public Single<HashSet<DeviceInformation>> getDevicesInformation() {
        return btService.getDevicesInformation();
    }

    public boolean isBTAvailable() {
        return btService.isAvailable();
    }

    public boolean isBTActivated() {
        return btService.isActivated();
    }

    public DeviceConnectionResult selectForDeviceType(String deviceKey, DeviceType deviceType) {
        try {
            return btService.selectForType(deviceKey, deviceType);
        } catch (DeviceNotFoundException e) {
            e.printStackTrace();
        } catch (DeviceConnectionFailureException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Single<DeviceInformation> pair(String deviceKey) {
        try {
            return btService.pair(deviceKey);
        } catch (DeviceConnectionFailureException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void print() {
        printerService.print();
    }
}
