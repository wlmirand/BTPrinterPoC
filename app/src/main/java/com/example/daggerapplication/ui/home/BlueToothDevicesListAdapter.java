package com.example.daggerapplication.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daggerapplication.R;
import com.example.daggerapplication.services.bluetooth.model.DeviceInformation;
import com.example.daggerapplication.services.bluetooth.model.DeviceType;
import com.example.daggerapplication.services.common.CompositeDisposable;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BlueToothDevicesListAdapter extends RecyclerView.Adapter<BlueToothDevicesListAdapter.MyViewHolder> {

    private final HomeViewModel viewModel;
    private List<DeviceInformation> dataList;
    private static OnClickListener onClickListener;

    private static class OnClickListener {
        private final HomeViewModel viewModel;
        private List<DeviceInformation> dataList;

        OnClickListener(HomeViewModel viewModel) {
            this.viewModel = viewModel;
        }

        void onClick(View v, int adapterPosition) {
            final boolean clicked = ((CompoundButton) v).isChecked();
            final DeviceInformation deviceInformation = dataList.get(adapterPosition);
            viewModel.selectPrinter(deviceInformation);
        }

        void updateDataList(List<DeviceInformation> updatedData) {
            this.dataList = updatedData;
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        Switch connection;

        MyViewHolder(View v, HomeViewModel viewModel) {
            super(v);
            deviceName = itemView.findViewById(R.id.btName);
            connection = itemView.findViewById(R.id.switch1);
            connection.setOnClickListener(v1 -> onClickListener.onClick(v1, getAdapterPosition()));
        }
    }

    BlueToothDevicesListAdapter(HomeViewModel viewModel) {
        this.viewModel = viewModel;
        this.dataList = new ArrayList<>();
        onClickListener = new OnClickListener(viewModel);
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
        return new MyViewHolder(v, viewModel);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final DeviceInformation data = dataList.get(position);
        holder.deviceName.setText(data.getName());
        holder.connection.setChecked(data.isConnected());
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    void updateDataList(List<DeviceInformation> dataList) {
        this.dataList = dataList;
        onClickListener.updateDataList(dataList);
        notifyDataSetChanged();
    }
}