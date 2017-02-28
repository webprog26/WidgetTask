package com.dark.webprog26.placessearchwidget.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by webpr on 27.02.2017.
 */

public class PlaceModel implements Serializable {

    @SerializedName("geometry")
    private GeometryModel mGeometry;
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;

    public PlaceModel(GeometryModel geometry, String id, String name) {
        this.mGeometry = geometry;
        this.id = id;
        this.name = name;
    }

    public GeometryModel getGeometry() {
        return mGeometry;
    }

    public void setGeometry(GeometryModel mGeometry) {
        this.mGeometry = mGeometry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
