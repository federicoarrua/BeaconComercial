package com.example.carlos.beaconcomercial.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.carlos.beaconcomercial.utils.BeaconJsonUtils;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Carlos on 27/01/2017.
 */

public class ItemsListActivity extends ListActivity {

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
        setContentView(R.layout.items_list_layout);

        //Inicialización Adaptadores para la listView
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,listItems);
        setListAdapter(adapter);

        //Inicialización del arreglo de beacons de la base de datos
        prefs = getSharedPreferences("con.example.carlos.beaconcomercial",MODE_PRIVATE);
        beaconModelArray = BeaconJsonUtils.JsonToBeaconArray(prefs.getString("itemsList",null));
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
                ItemsListActivity.this.adapter.getFilter().filter(cs);
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
        //addItems();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BeaconModel bm = beaconModelArray[position];

                Intent intent = new Intent(ItemsListActivity.this,ItemListActivity.class);
                intent.putExtra("beacon",bm);
                startActivity(intent);
            }
        });

        beaconManager = BeaconManager.getInstanceForApplication(this);

        Button b = (Button) findViewById(R.id.button_eraselist);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMonitor();
                prefs.edit().remove("itemsList").commit();
                Toast.makeText(ItemsListActivity.this, "Lista Borrada", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconModelArray = BeaconJsonUtils.JsonToBeaconArray(prefs.getString("itemsList",null));
        if(beaconModelArray != null)
            addItems();
        else
            finish();
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
