package com.dark.webprog26.placessearchwidget.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by webpr on 27.02.2017.
 */

public class PlacesResponseModel {

    @SerializedName("results")
    private List<PlaceModel> placeResults;

    public List<PlaceModel> getPlaceResults() {
        return placeResults;
    }

    public void setPlaceResults(List<PlaceModel> placeResults) {
        this.placeResults = placeResults;
    }
}
