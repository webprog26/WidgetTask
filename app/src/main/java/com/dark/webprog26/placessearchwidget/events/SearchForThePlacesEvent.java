package com.dark.webprog26.placessearchwidget.events;

/**
 * Created by webpr on 28.02.2017.
 */

public class SearchForThePlacesEvent {

    private final String mRequest;
    private final String mLocationString;

    public SearchForThePlacesEvent(String mLocationString, String mRequest) {
        this.mLocationString = mLocationString;
        this.mRequest = mRequest;
    }

    public String getRequest() {
        return mRequest;
    }

    public String getLocationString() {
        return mLocationString;
    }
}
