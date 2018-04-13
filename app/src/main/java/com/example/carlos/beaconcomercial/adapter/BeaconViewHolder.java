package com.example.carlos.beaconcomercial.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.carlos.beaconcomercial.R;

public class BeaconViewHolder extends RecyclerView.ViewHolder {

    TextView textView;

    public BeaconViewHolder(View view){
        super(view);
        textView = view.findViewById(R.id.beacon_row_text);
    }

    public TextView getTextView() {
        return textView;
    }

}
