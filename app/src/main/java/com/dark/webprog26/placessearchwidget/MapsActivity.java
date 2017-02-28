package com.dark.webprog26.placessearchwidget;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;
import com.dark.webprog26.placessearchwidget.events.PlacesIconsReadyEvent;
import com.dark.webprog26.placessearchwidget.events.PlacesListReadyEvent;
import com.dark.webprog26.placessearchwidget.helpers.BitmapDecoder;
import com.dark.webprog26.placessearchwidget.models.LocationModel;
import com.dark.webprog26.placessearchwidget.models.PlaceModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
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

import static com.dark.webprog26.placessearchwidget.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity_TAG";

    private static final float CAMERA_ZOOM_DISTANCE = 12f;

    public static final String USER_CURRENT_LOCATION = "com.dark.webprog26.placessearchwidget.user_current_location";
    public static final String USER_SEARCH_PLACES_LOCATIONS_LIST = "com.dark.webprog26.placessearchwidget.user_search_places_locations_list";

    private LocationModel mLocationModel;
    private Map<PlaceModel, Bitmap> mIconsMap = new HashMap<>();

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
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
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        try{
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException se){
            se.printStackTrace();
        }

        ArrayList<PlaceModel> placeModels = (ArrayList<PlaceModel>) getIntent()
                                                .getSerializableExtra(USER_SEARCH_PLACES_LOCATIONS_LIST);

        if(placeModels != null){
            if(placeModels.size() == 0){
                Toast.makeText(MapsActivity.this, getString(R.string.no_results_found), Toast.LENGTH_SHORT).show();
            } else {
                EventBus.getDefault().post(new PlacesListReadyEvent(placeModels));
            }
        }

        mLocationModel = (LocationModel) getIntent().getSerializableExtra(USER_CURRENT_LOCATION);
        if(mLocationModel != null){
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

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPlacesListReadyEvent(PlacesListReadyEvent placesListReadyEvent){
        ArrayList<PlaceModel> placeModels = placesListReadyEvent.getPlaceModels();

            for(PlaceModel placeModel: placeModels){
                mIconsMap.put(placeModel, BitmapDecoder.getBitmapFromURL(placeModel.getIcon()));
                Log.i(TAG, placeModel.getIcon());
        }
        EventBus.getDefault().post(new PlacesIconsReadyEvent(mIconsMap));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlacesIconsReadyEvent(PlacesIconsReadyEvent placesIconsReadyEvent){
        for(PlaceModel placeModel: mIconsMap.keySet()){
            LocationModel locationModel = placeModel.getGeometry().getLocation();
            final LatLng userSearchLocation = new LatLng(locationModel.getLat(), locationModel.getLng());
            Bitmap iconBitmap = mIconsMap.get(placeModel);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(userSearchLocation).title(placeModel.getName()).visible(false);

            if (iconBitmap != null) {
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconBitmap));
            }

            final Marker marker = mMap.addMarker(markerOptions);
            animateMarker(marker, userSearchLocation);
        }
    }

    private void animateMarker(final Marker marker, final LatLng targetLocation){
        final long duration = 400;
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();

        Point startPoint = proj.toScreenLocation(targetLocation);
        startPoint.y = 0;
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);

        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * targetLocation.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * targetLocation.latitude + (1 - t) * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    // Post again 10ms later.
                    handler.postDelayed(this, 10);
                } else {
                    // animation ended
                }
                if(!marker.isVisible()){
                    marker.setVisible(true);
                }
            }
        });
    }
}
