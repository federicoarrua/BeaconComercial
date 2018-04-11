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
import com.example.carlos.beaconcomercial.servertasks.BeaconsGetTask;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("Main Activity","Main Activity \r\n");

        this.prefs = getSharedPreferences("con.example.carlos.beaconcomercial",MODE_PRIVATE);

        ProgressBar pg = (ProgressBar) findViewById(R.id.progressBar);
        pg.setVisibility(View.INVISIBLE);

        Button bList = (Button) findViewById(R.id.buttonList);
        bList.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                launchList();
            }
        });

        Button bItems = (Button) findViewById(R.id.buttonItems);
        bItems.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                launchItems();
            }
        });

        Button bItemsList = (Button) findViewById(R.id.buttonItemsList);
        bItemsList.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                launchItemsList();
            }
        });

        Button bSync = (Button) findViewById(R.id.buttonSync);
        bSync.setVisibility(View.INVISIBLE);

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
                    TextView txt =(TextView) findViewById(R.id.textViewPerm);
                    txt.setVisibility(View.INVISIBLE);
                }
                if(grantResults[1] == PackageManager.PERMISSION_DENIED){
                    TextView txt =(TextView) findViewById(R.id.textViewPerm);
                    txt.setText("Para el funcionamiento de la aplicación debe proveer permisos de localización a la app.");
                    txt.setTextColor(Color.RED);
                }
                return;
            }
            case MY_PERMISSIONS_READ_CONTACTS:{}
            case MY_PERMISSIONS_ACCESS_COARSE_LOCATION:{
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    TextView txt =(TextView) findViewById(R.id.textViewPerm);
                    txt.setVisibility(View.INVISIBLE);
                }
                if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                    TextView txt =(TextView) findViewById(R.id.textViewPerm);
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
        Intent i = new Intent(this, ItemsActivity.class);
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

    /*
        syncWithDatabase Método del botón sincronizar
     */
    private void syncWithDatabase(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProgressBar pg = (ProgressBar) findViewById(R.id.progressBar);
                pg.setVisibility(View.VISIBLE);
            }
        });
        try {
            String beaconsJson = (String) new BeaconsGetTask().execute().get();
            prefs.edit().putString("beacons", beaconsJson).commit();
            int duration = Toast.LENGTH_SHORT;
            Log.d(TAG,"Se sincronizó");
            Toast toast = Toast.makeText(this, "App Sincronizada", duration);
            toast.show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProgressBar pg = (ProgressBar) findViewById(R.id.progressBar);
                pg.setVisibility(View.INVISIBLE);
            }
        });

    }
}