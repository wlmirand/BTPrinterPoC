package com.example.daggerapplication.ui.home;

import androidx.lifecycle.ViewModel;

import com.example.daggerapplication.services.bluetooth.BluetoothService;
import com.example.daggerapplication.services.bluetooth.model.DeviceInformation;
import com.example.daggerapplication.services.bluetooth.model.DeviceType;
import com.example.daggerapplication.services.printer.PrinterService;
import com.example.daggerapplication.services.printer.model.PrintStatus;
import com.example.daggerapplication.services.printer.model.PrintableDocument;

import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

public class HomeViewModel extends ViewModel {

    private final PrinterService printerService;
    private BluetoothService btService;

    @Inject
    HomeViewModel(BluetoothService btService, PrinterService printerService) {
        this.btService = btService;
        this.printerService = printerService;
    }

    void activate() {
        btService.activate();
    }

    Observable<Set<DeviceInformation>> getDevicesInformation() {
        return btService.getDevicesInformation();
    }

    boolean isBTAvailable() {
        return btService.isAvailable();
    }

    boolean isBTActivated() {
        return btService.isActivated();
    }


    Observable<PrintStatus> print(PrintableDocument document) {
        return printerService.print(document);
    }

    Single<DeviceInformation> selectUnselectDevice(DeviceInformation deviceInformation, DeviceType deviceType, boolean isChecked) {
        return btService.selectUnselectAndConnect(deviceInformation, deviceType, isChecked);
    }
}
