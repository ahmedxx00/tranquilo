package com.vegas.tranquilo.models;

import java.io.Serializable;

public class delivery_point implements Serializable {
    private double lat,lng;

    public delivery_point(double lat, double lng) {
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
