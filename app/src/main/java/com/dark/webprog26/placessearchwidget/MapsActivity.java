package com.dark.webprog26.placessearchwidget;


import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.dark.webprog26.placessearchwidget.events.LocationFoundEvent;
import com.dark.webprog26.placessearchwidget.events.PlacesIconsReadyEvent;
import com.dark.webprog26.placessearchwidget.events.PlacesListReadyEvent;
import com.dark.webprog26.placessearchwidget.helpers.BitmapDecoder;
import com.dark.webprog26.placessearchwidget.helpers.ConnectionDetector;
import com.dark.webprog26.placessearchwidget.helpers.MarkerAnimator;
import com.dark.webprog26.placessearchwidget.helpers.PlacesLoader;
import com.dark.webprog26.placessearchwidget.models.LocationModel;
import com.dark.webprog26.placessearchwidget.models.PlaceModel;
import com.dark.webprog26.placessearchwidget.services.ServiceLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.dark.webprog26.placessearchwidget.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity_TAG";

    private static final float CAMERA_ZOOM_DISTANCE = 12f;

    public static final int MAPS_ACTIVITY_MODE = 101;
    public static final String PREFS_LAST_SEARCH_RESULTS_COUNT = "com.dark.webprog26.placessearchwidget.prefs_last_search_result_count";

    @BindView(R.id.progressBarContainer)
    FrameLayout mProgressBarContainer;

    private LocationModel mLocationModel;
    private SharedPreferences mSharedPreferences;

    private GoogleMap mMap;
    private ConnectionDetector mConnectionDetector;
    private int widgetId;
    private String userRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        mConnectionDetector = new ConnectionDetector(this);
        if(!mConnectionDetector.isConnectedToInternet()){
            //Internet connection is missing! Show the message
            Toast.makeText(this, getString(R.string.internet_connection_is_missing), Toast.LENGTH_SHORT).show();
            //Can't process, finish & return
            finish();
            return;
        }
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        userRequest = mSharedPreferences.getString(MainActivity.PREFS_LAST_SEARCH_REQUEST, null);
        if(userRequest != null){
            if(getIntent().getIntExtra(MainActivity.NEW_REQUEST, 0) == MainActivity.NEW_REQUEST_MODE){
                widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            } else {
                //We've got a call from the widget
                widgetId = getIntent().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            }
            //Starting service to get current user location
            Intent locationIntent = new Intent(this, ServiceLocation.class);
            locationIntent.setAction(ServiceLocation.ACTION_LOCATION_REQUEST);
            locationIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            locationIntent.putExtra(ServiceLocation.SERVICE_MODE, MAPS_ACTIVITY_MODE);
            startService(locationIntent);
        } else {
            //App is newly installed or SharedPreferences were erased, but we've got a call from the widget
            Toast.makeText(this, getString(R.string.type_your_request), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationFoundEvent(LocationFoundEvent locationFoundEvent){
        mLocationModel = locationFoundEvent.getLocationModel();
        stopService(new Intent(this, ServiceLocation.class));
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
       new PlacesLoader(this).loadPlaces(MapsActivity.MAPS_ACTIVITY_MODE, mLocationModel, userRequest, widgetId);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Initializing the map
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        try{
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException se){
            se.printStackTrace();
        }

        if(mLocationModel != null){
            //Getting last known user location, marking it with app icon and move camera to it
            LatLng userLocation = new LatLng(mLocationModel.getLat(), mLocationModel.getLng());
            mMap.addMarker(new MarkerOptions().position(userLocation)
                    .title(getString(R.string.you_are_here))
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(CAMERA_ZOOM_DISTANCE));
        }
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * Receives list of places by user request and loads places icons
     * Shows message if no results found
     * @param placesListReadyEvent {@link PlacesListReadyEvent}
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPlacesListReadyEvent(PlacesListReadyEvent placesListReadyEvent){
        ArrayList<PlaceModel> placeModels = placesListReadyEvent.getPlaceModels();
        Log.i(TAG, "onPlacesListReadyEvent " + placeModels.size());
        if(placeModels.size() == 0){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MapsActivity.this, getString(R.string.no_results_found), Toast.LENGTH_SHORT).show();
                    hideProgressBar();
                }
            });
            return;
        }
            Map<PlaceModel, Bitmap> iconsMap = new HashMap<>();
            for(PlaceModel placeModel: placeModels){
                iconsMap.put(placeModel, BitmapDecoder.getBitmapFromURL(placeModel.getIcon()));
                Log.i(TAG, placeModel.getIcon());
        }
        EventBus.getDefault().post(new PlacesIconsReadyEvent(iconsMap));
    }

    /**
     * Draws markers with already loaded and stored in mIconsMap icons, animates them
     * @param placesIconsReadyEvent {@link PlacesIconsReadyEvent}
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlacesIconsReadyEvent(PlacesIconsReadyEvent placesIconsReadyEvent){
        Map<PlaceModel, Bitmap> iconsMap = placesIconsReadyEvent.getPlaceModelsWithIcons();
        for(PlaceModel placeModel: iconsMap.keySet()){
            LocationModel locationModel = placeModel.getGeometry().getLocation();
            final LatLng userSearchLocation = new LatLng(locationModel.getLat(), locationModel.getLng());
            Bitmap iconBitmap = iconsMap.get(placeModel);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(userSearchLocation).title(placeModel.getName()).visible(false);

            if (iconBitmap != null) {
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconBitmap));
            }

            final Marker marker = mMap.addMarker(markerOptions);
            MarkerAnimator.animateMarker(mMap, marker, userSearchLocation);
        }
        hideProgressBar();
    }

    /**
     * Hides progress bar
     */
    private void hideProgressBar(){
        if(mProgressBarContainer.getVisibility() == View.VISIBLE){
            mProgressBarContainer.setVisibility(View.GONE);
        }
    }
}
