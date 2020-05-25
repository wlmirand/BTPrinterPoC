package com.example.daggerapplication.ui.home;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daggerapplication.R;
import com.example.daggerapplication.services.bluetooth.DeviceType;

import java.util.ArrayList;

public class BlueToothDevicesListAdapter extends RecyclerView.Adapter<BlueToothDevicesListAdapter.MyViewHolder> {

    private final HomeViewModel viewModel;
    private ArrayList<BluetoothDevice> dataList;

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
        dataList = new ArrayList<>(viewModel.getDevicesInformation());
        this.viewModel = viewModel;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public BlueToothDevicesListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                       int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_row, parent, false);
        return new MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.btName.setText(dataList.get(position).getName());
        holder.btBonded.setEnabled(dataList.get(position).getBondState() != BluetoothDevice.BOND_BONDED);
        holder.btSelected.setEnabled(true);
        holder.btSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                viewModel.connect(dataList.get(position), DeviceType.PRINTER);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    void updateDataWith(BluetoothDevice device) {
        dataList.set(dataList.indexOf(device), device);
    }
}