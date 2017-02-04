package com.example.carlos.beaconcomercial.utils;

import com.example.carlos.beaconcomercial.classesBeacon.BeaconModel;
import com.example.carlos.beaconcomercial.classesBeacon.Device;
import com.example.carlos.beaconcomercial.classesBeacon.Discover;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Federico on 08/12/2016.
 * Utilidades para transformar clases del paquete classesBeacon a json y viceversa
 */

public class BeaconJsonUtils {


    /*
        Returns the json in a Beacon Array
     */
    public static BeaconModel[] JsonToBeaconArray(String json){
        Gson g = new Gson();
        return g.fromJson(json,BeaconModel[].class);
    }

    /*
        Returns the json in a list of Beacons
     */
    public static List<BeaconModel> JsonToBeaconList(String json){
        Gson gson = new Gson();
        Type beaconListType = new TypeToken<ArrayList<BeaconModel>>(){}.getType();
        return gson.fromJson(json, beaconListType);
    }

    public static BeaconModel JsonToBeacon(String json){
        Gson gson = new Gson();
        return gson.fromJson(json,BeaconModel.class);
    }

    public static String DiscoverToJson(Discover d){
        Gson g = new Gson();
        return g.toJson(d);
    }

    public static String DeviceToJson(Device d){
        Gson g = new Gson();
        return g.toJson(d);
    }
}
