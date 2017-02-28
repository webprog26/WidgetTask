package com.dark.webprog26.placessearchwidget.events;

import android.graphics.Bitmap;

import com.dark.webprog26.placessearchwidget.models.PlaceModel;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by webpr on 28.02.2017.
 */

public class PlacesIconsReadyEvent {

    private final Map<PlaceModel, Bitmap> mPlacesWithIconsMap;

    public PlacesIconsReadyEvent(Map<PlaceModel, Bitmap> placesWithIconsMap) {
        this.mPlacesWithIconsMap = placesWithIconsMap;
    }

    public Map<PlaceModel, Bitmap> getPlaceModelsWithIcons() {
        return mPlacesWithIconsMap;
    }
}
