package com.dark.webprog26.placessearchwidget.events;

import com.dark.webprog26.placessearchwidget.models.PlaceModel;

import java.util.ArrayList;

/**
 * Created by webpr on 28.02.2017.
 */

public class PlacesListReadyEvent {

    private final ArrayList<PlaceModel> mPlaceModels;

    public PlacesListReadyEvent(ArrayList<PlaceModel> mPlaceModels) {
        this.mPlaceModels = mPlaceModels;
    }

    public ArrayList<PlaceModel> getPlaceModels() {
        return mPlaceModels;
    }
}
