package com.dark.webprog26.placessearchwidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.dark.webprog26.placessearchwidget.MainActivity;
import com.dark.webprog26.placessearchwidget.MapsActivity;
import com.dark.webprog26.placessearchwidget.R;
import com.dark.webprog26.placessearchwidget.helpers.ConnectionDetector;
import com.dark.webprog26.placessearchwidget.helpers.GPSTracker;
import com.dark.webprog26.placessearchwidget.helpers.PlacesLoader;
import com.dark.webprog26.placessearchwidget.models.LocationModel;

/**
 * Created by webpr on 28.02.2017.
 */

public class PlacesSearchWidget extends AppWidgetProvider {

    private static final String TAG = "PSWidget";
    private static final String ACTION_UPDATE_PLACES_COUNT = "action_update_places_count";
    private static final String ACTION_SHOW_PLACES_ON_THE_MAP = "action_show_places_on_the_map";
    public static final int WIDGET_UPDATE_MODE = 103;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for(int id: appWidgetIds){
            setWidgetLastSearchResults(context, appWidgetManager, id);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(intent.getAction().equalsIgnoreCase(ACTION_UPDATE_PLACES_COUNT)){
            int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
            Bundle extras = intent.getExtras();
            if(extras != null){
                widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            }

            if(widgetId != AppWidgetManager.INVALID_APPWIDGET_ID){
                String userRequest = PreferenceManager.getDefaultSharedPreferences(context).getString(MainActivity.PREFS_LAST_SEARCH_REQUEST, null);
                if(userRequest != null){
                    ConnectionDetector connectionDetector = new ConnectionDetector(context);
                    if(!connectionDetector.isConnectedToInternet()){
                        //Internet connection is missing! Show the message
                        Toast.makeText(context, context.getString(R.string.internet_connection_is_missing), Toast.LENGTH_SHORT).show();
                        //Can't process, return
                        return;
                    }
                    GPSTracker gpsTracker = new GPSTracker(context);
                    final LocationModel locationModel = new LocationModel(gpsTracker.getLatitude(), gpsTracker.getLongitude());
                    new PlacesLoader(context).loadPlaces(WIDGET_UPDATE_MODE, locationModel, userRequest, widgetId);
                }
            }
        }

        if(intent.getAction().equalsIgnoreCase(PlacesLoader.ACTION_NEW_REQUEST_PROCESSED)){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            RemoteViews widgetView = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            String searchString = context.getString(R.string.search);;
            String lastSearchRequest = sharedPreferences.getString(MainActivity.PREFS_LAST_SEARCH_REQUEST, null);
            if(lastSearchRequest != null){
                searchString = searchString + " " + context.getString(R.string.results_found, lastSearchRequest, sharedPreferences.getInt(MapsActivity.PREFS_LAST_SEARCH_RESULTS_COUNT, 0));
            }
            widgetView.setTextViewText(R.id.tvSearchResults, searchString);
            ComponentName thisWidget = new ComponentName(context, PlacesSearchWidget.class);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.updateAppWidget(thisWidget, widgetView);
        }

        if(intent.getAction().equalsIgnoreCase(ACTION_SHOW_PLACES_ON_THE_MAP)){
            int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
            Bundle extras = intent.getExtras();
            if(extras != null){
                widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            }
            ConnectionDetector connectionDetector = new ConnectionDetector(context);
            if(!connectionDetector.isConnectedToInternet()){
                //Internet connection is missing! Show the message
                Toast.makeText(context, context.getString(R.string.internet_connection_is_missing), Toast.LENGTH_SHORT).show();
            } else {
                //Internet connection is available! Start MapsActivity
                Intent mapsIntent = new Intent(context.getApplicationContext(), MapsActivity.class);
                mapsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                mapsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mapsIntent);
            }
        }

    }

    /**
     *
     * @param context {@link Context}
     * @param appWidgetManager {@link AppWidgetProvider}
     * @param widgetId int
     */
    public static void setWidgetLastSearchResults(Context context, AppWidgetManager appWidgetManager, int widgetId){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        //Initializing widget Views
        RemoteViews widgetView = new RemoteViews(context.getPackageName(),
                R.layout.widget_layout);
        String searchString = context.getString(R.string.search);;
        String lastSearchRequest = sharedPreferences.getString(MainActivity.PREFS_LAST_SEARCH_REQUEST, null);
        //If we have last request and it's results count saved via SharedPreferences, show it with widget's TextView
        if(lastSearchRequest != null){
            searchString = searchString + " " + context.getString(R.string.results_found, lastSearchRequest, sharedPreferences.getInt(MapsActivity.PREFS_LAST_SEARCH_RESULTS_COUNT, 0));
        }
        widgetView.setTextViewText(R.id.tvSearchResults, searchString);

        //Calling MainActivity to enter new request
        Intent makeNewRequestIntent = new Intent(context, MainActivity.class);
        makeNewRequestIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        PendingIntent requestPendingIntent = PendingIntent.getActivity(context, widgetId, makeNewRequestIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        widgetView.setOnClickPendingIntent(R.id.widgetMainField, requestPendingIntent);

        //Calling to show found places on the map
        Intent getPlacesOnTheMapIntent = new Intent(context, PlacesSearchWidget.class);
        getPlacesOnTheMapIntent.setAction(ACTION_SHOW_PLACES_ON_THE_MAP);
        getPlacesOnTheMapIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        PendingIntent getPlacesOnTheMapPendingIntent = PendingIntent.getBroadcast(context, widgetId, getPlacesOnTheMapIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        widgetView.setOnClickPendingIntent(R.id.ibShowOnMap, getPlacesOnTheMapPendingIntent);


        //Updating widget with new places count without opening any activities
        Intent updatePlacesCountIntent = new Intent(context, PlacesSearchWidget.class);
        updatePlacesCountIntent.setAction(ACTION_UPDATE_PLACES_COUNT);
        updatePlacesCountIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        updatePlacesCountIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});
        PendingIntent updatePlacesCountInWidgetPendingIntent = PendingIntent.getBroadcast(context, widgetId, updatePlacesCountIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        widgetView.setOnClickPendingIntent(R.id.ibRefresh, updatePlacesCountInWidgetPendingIntent);
        appWidgetManager.updateAppWidget(widgetId, widgetView);
    }
}
