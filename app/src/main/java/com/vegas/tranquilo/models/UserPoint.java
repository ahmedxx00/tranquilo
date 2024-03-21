package com.vegas.tranquilo.models;

import java.io.Serializable;

public class UserPoint implements Serializable {

    private double lat,lng;

    public UserPoint(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }


}
