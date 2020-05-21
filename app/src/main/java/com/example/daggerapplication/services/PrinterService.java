package com.example.daggerapplication.services;

import javax.inject.Inject;

public class PrinterService {

    private final BluetoothService bluetoothService;

    @Inject
    public PrinterService(BluetoothService bluetoothService) {
        this.bluetoothService = bluetoothService;
    }

    public void print() {

    }
}
