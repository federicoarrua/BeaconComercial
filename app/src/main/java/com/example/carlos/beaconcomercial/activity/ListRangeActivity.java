package com.example.carlos.beaconcomercial.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.carlos.beaconcomercial.R;
import com.example.carlos.beaconcomercial.classesBeacon.BeaconModel;
import com.example.carlos.beaconcomercial.servertasks.DiscoverPostTask;
import com.example.carlos.beaconcomercial.utils.BeaconJsonUtils;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Federico on 13/12/2016.
 */

public class ListRangeActivity extends ListActivity implements BeaconConsumer {

    private String TAG = "RangingDetectBeacon";
    private BeaconManager beaconManager;

    //Adaptadores para la ListView
    ArrayList<String> listItems=new ArrayList<>();
    ArrayAdapter<String> adapter;

    private SharedPreferences prefs;

    //Colección de beacons que encuentro
    Collection<Beacon> beaconCollection;

    //Arreglo de beacons registrados en la base de datos
    BeaconModel[] beaconModelArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);

        //Inicialización Adaptadores para la listView
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,listItems);
        setListAdapter(adapter);

        //Inicialización del arreglo de beacons de la base de datos
        prefs = getSharedPreferences("con.example.carlos.beaconcomercial",MODE_PRIVATE);
        beaconModelArray = BeaconJsonUtils.JsonToBeaconArray(prefs.getString("beacons",null));

        ListView lv = getListView();

        //OnItemClickListener dispara la vista con la descripción del beacon seleccionado
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int i =0;
                Beacon b;
                BeaconModel bm = new BeaconModel();
                for (Iterator iterator = beaconCollection.iterator(); iterator.hasNext();) {
                    if(i == position) {
                        b = (Beacon) iterator.next();
                        bm.setMinor_region_id(b.getId3().toInt());
                        bm.setMajor_region_id(b.getId2().toInt());
                        bm.setDescription("Alguna descripción");
                    }
                    else
                        iterator.next();
                    i++;
                }

                Intent intent = new Intent(ListRangeActivity.this,RangingActivity.class);
                intent.putExtra("beacon",bm);
                startActivity(intent);
            }
        });

        //Inicialización beaconManager
        this.beaconManager = BeaconManager.getInstanceForApplication(this);
        this.beaconManager.setForegroundScanPeriod(1900l);
        this.beaconManager.setForegroundBetweenScanPeriod(0l);
        
        //Descomentar si el regionBootstrap esta inicializado en la aplicación
        //((ApplicationBeacon)this.getApplication()).stopBeaconMonitoring();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.bind(this);
    }

    @Override
    /*
       onBeaconServiceConnect() encargado de iniciar lectura de beacons
        añade un rangeNotifier para cualquier región y monitorea en las
       regiones de la base de datos para publicar descubrimientos
     */
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {

            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                beaconCollection = beacons;
                if (beacons.size()>0 ){
                    //Añado items a la listView
                    addItems(beacons);
                }
                else{
                    Log.i(TAG,"No beacons in this region.\r\n");
                    noItems();
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
            beaconManager.startRangingBeaconsInRegion(new Region("Ranging region",null, null,null));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
        try {
            beaconManager.stopRangingBeaconsInRegion(new Region(("Ranging region"), null, null, null));
        }
        catch(RemoteException re){
            re.printStackTrace();
        }
    }

    /*
        addItems método privado que añade items a la listView
     */
    private void addItems(Collection<Beacon> beacons) {
        listItems=new ArrayList<>();
        String item;
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,listItems);
        Integer i=0;

        for (Beacon b: beacons) {
            DecimalFormat df = new DecimalFormat("0.00");
            item= getNameBeacon(b.getId2().toInt(),b.getId3().toInt()) + " :\r\n" +i.toString()+". UUID: "+ b.getId1()+" Major: "+b.getId2()+" Minor: "+b.getId3()+" Distancia: " +df.format(beacons.iterator().next().getDistance())+"m";
            listItems.add(item);
            i++;
        }
        runOnUiThread(new Runnable() {
            public void run() {
                setListAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void noItems() {
        listItems=new ArrayList<>();
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,listItems);
        runOnUiThread(new Runnable() {
            public void run() {
                setListAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
    }
    private String getNameBeacon(int major_region_id,int minor_region_id){
        for(int i=0; i<beaconModelArray.length;i++){
            if(beaconModelArray[i].getMinor_region_id()==minor_region_id && beaconModelArray[i].getMajor_region_id()==major_region_id )
                return "Nombre: " + beaconModelArray[i].getName();
        }
        return "El beacon no está en la base de datos";
    }
}
