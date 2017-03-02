package com.dark.webprog26.placessearchwidget.events;

import android.graphics.Bitmap;

import com.dark.webprog26.placessearchwidget.models.PlaceModel;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by webpr on 28.02.2017.
 */

public class PlacesIconsReadyEvent {
    /**
     * When places are loaded from Google Places Web API via retrofit library methods
     * we should load their icons and put into {@link Map}
     */
    private final Map<PlaceModel, Bitmap> mPlacesWithIconsMap;

    public PlacesIconsReadyEvent(Map<PlaceModel, Bitmap> placesWithIconsMap) {
        this.mPlacesWithIconsMap = placesWithIconsMap;
    }

    public Map<PlaceModel, Bitmap> getPlaceModelsWithIcons() {
        return mPlacesWithIconsMap;
    }
}
