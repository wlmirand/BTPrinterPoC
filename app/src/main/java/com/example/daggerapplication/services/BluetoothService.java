package com.example.daggerapplication.services;

import android.content.Context;

import javax.inject.Inject;

public class BluetoothService {

    private final Context appContext;

    @Inject
    public BluetoothService(Context context) {
        this.appContext = context;
    }
}
