package com.example.daggerapplication.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daggerapplication.R;
import com.example.daggerapplication.dagger.ViewModelFactory;
import com.example.daggerapplication.services.printer.model.PrintableDocument;
import com.example.daggerapplication.ui.CompositeDisposable;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class HomeFragment extends DaggerFragment {

    public static final String ACTIVE = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";
    public static final String BLUETOOTH_NOT_SUPPORTED = "BLUETOOTH NOT SUPPORTED";
    /**
     * ViewModel Factory, because we dont want to pass lots of parameters
     */
    @Inject
    ViewModelFactory viewModelFactory;

    /**
     * ViewModel
     */
    private HomeViewModel viewModel;
    private TextView status;
    private TextView message;
    private RecyclerView devices;
    private Button buttonBTActivate;
    private Button buttonBTSearch;
    private TextView action;
    private Button buttonPrint;
    private BlueToothDevicesListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this, viewModelFactory).get(HomeViewModel.class);
        mAdapter = new BlueToothDevicesListAdapter(viewModel);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        action = view.findViewById(R.id.action);
        message = view.findViewById(R.id.message);

        devices = view.findViewById(R.id.devices);
        devices.setHasFixedSize(true);

        devices.setLayoutManager(new LinearLayoutManager(getContext()));
        devices.addItemDecoration(new DividerItemDecoration(devices.getContext(), DividerItemDecoration.VERTICAL));
        devices.setAdapter(mAdapter);

        buttonBTActivate = view.findViewById(R.id.buttonBTActivate);
        buttonBTActivate.setOnClickListener(v -> {
            action.setText("ACTIVATE BT");
            action.setTextColor(Color.GRAY);
            viewModel.activateBluetooth();
        });

        buttonPrint = view.findViewById(R.id.print);
        status = view.findViewById(R.id.status);

        buttonPrint.setOnClickListener(e -> {
            action.setText("PRINT");
            action.setTextColor(Color.GRAY);
            onClickPrint(e,action);
        });

        buttonBTActivate.setEnabled(viewModel.isBluetoothAvailable() && !viewModel.isBlueToothActivated());

        if (viewModel.isBluetoothAvailable()) {
            if (viewModel.isBlueToothActivated()) {
                status.setText(ACTIVE);
                status.setTextColor(Color.GREEN);
            } else {
                status.setText(INACTIVE);
                status.setTextColor(Color.YELLOW);
            }
        } else {
            status.setText(BLUETOOTH_NOT_SUPPORTED);
            status.setTextColor(Color.RED);
        }
    }


    private void onClickPrint(View v, TextView status) {
        CompositeDisposable.add(viewModel.print(
                PrintableDocument.builder()
                        .title("A TITLE")
                        .build()).subscribe(
                printStatus -> {
                    final int color;
                    status.setText(printStatus.getStatus().toString());
                    switch (printStatus.getStatus()) {
                        case SUCCESS:
                            color = Color.GREEN;
                            break;
                        case ERROR:
                            color = Color.RED;
                            break;
                        default:
                            color = Color.YELLOW;
                    }
                    status.setTextColor(color);
                }
        ));
    }

}
