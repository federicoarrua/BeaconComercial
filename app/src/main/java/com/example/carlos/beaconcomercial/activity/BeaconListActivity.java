package com.example.carlos.beaconcomercial.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.example.carlos.beaconcomercial.R;
import com.example.carlos.beaconcomercial.adapter.BeaconListAdapter;
import com.example.carlos.beaconcomercial.api.ApiUtils;
import com.example.carlos.beaconcomercial.api.BeaconApi;
import com.example.carlos.beaconcomercial.classesBeacon.BeaconModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BeaconListActivity extends AppCompatActivity {

    private RecyclerView beaconListRecyclerView;
    private BeaconListAdapter beaconListAdapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_list);

        beaconListRecyclerView = findViewById(R.id.beacon_recycler_view);
        progressBar = findViewById(R.id.progress_bar_recycler);

        beaconListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        getBeacons();
    }

    //Esto va al presenter fija
    private void getBeacons(){
        BeaconApi api = ApiUtils.getAPIService();
        api.getBeacons().enqueue(new Callback<List<BeaconModel>>() {
            @Override
            public void onResponse(Call<List<BeaconModel>> call, Response<List<BeaconModel>> response) {
                if(response.isSuccessful()) {
                    beaconListAdapter = new BeaconListAdapter(response.body() , BeaconListActivity.this);
                    beaconListRecyclerView.setAdapter(beaconListAdapter);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<BeaconModel>> call, Throwable t) {

            }
        });
    }
}
