package com.example.carlos.beaconcomercial.classesBeacon;

/**
 * Created by Federico on 08/12/2016.
 * BeaconModel Implementa los campos que se reciben en el json de la API BeaconTaller
 */

public class Discover {

    private String device_id;
    private Integer major_id;
    private Integer minor_id;
    private String discover_time;

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public Integer getMajor_id() {
        return major_id;
    }

    public void setMajor_id(Integer major_id) {
        this.major_id = major_id;
    }

    public Integer getMinor_id() {
        return minor_id;
    }

    public void setMinor_id(Integer minor_id) {
        this.minor_id = minor_id;
    }

    public String getDiscover_time() {
        return discover_time;
    }

    public void setDiscover_time(String discover_time) {
        this.discover_time = discover_time;
    }
}
