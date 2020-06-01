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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daggerapplication.R;
import com.example.daggerapplication.dagger.ViewModelFactory;

import org.universalpostalunion.printerlibrary.bluetooth.model.DeviceInformation;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class HomeFragment extends DaggerFragment {

    private static final String ACTIVE = "ACTIVE";
    private static final String INACTIVE = "INACTIVE";
    private static final String BLUETOOTH_NOT_SUPPORTED = "BLUETOOTH NOT SUPPORTED";

    /**
     * ViewModel Factory, because we dont want to pass lots of parameters
     */
    @Inject
    ViewModelFactory viewModelFactory;

    /**
     * ViewModel
     */
    private HomeViewModel viewModel;
    private TextView action;
    private BlueToothDevicesListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this, viewModelFactory).get(HomeViewModel.class);
        mAdapter = new BlueToothDevicesListAdapter(viewModel);

        final Observer<List<DeviceInformation>> listObserver = list -> {
            // Update the UI, in this case, a TextView.
            mAdapter.updateDataList(list);
        };
        viewModel.getPrintersList().observeForever(listObserver);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        action = view.findViewById(R.id.action);
        TextView message = view.findViewById(R.id.message);

        RecyclerView devices = view.findViewById(R.id.devices);
        devices.setHasFixedSize(true);

        devices.setLayoutManager(new LinearLayoutManager(getContext()));
        devices.addItemDecoration(new DividerItemDecoration(devices.getContext(), DividerItemDecoration.VERTICAL));
        viewModel.getAvailablePrinters();
        devices.setAdapter(mAdapter);

        Button buttonPrint = view.findViewById(R.id.print);
        TextView status = view.findViewById(R.id.status);

        buttonPrint.setOnClickListener(e -> {
            action.setText("PRINT");
            action.setTextColor(Color.GRAY);
            onClickPrint(e, action);
        });

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
        viewModel.print();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////

}
