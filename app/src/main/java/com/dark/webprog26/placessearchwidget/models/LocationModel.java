package com.dark.webprog26.placessearchwidget.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by webpr on 27.02.2017.
 */

public class LocationModel {

    @SerializedName("lat")
    private double mLat;
    @SerializedName("lng")
    private double mLng;

    public LocationModel(double lat, double lng) {
        this.mLat = lat;
        this.mLng = lng;
    }

    public double getLat() {
        return mLat;
    }

    public void setLat(double mLat) {
        this.mLat = mLat;
    }

    public double getLng() {
        return mLng;
    }

    public void setLng(double mLng) {
        this.mLng = mLng;
    }
}
