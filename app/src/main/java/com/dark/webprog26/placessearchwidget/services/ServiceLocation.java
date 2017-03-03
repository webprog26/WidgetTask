package com.dark.webprog26.placessearchwidget.services;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dark.webprog26.placessearchwidget.MainActivity;
import com.dark.webprog26.placessearchwidget.MapsActivity;
import com.dark.webprog26.placessearchwidget.events.LocationFoundEvent;
import com.dark.webprog26.placessearchwidget.helpers.PlacesLoader;
import com.dark.webprog26.placessearchwidget.models.LocationModel;
import com.dark.webprog26.placessearchwidget.widget.PlacesSearchWidget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;

import static com.dark.webprog26.placessearchwidget.widget.PlacesSearchWidget.WIDGET_UPDATE_MODE;

/**
 * Created by webpr on 03.03.2017.
 */

public class ServiceLocation extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "ServiceLocation";

    public static final String ACTION_LOCATION_REQUEST = "com.dark.webprog26.placessearchwidget.action_location_request";
    public static final String SERVICE_MODE = "com.dark.webprog26.placessearchwidget.service_mode";

    private GoogleApiClient mGoogleApiClient;
    private int serviceMode = -1;
    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private LocationRequest mLocationRequest;

    private LocationListener locationListener = new LocationListener();

    private class LocationListener implements
            com.google.android.gms.location.LocationListener {

        public LocationListener() {
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged()");
            //User request stored via SharedPreferences
            String userRequest = PreferenceManager.getDefaultSharedPreferences(ServiceLocation.this).getString(MainActivity.PREFS_LAST_SEARCH_REQUEST, null);
            if(userRequest!= null){
                //Fill LocationModel instance
                LocationModel locationModel = new LocationModel(location.getLatitude(), location.getLongitude());
                //Initializng PlacesLoader instance
                PlacesLoader placesLoader = new PlacesLoader(ServiceLocation.this);
                switch (serviceMode){
                    case MapsActivity.MAPS_ACTIVITY_MODE:
                        //Request from MapsActivity
                        EventBus.getDefault().post(new LocationFoundEvent(locationModel));
                        break;
                    case WIDGET_UPDATE_MODE:
                        //Request from widget
                            if(widgetId != AppWidgetManager.INVALID_APPWIDGET_ID){
                                placesLoader.loadPlaces(PlacesSearchWidget.WIDGET_UPDATE_MODE, locationModel, userRequest, widgetId);
                            }
                        break;
                    default:
                        break;
                }
            }

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");
        if(intent != null){
            if(intent.getAction().equalsIgnoreCase(ACTION_LOCATION_REQUEST)){
                serviceMode = intent.getIntExtra(SERVICE_MODE, -1);
                widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            }

            if (!mGoogleApiClient.isConnected()){
                mGoogleApiClient.connect();
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        if(mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        if(mGoogleApiClient.isConnected())
        {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, locationListener);

        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        //

    }

    @Override
    public void onConnected(Bundle arg0) {
        //
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(35000);
        mLocationRequest.setFastestInterval(30000);
        startLocationUpates();
    }
    private void startLocationUpates() {
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, locationListener);
        } catch (SecurityException se){
            se.printStackTrace();
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }
}
