package com.example.carlos.beaconcomercial.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.carlos.beaconcomercial.R;
import com.example.carlos.beaconcomercial.classesBeacon.BeaconModel;
import com.example.carlos.beaconcomercial.servertasks.BeaconsGetTask;
import com.example.carlos.beaconcomercial.utils.BeaconJsonUtils;
import com.google.gson.Gson;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Carlos on 12/01/2017.
 */

public class ItemsActivity extends ListActivity {

    private String TAG = "RangingDetectBeacon";

    //Adaptadores para la ListView
    private ArrayList<String> listItems=new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private SharedPreferences prefs;

    //Colección de beacons que encuentro
    private Collection<Beacon> beaconCollection;

    //Arreglo de beacons registrados en la base de datos
    private BeaconModel[] beaconModelArray;
    private Boolean[] selected;
    private List<BeaconModel> itemList;

    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.items_layout);

        //Inicialización Adaptadores para la listView
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,listItems);
        setListAdapter(adapter);

        //Inicialización del arreglo de beacons de la base de datos
        prefs = getSharedPreferences("con.example.carlos.beaconcomercial",MODE_PRIVATE);

        try {
            String beaconsJson = (String) new BeaconsGetTask().execute().get();
            prefs.edit().putString("beacons", beaconsJson).commit();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        beaconModelArray = BeaconJsonUtils.JsonToBeaconArray(prefs.getString("beacons",null));
        selected = new Boolean[beaconModelArray.length];
        for(int i=0;i<selected.length;i++){
            selected[i] = new Boolean(false);
        }
        itemList = new ArrayList<BeaconModel>();

        EditText inputSearch = (EditText) findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                ItemsActivity.this.adapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });

        ListView lv = getListView();
        addItems();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(!selected[position]) {
                    view.setBackgroundColor(Color.parseColor("#ffff00"));
                    selected[position] = true;
                }
                else{
                    view.setBackgroundColor(getResources().getColor(R.color.background_holo_light));
                    selected[position] = false;
                }
            }
        });
        beaconManager = BeaconManager.getInstanceForApplication(this);
        Button b = (Button) findViewById(R.id.button_listitems);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i =0 ; i<selected.length;i++){
                    if(selected[i])
                        itemList.add(beaconModelArray[i]);
                }
                if(itemList.size() >0){
                    if(prefs.getString("itemsList",null) != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ItemsActivity.this);
                        builder.setMessage("Al crear una nueva lista se elimina la anterior. Está seguro?")
                                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        stopMonitor();
                                        for (int i = 0; i < selected.length; i++) {
                                            if (selected[i]) {
                                                try {
                                                    beaconManager.startMonitoringBeaconsInRegion(new Region(beaconModelArray[i].getName(), null, Identifier.parse(beaconModelArray[i].getMajor_region_id().toString()), Identifier.parse(beaconModelArray[i].getMinor_region_id().toString())));
                                                } catch (RemoteException re) {
                                                    re.printStackTrace();
                                                }
                                            }
                                        }
                                        //Guardo la lista de items escogidos en formato Json para recuperarla mas fácil de las SharedPrefs
                                        prefs.edit().putString("itemsList", new Gson().toJson(itemList)).commit();
                                        finish();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do things
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    else{
                        stopMonitor();
                        for (int i = 0; i < selected.length; i++) {
                            if (selected[i]) {
                                try {
                                    beaconManager.startMonitoringBeaconsInRegion(new Region(beaconModelArray[i].getName(), null, Identifier.parse(beaconModelArray[i].getMajor_region_id().toString()), Identifier.parse(beaconModelArray[i].getMinor_region_id().toString())));
                                } catch (RemoteException re) {
                                    re.printStackTrace();
                                }
                            }
                        }
                        //Guardo la lista de items escogidos en formato Json para recuperarla mas fácil de las SharedPrefs
                        prefs.edit().putString("itemsList", new Gson().toJson(itemList)).commit();
                        finish();
                    }
                }
                else
                    Toast.makeText(ItemsActivity.this, "La lista debe tener al menos un ítem", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addItems() {
        listItems=new ArrayList<>();
        String item;
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,listItems);
        Integer i;

        for ( i = 0 ; i<beaconModelArray.length ;i++) {
            BeaconModel b = beaconModelArray[i];
            item= b.getName() + " :\r\n" +i.toString()+" Major: "+b.getMajor_region_id()+" Minor: "+b.getMinor_region_id();
            listItems.add(item);
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

    private void stopMonitor(){
        //Recupero la lista de items antigua
        String s = prefs.getString("itemsList",null);
        if(s != null) {
            BeaconModel[] bm = BeaconJsonUtils.JsonToBeaconArray(s);
            for (int i = 0; i < bm.length; i++) {
                try {
                    beaconManager.stopMonitoringBeaconsInRegion(new Region(bm[i].getName(), null, Identifier.parse(bm[i].getMajor_region_id().toString()), Identifier.parse(bm[i].getMinor_region_id().toString())));
                } catch (RemoteException re) {
                    re.printStackTrace();
                }
            }
        }
    }
}
