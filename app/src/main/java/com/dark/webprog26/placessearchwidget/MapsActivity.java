package com.dark.webprog26.placessearchwidget;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.dark.webprog26.placessearchwidget.models.LocationModel;
import com.dark.webprog26.placessearchwidget.models.PlaceModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity_TAG";
    public static final String USER_CURRENT_LOCATION = "com.dark.webprog26.placessearchwidget.user_current_location";
    public static final String USER_SEARCH_PLACES_LOCATIONS_LIST = "com.dark.webprog26.placessearchwidget.user_search_places_locations_list";

    private LocationModel mLocationModel;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        mLocationModel = (LocationModel) getIntent().getSerializableExtra(USER_CURRENT_LOCATION);
        if(mLocationModel != null){
            LatLng userLocation = new LatLng(mLocationModel.getLat(), mLocationModel.getLng());
            mMap.addMarker(new MarkerOptions().position(userLocation).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
        }


        ArrayList<PlaceModel> placeModels = (ArrayList<PlaceModel>) getIntent().getSerializableExtra(USER_SEARCH_PLACES_LOCATIONS_LIST);

        if(placeModels != null){
            for(PlaceModel placeModel: placeModels){
                LocationModel locationModel = placeModel.getGeometry().getLocation();
                LatLng userSearchLocation = new LatLng(locationModel.getLat(), locationModel.getLng());
                mMap.addMarker(new MarkerOptions().position(userSearchLocation).title(placeModel.getName()));
            }
        }

    }
}
