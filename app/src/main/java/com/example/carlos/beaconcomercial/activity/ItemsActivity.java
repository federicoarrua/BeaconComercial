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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.carlos.beaconcomercial.R;
import com.example.carlos.beaconcomercial.api.ApiUtils;
import com.example.carlos.beaconcomercial.api.BeaconApi;
import com.example.carlos.beaconcomercial.classesBeacon.BeaconModel;
import com.example.carlos.beaconcomercial.utils.BeaconJsonUtils;
import com.google.gson.Gson;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Carlos on 12/01/2017.
 * ItemsActivity lista los items de la base de datos y permite armar una lista a monitorear
 */

public class ItemsActivity extends ListActivity {

    private String TAG = "RangingDetectBeacon";

    //Adaptadores para la ListView
    private ArrayList<String> listItems;
    private ArrayAdapter<String> adapter;
    private Button buttonListItem;

    private SharedPreferences prefs;

    //Colección de beacons que encuentro
    private Collection<Beacon> beaconCollection;

    //Arreglo de beacons registrados en la base de datos
    private List<BeaconModel> beaconModelList;
    private Boolean[] selected;
    private List<BeaconModel> itemList;

    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.items_layout);

        //Inicialización Adaptadores para la listView
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,listItems);

        //Inicialización del arreglo de beacons de la base de datos
        prefs = getSharedPreferences("con.example.carlos.beaconcomercial",MODE_PRIVATE);

        itemList = new ArrayList<>();
        listItems = new ArrayList<>();

        //Barra search
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

        //Cambio el color de los items seleccionados
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
        buttonListItem = findViewById(R.id.button_listitems);

        getBeaconsList();
    }

    //Añade items a la listview
    private void addItems() {
        String item;

        for (Integer i = 0 ; i< beaconModelList.size() ; i++) {
            BeaconModel b = beaconModelList.get(i);
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

    //Metodo para dejar de monitorear al eliminar la lista antigua
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

    private void getBeaconsList(){
        BeaconApi service = ApiUtils.getAPIService();
        service.getBeacons().enqueue(new Callback<List<BeaconModel>>() {
            @Override
            public void onResponse(Call<List<BeaconModel>> call, Response<List<BeaconModel>> response) {
                if(response.isSuccessful()){
                    beaconModelList = response.body();
                    for (BeaconModel b : beaconModelList)
                        listItems.add(b.getName() + " :\r\n" +" Major: "+b.getMajor_region_id()+" Minor: "+b.getMinor_region_id());
                    selected = new Boolean[beaconModelList.size()];
                    for(int i=0;i<selected.length;i++){
                        selected[i] = new Boolean(false);
                    }
                    setListAdapter(adapter);
                    addItems();
                    //Al hacer click creo la lista de items a monitorear
                    buttonListItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for(int i =0 ; i<selected.length;i++){
                                if(selected[i])
                                    itemList.add(beaconModelList.get(i));
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
                                                                beaconManager.startMonitoringBeaconsInRegion(new Region(beaconModelList.get(i).getName(), null, Identifier.parse(beaconModelList.get(i).getMajor_region_id().toString()), Identifier.parse(beaconModelList.get(i).getMinor_region_id().toString())));
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
                                                beaconManager.startMonitoringBeaconsInRegion(new Region(beaconModelList.get(i).getName(), null, Identifier.parse(beaconModelList.get(i).getMajor_region_id().toString()), Identifier.parse(beaconModelList.get(i).getMinor_region_id().toString())));
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
                else
                    Log.d(TAG,"ERROR AL SINCRONIZAR LISTA");
            }

            @Override
            public void onFailure(Call<List<BeaconModel>> call, Throwable t) {
                Log.d(TAG,"ERROR DE RED");
            }
        });
    }
}
