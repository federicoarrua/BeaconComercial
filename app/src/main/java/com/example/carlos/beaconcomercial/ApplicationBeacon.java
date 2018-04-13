package com.example.carlos.beaconcomercial;

import android.Manifest;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import com.example.carlos.beaconcomercial.activity.ItemListActivity;
import com.example.carlos.beaconcomercial.classesBeacon.BeaconModel;
import com.example.carlos.beaconcomercial.servertasks.DiscoverPostTask;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.HashMap;

/**
 * Created by Federico on 31/10/2016.
 * implementa BootstrapNotifier para monitorear beacons en background
 */

public class ApplicationBeacon extends Application implements BootstrapNotifier {

    private static final String TAG = " ApplicationBeacon";
    private static final int NOTIFICATION_ID = 123;
    public RegionBootstrap regionBootstrap;
    private SharedPreferences prefs;

    @RequiresPermission(Manifest.permission.ACCOUNT_MANAGER)
    public void onCreate() {
        super.onCreate();

        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);

        //Seteo periodos de escaneo en ms
        beaconManager.setBackgroundScanPeriod(1000l);
        beaconManager.setBackgroundBetweenScanPeriod(0l);

        //Seteo en cuanto tiempo sin recibir mensajes beacon considero que sali de la región en ms
        beaconManager.setRegionExitPeriod(2000l);

        //iBeacon Parser
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));

        try {
            //Código para monitorear en background y lanzar notificaciones
            regionBootstrap = new RegionBootstrap(this,new Region("todos",Identifier.parse("1"),Identifier.parse("1"),Identifier.parse("1")));
            Log.d(TAG,"Inititialization Completed!");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    /*
    didEnterRegion(Region region) se ejecuta este metodo cada vez que se comienza a recibir algún beacon
     */
    @Override
    public void didEnterRegion(Region region) {
        Log.d(TAG, "Got a didEnterRegion call: ");

        BeaconModel b = new BeaconModel();
        b.setName(region.getUniqueId());
        b.setMajor_region_id(region.getId2().toInt());
        b.setMinor_region_id(region.getId3().toInt());
        b.setDescription(region.getUniqueId());

        HashMap<String,String> ids = new HashMap<>();
        ids.put("device_id",prefs.getString("device_id",null));
        ids.put("major_region_id",b.getMajor_region_id().toString());
        ids.put("minor_region_id",b.getMinor_region_id().toString());
        new DiscoverPostTask().execute(ids);

        showNotification("Beacon Notification","Estas cerca de \""+b.getName()+"\"",b);
        Log.d(TAG,"NOTIFICACION "+region.toString());

    }


    /*
    didExitRegion(Region region) se ejecuta este metodo cada vez que se sale de una zona beacon
     */
    @Override
    public void didExitRegion(Region region) {
        Log.d(TAG, "Got a didExitRegion call");

        BeaconModel b = new BeaconModel();
        b.setName(region.getUniqueId());
        b.setMajor_region_id(region.getId2().toInt());
        b.setMinor_region_id(region.getId3().toInt());
        b.setDescription(region.getUniqueId());

        showNotification("Beacon Notification","Te alejaste de \""+b.getName()+"\"",b);
        Log.d(TAG,"NOTIFICACION "+region.toString());
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }

    //Armo la notificación a visualizar
    public void showNotification(String title, String message , BeaconModel b) {
        Intent notifyIntent = new Intent(this, ItemListActivity.class);
        notifyIntent.putExtra("beacon",b);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

}
