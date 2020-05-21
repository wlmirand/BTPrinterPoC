package com.example.daggerapplication.ui.details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.daggerapplication.dagger.ViewModelFactory;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class DetailsFragment extends DaggerFragment {

    /**
     * ViewModel Factory, because we dont want to pass lots of parameters
     */
    @Inject
    ViewModelFactory viewModelFactory;

    /**
     * ViewModel
     */
    private DetailsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this, viewModelFactory).get(DetailsViewModel.class);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
