package com.dark.webprog26.placessearchwidget.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by webpr on 27.02.2017.
 */

public class GeometryModel implements Serializable{

    @SerializedName("location")
    private LocationModel mLocation;

    public GeometryModel(LocationModel location) {
        this.mLocation = location;
    }

    public LocationModel getLocation() {
        return mLocation;
    }

    public void setLocation(LocationModel mLocation) {
        this.mLocation = mLocation;
    }
}
