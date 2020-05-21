package com.example.daggerapplication.dagger.module;

import com.example.daggerapplication.ui.MainActivity;
import com.example.daggerapplication.ui.MainActivityBindingModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBindingModule {

    /**
     * For each Activity, we may need to have some fragments inside it
     */
    @ContributesAndroidInjector(modules = {MainActivityBindingModule.class})
    abstract MainActivity bindMainActivity();
}
