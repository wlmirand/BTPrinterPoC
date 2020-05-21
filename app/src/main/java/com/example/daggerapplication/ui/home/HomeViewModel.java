package com.example.daggerapplication.ui.home;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.daggerapplication.services.PrinterService;

import javax.inject.Inject;

public class HomeViewModel extends ViewModel {

    private PrinterService printerService;

    @Inject
    public HomeViewModel(PrinterService printerService) {
        this.printerService = printerService;
    }

    public void doStuff() {
        if (printerService != null) {
            Log.d("BRUTUS", "WORKED!!!");
        }
    }
}
