package com.example.carlos.beaconcomercial.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.carlos.beaconcomercial.R;
import com.example.carlos.beaconcomercial.classesBeacon.BeaconModel;
import com.example.carlos.beaconcomercial.servertasks.BeaconByRegionGetTask;
import com.example.carlos.beaconcomercial.servertasks.DiscoverPostTask;
import com.example.carlos.beaconcomercial.utils.BeaconJsonUtils;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Federico on 24/10/2016.
 * RangingActivity visualiza un beacon con los detalles del mismo que están en la base de datos y
 * la distancia si es que sigue dentro del rango.
 */

public class RangingActivity extends Activity implements BeaconConsumer {

    private String TAG = "RangingDetectBeacon";

    private BeaconManager beaconManager;

    private SharedPreferences prefs;

    //Beacon del cual muestro la información
    private BeaconModel beacon;

    //Usada al principio para iniciar monitoreo
    private BeaconModel[] beaconModelArray;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.range_layout);

        //Recibo el beacon a visualizar desde la vista anterior
        beacon = (BeaconModel) getIntent().getSerializableExtra("beacon");

        //Obtengo lista de beacons en base de datos
        prefs = getSharedPreferences("con.example.carlos.beaconcomercial",MODE_PRIVATE);
        beaconModelArray = BeaconJsonUtils.JsonToBeaconArray(prefs.getString("beacons",null));

        ((Button) findViewById(R.id.button_remove)).setVisibility(View.INVISIBLE);

        //HashMap usado para pasar a la AsyncTask que recupera al beacon
        HashMap<String,Integer> hm = new HashMap<>();
        hm.put("minor_region_id",beacon.getMinor_region_id());
        hm.put("major_region_id",beacon.getMajor_region_id());

        try {
            //Recupero el beacon desde el servidor
            String json =(String) new BeaconByRegionGetTask().execute(hm).get();
            TextView txtDesc = (TextView) findViewById(R.id.textViewDesc);
            TextView txtName = (TextView) findViewById(R.id.textViewName);

            //Si json es null significa que el beacon no esta en la base de datos
            if(json != null) {
                BeaconModel b = BeaconJsonUtils.JsonToBeacon(json);
                txtName.setText(b.getName());
                txtDesc.setText(b.getDescription());
            }
            else{
                txtName.setText("No hay nombre");
                txtDesc.setText("El beacon no está en la base de datos.");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        //Inicialización de beaconManager
        this.beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            beaconManager.stopRangingBeaconsInRegion(new Region(beacon.getDescription(),null, Identifier.parse(beacon.getMajor_region_id().toString()),Identifier.parse(beacon.getMinor_region_id().toString())));
        }
        catch(RemoteException re){
            re.printStackTrace();
        }
        beaconManager.unbind(this);
    }

    @Override
    /*
       onBeaconServiceConnect() encargado de iniciar lectura de beacons
       añade un rangeNotifier para la región de la variable beacon y monitorea en las
       regiones de la base de datos para publicar descubrimientos
     */
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {

            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                boolean found = false;
                if (beacons.size()>0){
                    for (Beacon b: beacons) {
                        if(b.getId2().toInt() == beacon.getMajor_region_id() && b.getId3().toInt() == beacon.getMinor_region_id()) {
                            DecimalFormat df = new DecimalFormat("0.00");
                            logToDisplay(df.format(beacons.iterator().next().getDistance()));
                            found = true;
                            break;
                        }
                    }
                }
                if(found == false){
                    Log.i(TAG,"No beacons in this region.\r\n");
                    logToDisplayNoBeacon();
                }

            }
        });
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.d(TAG, "Got a didEnterRegion call");

                BeaconModel b = new BeaconModel();
                b.setMajor_region_id(region.getId2().toInt());
                b.setMinor_region_id(region.getId3().toInt());
                b.setDescription(region.getUniqueId());

                HashMap<String,String> ids = new HashMap<>();
                ids.put("device_id",prefs.getString("device_id",null));
                ids.put("major_region_id",b.getMajor_region_id().toString());
                ids.put("minor_region_id",b.getMinor_region_id().toString());
                new DiscoverPostTask().execute(ids);

                Log.d(TAG,"NOTIFICACION "+region.toString());
            }

            @Override
            public void didExitRegion(Region region) {

            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });
        try{
            beaconManager.startRangingBeaconsInRegion(new Region(beacon.getDescription(),null, Identifier.parse(beacon.getMajor_region_id().toString()),Identifier.parse(beacon.getMinor_region_id().toString())));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
        logToDisplay método privado para graficar la distancia al beacon
     */
    private void logToDisplay( final String dist) {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView distance = (TextView) findViewById(R.id.textViewDist);
                distance.setText("Esta a "+dist+"m de distancia");
            }
        });
    }

    /*
        logToDisplayNoBeacon método privado para gráficar que no estoy cerca del beacon
     */
    private void logToDisplayNoBeacon() {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView distance = (TextView) findViewById(R.id.textViewDist);
                distance.setText("No estás dentro de la región del beacon.");
            }
        });
    }
    
}
