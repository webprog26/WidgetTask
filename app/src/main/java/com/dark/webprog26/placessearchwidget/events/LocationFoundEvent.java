package com.dark.webprog26.placessearchwidget.events;

import com.dark.webprog26.placessearchwidget.models.LocationModel;

/**
 * Created by webpr on 03.03.2017.
 */

public class LocationFoundEvent {

    /**
     * When user location is known we should sent this appropriate event
     * to {@link com.dark.webprog26.placessearchwidget.MapsActivity}
     */

    private final LocationModel mLocationModel;

    public LocationFoundEvent(LocationModel locationModel) {
        this.mLocationModel = locationModel;
    }

    public LocationModel getLocationModel() {
        return mLocationModel;
    }
}
