package com.example.hoauy.carrottv.item;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by hoauy on 2018-06-04.
 */

public class Marker_Item implements ClusterItem{

    double lat;
    double lon;
    String image_address;
    LatLng location;

    public Marker_Item(double lat, double lon, String image_address)
    {
        this.lat = lat;
        this.lon = lon;
        this.image_address = image_address;
        location = new LatLng(lat, lon);

    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setImage_address(String image_address) {
        this.image_address = image_address;
    }

    public String getImage_address() {
        return image_address;
    }

    @Override
    public LatLng getPosition() {

        return location;

    }
}
