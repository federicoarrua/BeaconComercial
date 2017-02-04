package com.example.carlos.beaconcomercial.servertasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.carlos.beaconcomercial.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Federico on 10/12/2016.
 * AsyncTask que recibe los parametros de major_region_id y minor_region_id y hace un GET a la API
 * BeaconTaller.
 * GET /beacons/showregion.json?major_region_id=?&minor_region_id=?
 * return json de la respuesta o null en caso de que no haya respuesta.
 */

public class BeaconByRegionGetTask extends AsyncTask<HashMap<String,Integer>,Void,Object> {
    @Override
    protected Object doInBackground(HashMap<String,Integer>... params) {
        HashMap<String,Integer> regions = params[0];

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(Constants.URL+"/beacons/showregion.json?major_region_id="+regions.get("major_region_id")+"&minor_region_id="+regions.get("minor_region_id"));
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();// In this buffer i save the whole response
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
                Log.d("Response: ", "> " + line);
            }

            return buffer.toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
