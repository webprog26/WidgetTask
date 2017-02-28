package com.dark.webprog26.placessearchwidget;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.dark.webprog26.placessearchwidget.helpers.ConnectionDetector;
import com.dark.webprog26.placessearchwidget.helpers.GPSTracker;
import com.dark.webprog26.placessearchwidget.models.LocationModel;
import com.dark.webprog26.placessearchwidget.models.PlaceModel;
import com.dark.webprog26.placessearchwidget.models.PlacesResponseModel;
import com.dark.webprog26.placessearchwidget.retrofit.ApiClient;
import com.dark.webprog26.placessearchwidget.retrofit.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity_TAG";

    private static final String API_KEY = "AIzaSyChjhvT_en1QoGu5aICiDU8WEPmrqS7CeI";

    @BindView(R.id.etRequest)
    EditText mEtRequest;
    @BindView(R.id.fbSearchByRequest)
    FloatingActionButton mFbSearchByRequest;
    private ApiInterface mApiInterface;
    private ConnectionDetector mConnectionDetector;
    private GPSTracker mGpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mGpsTracker = new GPSTracker(this);
        if(!mGpsTracker.canGetLocation()){
            mGpsTracker.showSettingsAlert();
        }
        mApiInterface = ApiClient.getClient().create(ApiInterface.class);
        mConnectionDetector = new ConnectionDetector(this);

        mFbSearchByRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mConnectionDetector.isConnectedToInternet()){
                    String mRequestString = mEtRequest.getText().toString();
                    if(mRequestString.length() > 0){
                        final LocationModel userLocationModel = new LocationModel(mGpsTracker.getLatitude(), mGpsTracker.getLongitude());
                        String locationString = makeLocationString(userLocationModel);
                        final Call<PlacesResponseModel> placesResponseCall = mApiInterface.getPlaces(mRequestString, locationString, API_KEY);
                        Log.i(TAG, placesResponseCall.request().toString());
                        placesResponseCall.enqueue(new Callback<PlacesResponseModel>() {
                            @Override
                            public void onResponse(Call<PlacesResponseModel> call, Response<PlacesResponseModel> response) {
                                ArrayList<PlaceModel> placeModels = new ArrayList<PlaceModel>();
                                for(PlaceModel placeModel: response.body().getPlaceResults()){
                                    Log.i(TAG, "PlaceModel name: " + placeModel.getName());
                                    Log.i(TAG, "PlaceModel coordinates: lat: " + placeModel.getGeometry().getLocation().getLat()
                                            + " lng "
                                            + placeModel.getGeometry().getLocation().getLng());
                                    placeModels.add(placeModel);
                                }
                                Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
                                mapIntent.putExtra(MapsActivity.USER_CURRENT_LOCATION, userLocationModel);
                                mapIntent.putExtra(MapsActivity.USER_SEARCH_PLACES_LOCATIONS_LIST, placeModels);
                                startActivity(mapIntent);
                            }

                            @Override
                            public void onFailure(Call<PlacesResponseModel> call, Throwable t) {
                                Log.i(TAG, t.toString());
                            }
                        });
                    }
                }
               hideKeyboard();
            }
        });
    }

    private String makeLocationString(LocationModel locationModel){
        return "" + locationModel.getLat() + "," + locationModel.getLng();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
