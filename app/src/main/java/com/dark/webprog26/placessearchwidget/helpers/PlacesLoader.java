package com.dark.webprog26.placessearchwidget.helpers;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.dark.webprog26.placessearchwidget.MapsActivity;
import com.dark.webprog26.placessearchwidget.R;
import com.dark.webprog26.placessearchwidget.events.PlacesListReadyEvent;
import com.dark.webprog26.placessearchwidget.models.LocationModel;
import com.dark.webprog26.placessearchwidget.models.PlaceModel;
import com.dark.webprog26.placessearchwidget.models.PlacesResponseModel;
import com.dark.webprog26.placessearchwidget.retrofit.ApiClient;
import com.dark.webprog26.placessearchwidget.retrofit.ApiInterface;
import com.dark.webprog26.placessearchwidget.services.ServiceLocation;
import com.dark.webprog26.placessearchwidget.widget.PlacesSearchWidget;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by webprog26 on 01.03.2017.
 */

public class PlacesLoader {

    private static final String TAG = "PlacesLoader";

    private static final String API_KEY = "AIzaSyChjhvT_en1QoGu5aICiDU8WEPmrqS7CeI";//Google Places Web API key

    //Action attribute to refresh widget's data if entry point is not the widget, but MainActivity so widget's id is not known
    public static final String ACTION_NEW_REQUEST_PROCESSED = "com.dark.webprog26.placessearchwidget.action_new_request_processed";

    private ApiInterface mApiInterface;
    private final WeakReference<Context> mContextWeakReference;
    private SharedPreferences mSharedPreferences;

    public PlacesLoader(Context context) {
        this.mApiInterface = ApiClient.getClient().create(ApiInterface.class);
        this.mContextWeakReference = new WeakReference<Context>(context);
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void loadPlaces(final int mCurrentMode, final LocationModel locationModel, String userRequest, final int widgetId){
        final Context context = mContextWeakReference.get();

        Log.i(TAG, "loadPlaces");
        //Making call to Google Places API via retrofit library methods
        final Call<PlacesResponseModel> placesResponseCall = mApiInterface.getPlaces(userRequest,
                makeLocationString(locationModel), API_KEY);

        placesResponseCall.enqueue(new Callback<PlacesResponseModel>() {
            @Override
            public void onResponse(Call<PlacesResponseModel> call, Response<PlacesResponseModel> response) {
                ArrayList<PlaceModel> placeModels = new ArrayList<PlaceModel>();
                for(PlaceModel placeModel: response.body().getPlaceResults()){
                    placeModels.add(placeModel);
                }
                //saving number of places found in SharedPreferences
                mSharedPreferences.edit().putInt(MapsActivity.PREFS_LAST_SEARCH_RESULTS_COUNT, placeModels.size()).apply();

                //Checking entry point
                switch (mCurrentMode){
                        case MapsActivity.MAPS_ACTIVITY_MODE:
                            EventBus.getDefault().post(new PlacesListReadyEvent(placeModels));
                            if(widgetId != AppWidgetManager.INVALID_APPWIDGET_ID){
                                //Entry point is widget, refresh it's data by widget's id
                                PlacesSearchWidget.setWidgetLastSearchResults(context, AppWidgetManager.getInstance(context), widgetId);
                            } else {
                                Log.i(TAG, "in MapActivity mode INVALID_APPWIDGET_ID");
                                //Entry point is MainActivity, widget's id is not known,
                                //but for it works like a BroadcastReceiver we could send broadcast
                                context.sendBroadcast(new Intent(ACTION_NEW_REQUEST_PROCESSED));
                            }
                            break;
                        //Widget "wants" to update it's data without starting any activity
                        case PlacesSearchWidget.WIDGET_UPDATE_MODE:
                            //Because this is explicitly call from the widget we can update it's data by id
                            PlacesSearchWidget.setWidgetLastSearchResults(context, AppWidgetManager.getInstance(context), widgetId);
                            context.stopService(new Intent(context, ServiceLocation.class));
                            break;
                    }
            }

            @Override
            public void onFailure(Call<PlacesResponseModel> call, Throwable t) {
                //Retrofit's request failed
                t.printStackTrace();
            }
        });
    }

    /**
     * Converts {@link LocationModel} to special {@link String}
     * used in request to Google Places Web API for identifying
     * current users location
     * @param locationModel {@link LocationModel}
     * @return {@link String}
     */
    private String makeLocationString(LocationModel locationModel){
        return "" + locationModel.getLat() + "," + locationModel.getLng();
    }
}
