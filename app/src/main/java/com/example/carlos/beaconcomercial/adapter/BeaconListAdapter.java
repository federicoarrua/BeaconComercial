package com.example.carlos.beaconcomercial.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.carlos.beaconcomercial.R;
import com.example.carlos.beaconcomercial.classesBeacon.BeaconModel;

import java.util.List;

public class BeaconListAdapter extends RecyclerView.Adapter<BeaconViewHolder> {

    List<BeaconModel> beaconModelList;
    Context context;

    public BeaconListAdapter(List<BeaconModel> beaconModelList,Context context){
        this.beaconModelList = beaconModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public BeaconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.beacon_row, null);
        BeaconViewHolder beaconViewHolder = new BeaconViewHolder(view);

        return beaconViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BeaconViewHolder holder, int position) {
        holder.getTextView().setText(new Integer(position).toString() + " : " + beaconModelList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return beaconModelList.size();
    }
}
