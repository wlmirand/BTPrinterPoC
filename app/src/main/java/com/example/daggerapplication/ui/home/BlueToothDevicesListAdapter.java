package com.example.daggerapplication.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daggerapplication.R;
import com.example.daggerapplication.services.bluetooth.model.DeviceInformation;
import com.example.daggerapplication.services.bluetooth.model.DeviceType;
import com.example.daggerapplication.ui.CompositeDisposable;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class BlueToothDevicesListAdapter extends RecyclerView.Adapter<BlueToothDevicesListAdapter.MyViewHolder> {

    private final HomeViewModel viewModel;
    private HashMap<String, DeviceInformation> dataMap;

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        Switch connection;

        MyViewHolder(View v) {
            super(v);
            deviceName = itemView.findViewById(R.id.btName);
            connection = itemView.findViewById(R.id.switch1);
        }
    }

    BlueToothDevicesListAdapter(HomeViewModel viewModel) {
        this.viewModel = viewModel;
        this.dataMap = new HashMap<>();
        CompositeDisposable.add(viewModel.getDevicesInformation()
                .subscribe(
                        devicesInformation -> {
                            for (DeviceInformation info : devicesInformation) {
                                dataMap.put(info.getAddress(), info);
                            }
                            notifyDataSetChanged();
                        },
                        throwable -> dataMap = new HashMap<>()
                ));
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
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
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        final ArrayList<String> keys = new ArrayList(dataMap.keySet());
        final String key = keys.get(position);

        holder.deviceName.setText(dataMap.get(key).getName());
        holder.connection.setChecked(dataMap.get(key).isConnected());
        holder.connection.setOnCheckedChangeListener((buttonView, clicked) -> {
            final Disposable disposable = viewModel.selectUnselectDevice(dataMap.get(key), DeviceType.PRINTER, clicked)
                    .subscribe(success -> System.out.println("OK"), throwable -> System.out.println("NOK"));
            CompositeDisposable.add(disposable);
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataMap != null ? dataMap.size() : 0;
    }

}