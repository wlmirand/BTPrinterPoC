package com.example.daggerapplication.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daggerapplication.R;
import com.example.daggerapplication.services.bluetooth.model.DeviceInformation;
import com.example.daggerapplication.services.bluetooth.model.DeviceType;
import com.example.daggerapplication.ui.CompositeDisposable;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BlueToothDevicesListAdapter extends RecyclerView.Adapter<BlueToothDevicesListAdapter.MyViewHolder> {

    private final HomeViewModel viewModel;
    private List<DeviceInformation> dataList = new ArrayList<>();

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
        Disposable disposable = viewModel.getDevicesInformation()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        devicesInformation -> {
                            final ArrayList<DeviceInformation> devices = new ArrayList<>();
                            devices.addAll(devicesInformation);
                            dataList = devices;
                            notifyDataSetChanged();
                        },
                        throwable -> dataList.clear());

        CompositeDisposable.add(disposable);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public BlueToothDevicesListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                       int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_row, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        final DeviceInformation data = dataList.get(position);

        holder.deviceName.setText(data.getName());
        holder.connection.setOnCheckedChangeListener((buttonView, clicked) -> {
            System.out.println(data);
            final boolean isChecked = clicked;
            buttonView.setChecked(!clicked);
            final Disposable disposable = viewModel.selectUnselectDevice(dataList.get(position), DeviceType.PRINTER, isChecked)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(deviceInformation -> {
                        Toast.makeText(buttonView.getContext(), data.getName() + "[" + data.getAddress() + "] Connected", Toast.LENGTH_LONG).show();
                    }, throwable -> Toast.makeText(buttonView.getContext(), data.getName() + "[" + data.getAddress() + "] Not Connected", Toast.LENGTH_LONG).show());
            CompositeDisposable.add(disposable);
        });
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }
}