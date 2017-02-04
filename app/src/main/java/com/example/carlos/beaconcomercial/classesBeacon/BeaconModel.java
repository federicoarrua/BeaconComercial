package com.example.carlos.beaconcomercial.classesBeacon;

import java.io.Serializable;

/**
 * Created by Federico on 02/12/2016.
 * BeaconModel Implementa los campos que se reciben en el json de la API BeaconTaller
 * implementa Serializable para pasar entre actividades.
 */

public class BeaconModel implements Serializable {

    private Integer id;
    private String name;
    private Integer major_region_id;
    private Integer minor_region_id;
    private String description;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMajor_region_id() {
        return major_region_id;
    }

    public void setMajor_region_id(Integer major_region_id) {
        this.major_region_id = major_region_id;
    }

    public Integer getMinor_region_id() {
        return minor_region_id;
    }

    public void setMinor_region_id(Integer minor_region_id) {
        this.minor_region_id = minor_region_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
