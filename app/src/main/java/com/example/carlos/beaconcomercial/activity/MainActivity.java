package com.example.carlos.beaconcomercial.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.carlos.beaconcomercial.R;
import com.example.carlos.beaconcomercial.api.ApiUtils;
import com.example.carlos.beaconcomercial.api.BeaconApi;
import com.example.carlos.beaconcomercial.classesBeacon.Device;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Federico on 13/12/2016.
 * MainActivity vista principal de la aplicación contiene los botones que dirigen a la funcionalidad
 * de la aplicación.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN_ACTIVITY";
    private final int MY_PERMISSIONS_REQUEST = 1;
    private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 2;
    private final int MY_PERMISSIONS_READ_CONTACTS = 3;
    private SharedPreferences prefs;

    private ProgressBar progressBar;
    private Button buttonList;
    private Button buttonItems;
    private Button buttonItemsList;
    private Button buttonSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("Main Activity","Main Activity \r\n");

        this.prefs = getSharedPreferences("con.example.carlos.beaconcomercial",MODE_PRIVATE);

        progressBar = findViewById(R.id.progressBar);
        buttonList = findViewById(R.id.buttonList);
        buttonItems = findViewById(R.id.buttonItems);
        buttonItemsList = findViewById(R.id.buttonItemsList);
        buttonSync = findViewById(R.id.buttonSync);


        progressBar.setVisibility(View.INVISIBLE);
        buttonSync.setVisibility(View.INVISIBLE);

        buttonItemsList.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                launchItemsList();
            }
        });
        buttonItems.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                launchItems();
            }
        });
        buttonList.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                launchList();
            }
        });

        syncBeacons();

    }

    private void syncBeacons(){
        prefs = getSharedPreferences("con.example.carlos.beaconcomercial",MODE_PRIVATE);
        if (prefs.getBoolean("firstrun", true)) {
            // En la primer ejecución de la app registro el mail del dispositivo a la base de datos
            Log.d(TAG,"Firts Run");
            prefs.edit().putBoolean("firstrun",false);
            String device_id = "UserEmailFetcher.getEmail(getApplicationContext())";

            prefs.edit().putString("device_id", device_id).commit();
            prefs.edit().putBoolean("firstrun", false).commit();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiUtils.BaseURL)
                    .build();

            BeaconApi service = retrofit.create(BeaconApi.class);
            service.postDevice(new Device(device_id)).enqueue(new Callback<Device>() {
                @Override
                public void onResponse(Call<Device> call, Response<Device> response) {
                    Log.d("Success",response.toString());
                }

                @Override
                public void onFailure(Call<Device> call, Throwable t) {
                    Log.d("Success",t.toString());
                }
            });
            Gson g = new Gson();
            prefs.edit().putString("beacons", g.toJson(service.getBeacons())).commit();
        }

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.GET_ACCOUNTS)!= PackageManager.PERMISSION_GRANTED  && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.GET_ACCOUNTS,Manifest.permission.ACCESS_COARSE_LOCATION},MY_PERMISSIONS_REQUEST);
        else{
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.GET_ACCOUNTS)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.GET_ACCOUNTS},MY_PERMISSIONS_READ_CONTACTS);
            }
            else{
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    TextView txt = findViewById(R.id.textViewPerm);
                    txt.setVisibility(View.INVISIBLE);
                }
                if(grantResults[1] == PackageManager.PERMISSION_DENIED){
                    TextView txt = findViewById(R.id.textViewPerm);
                    txt.setText("Para el funcionamiento de la aplicación debe proveer permisos de localización a la app.");
                    txt.setTextColor(Color.RED);
                }
                return;
            }
            case MY_PERMISSIONS_READ_CONTACTS:{}
            case MY_PERMISSIONS_ACCESS_COARSE_LOCATION:{
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    TextView txt = findViewById(R.id.textViewPerm);
                    txt.setVisibility(View.INVISIBLE);
                }
                if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                    TextView txt = findViewById(R.id.textViewPerm);
                    txt.setText("Para el funcionamiento de la aplicación debe proveer permisos de localización a la app.");
                    txt.setTextColor(Color.RED);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            TextView txt =(TextView) findViewById(R.id.textViewPerm);
            txt.setVisibility(View.INVISIBLE);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED){
            TextView txt =(TextView) findViewById(R.id.textViewPerm);
            txt.setText("Para el funcionamiento de la aplicación debe proveer permisos de localización a la app.");
            txt.setTextColor(Color.RED);
        }
    }

    /*
        launchList Método del botón Lista de Beacons
     */
    private void launchList(){
        Intent i = new Intent(this, ListRangeActivity.class);
        startActivity(i);
    }

    /*
        launchItems Método del botón Lista de Items
     */
    private void launchItems(){
        Intent i = new Intent(this, BeaconListActivity.class);
        startActivity(i);
    }

    private void launchItemsList(){
        if(prefs.getString("itemsList",null) != null) {
            Intent i = new Intent(this, ItemsListActivity.class);
            startActivity(i);
        }
        else{
            Toast.makeText(MainActivity.this, "No hay lista activa", Toast.LENGTH_SHORT).show();
        }
    }

}