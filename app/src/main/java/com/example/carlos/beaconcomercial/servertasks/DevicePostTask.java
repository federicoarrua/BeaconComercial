package com.example.carlos.beaconcomercial.servertasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.carlos.beaconcomercial.Constants;
import com.example.carlos.beaconcomercial.classesBeacon.Device;
import com.example.carlos.beaconcomercial.utils.BeaconJsonUtils;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by Federico on 10/11/2016.
 * AsyncTask que recibe el mail del celular anfitrión y hace un POST a la API de BeaconTaller con
 * el email como cuerpo del request.
 * POST /devices.json
 */

public class DevicePostTask extends AsyncTask<HashMap<String,String>,Void,Object> {

    private static final String TAG ="DEVICE_POST_TASK";

    @Override
    protected Object doInBackground(HashMap<String,String>... params) {
        HashMap<String,String> p = params[0];

        Device device = new Device();

        device.setDevice_id(p.get("device_id"));

        String json = BeaconJsonUtils.DeviceToJson(device);

        try {
            HttpPost httpPost = new HttpPost(Constants.URL+"/devices.json");
            httpPost.setEntity(new StringEntity(json));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            return new DefaultHttpClient().execute(httpPost);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {

        super.onPostExecute(o);
        BasicHttpResponse bo = (BasicHttpResponse) o;
        Log.d(TAG,bo.getStatusLine().toString());
    }
}
