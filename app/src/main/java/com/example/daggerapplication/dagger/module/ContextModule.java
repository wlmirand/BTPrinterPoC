package com.example.daggerapplication.dagger.module;

import android.app.Application;
import android.content.Context;

import dagger.Binds;
import dagger.Module;

/**
 * Module to return the Application Context when needed
 */
@Module
public abstract class ContextModule {

    /**
     * Return the Application Context from the Application object
     */
    @Binds
    abstract Context provideContext(Application application);
}
