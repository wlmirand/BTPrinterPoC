package com.example.daggerapplication.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.daggerapplication.R;
import com.example.daggerapplication.services.bluetooth.model.BondedState;
import com.example.daggerapplication.services.bluetooth.model.ConnectionStatus;
import com.example.daggerapplication.services.bluetooth.model.DeviceConnectionResult;
import com.example.daggerapplication.services.bluetooth.model.DeviceInformation;
import com.example.daggerapplication.services.bluetooth.model.DeviceType;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class BlueToothDevicesListAdapter extends RecyclerView.Adapter<BlueToothDevicesListAdapter.MyViewHolder> {

    private final HomeViewModel viewModel;
    private List<DeviceInformation> dataList;
    private Disposable disposableDevices;

    public void search() {
//        disposableDevices = viewModel.getDevicesInformation()
//                .subscribe(devicesInformation -> {
//                    this.dataList = new ArrayList<>(devicesInformation);
//                    notifyDataSetChanged();
//                });
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView btName;
        Button btBonded;
        CheckBox btSelected;

        MyViewHolder(View v) {
            super(v);
            btName = itemView.findViewById(R.id.btName);
            btBonded = itemView.findViewById(R.id.btBonded);
            btSelected = itemView.findViewById(R.id.btSelected);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    BlueToothDevicesListAdapter(HomeViewModel viewModel) {


        disposableDevices = viewModel.getDevicesInformation()
                .subscribe(devicesInformation -> {
                    this.dataList = new ArrayList<>(devicesInformation);
                    notifyDataSetChanged();
                });

        this.viewModel = viewModel;
    }


    @Override
    public void onDetachedFromRecyclerView(RecyclerView rcView) {
        disposableDevices.dispose();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public BlueToothDevicesListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                       int viewType) {

        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_row, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.btName.setText(dataList.get(position).getName());
        holder.btBonded.setEnabled(dataList.get(position).getBoundedStatus() != BondedState.BONDED);
        holder.btBonded.setOnClickListener(v -> {
            viewModel.pair(dataList.get(position).getKey()).subscribe((deviceInformation, throwable) -> {
                dataList.set(position, deviceInformation);
                v.setEnabled(deviceInformation.getBoundedStatus() != BondedState.BONDED);
            });
        });
        holder.btSelected.setEnabled(dataList.get(position).getSelectedForDeviceType() == DeviceType.NONE);
        holder.btSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                final DeviceConnectionResult connectionResult = viewModel.selectForDeviceType(dataList.get(position).getKey(), DeviceType.PRINTER);
                if (connectionResult.getStatus() == ConnectionStatus.SUCCESS) {
                    dataList.set(position, connectionResult.getDeviceInformation());
                    buttonView.setChecked(true);
                } else {
                    buttonView.setChecked(true);
                }
            }
        });
        holder.btName.setOnClickListener((v) -> {
            viewModel.selectForDeviceType(dataList.get(position).getKey(), DeviceType.PRINTER);
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataList!=null ? dataList.size() : 0;
    }

}