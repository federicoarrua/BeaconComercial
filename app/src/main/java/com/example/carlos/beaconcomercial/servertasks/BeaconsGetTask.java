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

/**
 * Created by Federico on 10/11/2016.
 * AsyncTask que hace un GET a la API BeaconTaller para recibir la lista de todos los beacons en
 * formato json
 * GET /beacons.json
 * return json con todos los beacons de la base de datos
 */

public class BeaconsGetTask extends AsyncTask{

    @Override
    protected Object doInBackground(Object[] params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(Constants.URL+"/beacons.json");
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
                Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
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
