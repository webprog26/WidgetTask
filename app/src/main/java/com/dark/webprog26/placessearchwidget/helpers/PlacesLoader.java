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

    private static final String API_KEY = "AIzaSyChjhvT_en1QoGu5aICiDU8WEPmrqS7CeI";
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
        Log.i(TAG, "loadPlaces");
        final Call<PlacesResponseModel> placesResponseCall = mApiInterface.getPlaces(userRequest,
                makeLocationString(locationModel), API_KEY);
        final Context context = mContextWeakReference.get();

        placesResponseCall.enqueue(new Callback<PlacesResponseModel>() {
            @Override
            public void onResponse(Call<PlacesResponseModel> call, Response<PlacesResponseModel> response) {
                ArrayList<PlaceModel> placeModels = new ArrayList<PlaceModel>();
                for(PlaceModel placeModel: response.body().getPlaceResults()){
                    placeModels.add(placeModel);
                }
                mSharedPreferences.edit().putInt(MapsActivity.PREFS_LAST_SEARCH_RESULTS_COUNT, placeModels.size()).apply();



                if(placeModels.size() == 0){
                    Toast.makeText(context, context.getResources().getString(R.string.no_results_found), Toast.LENGTH_SHORT).show();
                } else {
                    switch (mCurrentMode){
                        case MapsActivity.MAPS_ACTIVITY_MODE:
                            EventBus.getDefault().post(new PlacesListReadyEvent(placeModels));
                            if(widgetId != AppWidgetManager.INVALID_APPWIDGET_ID){
                                PlacesSearchWidget.setWidgetLastSearchResults(context, AppWidgetManager.getInstance(context), widgetId);
                            } else {
                                Log.i(TAG, "in MapActivity mode INVALID_APPWIDGET_ID");
                                context.sendBroadcast(new Intent(ACTION_NEW_REQUEST_PROCESSED));
                            }
                            break;
                        case PlacesSearchWidget.WIDGET_UPDATE_MODE:
                            PlacesSearchWidget.setWidgetLastSearchResults(context, AppWidgetManager.getInstance(context), widgetId);
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<PlacesResponseModel> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private String makeLocationString(LocationModel locationModel){
        return "" + locationModel.getLat() + "," + locationModel.getLng();
    }
}
