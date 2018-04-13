package com.example.carlos.beaconcomercial.api;

/**
 * Created by farrua on 26/02/2018.
 */

public class ApiUtils {
    public static final String URL = "http://beacontaller.herokuapp.com";
    public static final String BaseURL = "http://beacontaller.herokuapp.com";

    public static BeaconApi getAPIService() {
        return RetrofitClient.getClient().create(BeaconApi.class);
    }
}
