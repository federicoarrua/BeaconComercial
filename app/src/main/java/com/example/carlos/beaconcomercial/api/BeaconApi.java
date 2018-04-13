package com.example.carlos.beaconcomercial.api;

import com.example.carlos.beaconcomercial.classesBeacon.BeaconModel;
import com.example.carlos.beaconcomercial.classesBeacon.Device;
import com.example.carlos.beaconcomercial.classesBeacon.Discover;

import org.altbeacon.beacon.Beacon;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by farrua on 26/02/2018.
 */

public interface BeaconApi {

    @GET("beacons.json")
    Call<List<BeaconModel>> getBeacons();

    @GET("beacons/showregion.json?major_region_id={major_region_id}&minor_region_id={minor_region_id}")
    Call<List<BeaconModel>> getBeaconsByRegions(@Path("major_region_id") Integer majorRegionId, @Path("minor_region_id") Integer minorRegionId);

    @POST("devices.json")
    Call<Device> postDevice(@Body Device device);

    @POST("discovers.json")
    Call<Discover> postDiscover(@Body Discover discover);
}
