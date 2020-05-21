package com.example.daggerapplication;

import com.example.daggerapplication.dagger.ApplicationComponent;
import com.example.daggerapplication.dagger.DaggerApplicationComponent;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;

/**
 * Application Class, we should extend from Dagger Application
 */
public class CustomApplication extends DaggerApplication {

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        ApplicationComponent component = DaggerApplicationComponent.builder()
                .application(this)
                .build();

        return component;
    }
}
