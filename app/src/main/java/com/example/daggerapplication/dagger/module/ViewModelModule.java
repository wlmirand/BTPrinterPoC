package com.example.daggerapplication.dagger.module;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.daggerapplication.dagger.ViewModelFactory;
import com.example.daggerapplication.dagger.ViewModelKey;
import com.example.daggerapplication.ui.details.DetailsViewModel;
import com.example.daggerapplication.ui.home.HomeViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

/**
 * Module related to ViewModel objects
 */
@Module
public abstract class ViewModelModule {

    /**
     * Creates the ViewModel Factory, so we can inject into Fragments and Activities
     */
    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);

    /**
     * We need to create one Map entry for each ViewModel
     */
    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel.class)
    abstract ViewModel bindHomeViewModel(HomeViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DetailsViewModel.class)
    abstract ViewModel bindDetailsViewModel(DetailsViewModel viewModel);
}
