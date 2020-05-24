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
import com.example.daggerapplication.services.bluetooth.BluetoothService;
import com.example.daggerapplication.services.bluetooth.model.DeviceConnectionResult;
import com.example.daggerapplication.services.bluetooth.model.DeviceInformation;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class HomeFragment extends DaggerFragment {

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
    private RecyclerView devices;
    private Button buttonBTActivate;
    private Button buttonBTSearch;
    private TextView action;
    private Button buttonPrint;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this, viewModelFactory).get(HomeViewModel.class);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        action = (TextView) view.findViewById(R.id.action);
        devices = (RecyclerView) view.findViewById(R.id.devices);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        devices.setHasFixedSize(true);

        // use a linear layout manager
        devices.setLayoutManager(new LinearLayoutManager(getContext()));
        devices.addItemDecoration(new DividerItemDecoration(devices.getContext(), DividerItemDecoration.HORIZONTAL));

        // specify an adapter (see also next example)
        final BlueToothDevicesListAdapter mAdapter = new BlueToothDevicesListAdapter(viewModel);
        devices.setAdapter(mAdapter);

        buttonBTActivate = (Button) view.findViewById(R.id.buttonBTActivate);
        buttonBTActivate.setOnClickListener(v -> {
            action.setText("Activating ......");
            viewModel.activate();
        });

        buttonPrint = (Button) view.findViewById(R.id.print);
        buttonPrint.setOnClickListener(v -> {
            action.setText("Printing ......");
            viewModel.print();
        });

        buttonBTActivate.setEnabled(viewModel.isBTAvailable() && !viewModel.isBTActivated());

        buttonBTSearch = (Button) view.findViewById(R.id.buttonBTSearch);
        buttonBTSearch.setOnClickListener(v -> {
            action.setText("Searching ......");
            mAdapter.search();
        });
        buttonBTSearch.setEnabled(viewModel.isBTActivated());

        status = (TextView) view.findViewById(R.id.status);

        if (viewModel.isBTAvailable()) {
            if (viewModel.isBTActivated()) {
                status.setText("ACTIVE");
                status.setTextColor(Color.GREEN);
            } else {
                status.setText("INACTIVE");
                status.setTextColor(Color.YELLOW);
            }
        } else {
            status.setText("BLUETOOTH NOT SUPPORTED");
            status.setTextColor(Color.RED);
        }

    }

//    @Override
//    public void update(Observable observable, Object observableArgs) {
//            action.setText("");
//            if (observable instanceof BluetoothService) {
//                final BluetoothService service = (BluetoothService) observable;
//
//                if (observableArgs instanceof BluetoothService.Notifications) {
//                    final BluetoothService.Notifications notification = (BluetoothService.Notifications) observableArgs;
//
//                    switch (notification) {
//                        case DEVICES_SEARCH:
//                            Set<DeviceInformation> setOfDevices = service.getDevicesInformation();
//                            ((BlueToothDevicesListAdapter)devices.getAdapter()).updateData(new ArrayList<>(setOfDevices));
//                            devices.getAdapter().notifyDataSetChanged();
//                            break;
//                        case BLUETOOTH_STATUS:
//                            if (service.isActivated()) {
//                                status.setText("ACTIVE");
//                                status.setTextColor(Color.GREEN);
//                            } else {
//                                status.setText("INACTIVE");
//                                status.setTextColor(Color.YELLOW);
//                            }
//                    }
//                    buttonBTActivate.setEnabled(!service.isActivated());
//                    buttonBTSearch.setEnabled(service.isAvailable());
//                } else {
//                    final DeviceConnectionResult deviceConnectionResult = (DeviceConnectionResult) observableArgs;
//                    action.setText("DEV: " + deviceConnectionResult.getName() + "-CNX: " + deviceConnectionResult.getConnectionStatus().getStatus());
//                }
//            }
//    }
}
