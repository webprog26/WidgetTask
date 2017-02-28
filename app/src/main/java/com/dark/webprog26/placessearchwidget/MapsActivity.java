package com.dark.webprog26.placessearchwidget;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.dark.webprog26.placessearchwidget.events.PlacesIconsReadyEvent;
import com.dark.webprog26.placessearchwidget.events.PlacesListReadyEvent;
import com.dark.webprog26.placessearchwidget.events.SearchForThePlacesEvent;
import com.dark.webprog26.placessearchwidget.helpers.BitmapDecoder;
import com.dark.webprog26.placessearchwidget.helpers.GPSTracker;
import com.dark.webprog26.placessearchwidget.helpers.MarkerAnimator;
import com.dark.webprog26.placessearchwidget.models.LocationModel;
import com.dark.webprog26.placessearchwidget.models.PlaceModel;
import com.dark.webprog26.placessearchwidget.models.PlacesResponseModel;
import com.dark.webprog26.placessearchwidget.retrofit.ApiClient;
import com.dark.webprog26.placessearchwidget.retrofit.ApiInterface;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.dark.webprog26.placessearchwidget.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity_TAG";

    private static final float CAMERA_ZOOM_DISTANCE = 12f;

    private static final String API_KEY = "AIzaSyChjhvT_en1QoGu5aICiDU8WEPmrqS7CeI";

    public static final String USER_REQUEST = "com.dark.webprog26.placessearchwidget.user_request";
    public static final String PREFS_LAST_SEARCH_RESULTS_COUNT = "com.dark.webprog26.placessearchwidget.prefs_last_search_result_count";


    private LocationModel mLocationModel;
    private Map<PlaceModel, Bitmap> mIconsMap = new HashMap<>();
    private SharedPreferences mSharedPreferences;

    private GoogleMap mMap;
    private ApiInterface mApiInterface;
    private GPSTracker mGpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mGpsTracker = new GPSTracker(this);
        mGpsTracker = new GPSTracker(this);
        if(!mGpsTracker.canGetLocation()){
            mGpsTracker.showSettingsAlert();
        }
        mApiInterface = ApiClient.getClient().create(ApiInterface.class);
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

    @Override
    protected void onResume() {
        super.onResume();
        String userRequest = mSharedPreferences.getString(MainActivity.PREFS_LAST_SEARCH_REQUEST, null);
        if(userRequest != null){
            final LocationModel userLocationModel = new LocationModel(mGpsTracker.getLatitude(), mGpsTracker.getLongitude());
            String locationString = makeLocationString(userLocationModel);
            EventBus.getDefault().post(new SearchForThePlacesEvent(locationString, userRequest));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSearchForThePlacesEvent(SearchForThePlacesEvent searchForThePlacesEvent){
        final Call<PlacesResponseModel> placesResponseCall = mApiInterface.getPlaces(searchForThePlacesEvent.getRequest(),
                                                             searchForThePlacesEvent.getLocationString(), API_KEY);
        Log.i(TAG, placesResponseCall.request().toString());
        placesResponseCall.enqueue(new Callback<PlacesResponseModel>() {
            @Override
            public void onResponse(Call<PlacesResponseModel> call, Response<PlacesResponseModel> response) {
                ArrayList<PlaceModel> placeModels = new ArrayList<PlaceModel>();
                for(PlaceModel placeModel: response.body().getPlaceResults()){
                    placeModels.add(placeModel);
                }
                mSharedPreferences.edit().putInt(PREFS_LAST_SEARCH_RESULTS_COUNT, placeModels.size()).apply();

                if(placeModels.size() == 0){
                    Toast.makeText(MapsActivity.this, getString(R.string.no_results_found), Toast.LENGTH_SHORT).show();
                } else {
                    EventBus.getDefault().post(new PlacesListReadyEvent(placeModels));
                }
            }

            @Override
            public void onFailure(Call<PlacesResponseModel> call, Throwable t) {
                Log.i(TAG, t.toString());
            }
        });
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

        mLocationModel = new LocationModel(mGpsTracker.getLatitude(), mGpsTracker.getLongitude());
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
            MarkerAnimator.animateMarker(mMap, marker, userSearchLocation);
        }
    }

    private String makeLocationString(LocationModel locationModel){
        return "" + locationModel.getLat() + "," + locationModel.getLng();
    }
}
