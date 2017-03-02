package com.dark.webprog26.placessearchwidget.helpers;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.dark.webprog26.placessearchwidget.R;

import java.lang.ref.WeakReference;

/**
 * Created by webpr on 28.02.2017.
 */

public class GPSTracker implements LocationListener {

    private final WeakReference<Context> mContextWeakReference;

    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private Location mLocation;
    double lat;
    double lng;

    private static final long MIN_UPDATE_DISTANCE = 10;//10 meters
    private static final long MIN_UPDATE_TIME = 1000 * 60 * 10;//10 minutes

    protected LocationManager mLocationManager;

    public GPSTracker(Context context) {
        this.mContextWeakReference = new WeakReference<Context>(context);
        getLocation();
    }

    /**
     * Gets user last known location
     * @return Location
     */
    public Location getLocation(){
        try{
            Context context = mContextWeakReference.get();
            //Initializing LocationManager
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            //Initializing location providers
            isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(!isGPSEnabled && !isNetworkEnabled){
                //Both location providers are disabled. Show user message
                Toast.makeText(context, context.getString(R.string.location_providers_are_not_enabled), Toast.LENGTH_SHORT).show();
            } else {
                canGetLocation = true;
                if(isNetworkEnabled){
                    if(ContextCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, this);

                        if(mLocationManager != null){
                            mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                if(mLocation != null){
                                    lat = mLocation.getLatitude();
                                    lng = mLocation.getLongitude();
                                }
                            }
                        }
                    }
                }

                if(isGPSEnabled){
                    if(mLocation != null){
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, this);

                        if(mLocationManager != null){
                            mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            if(mLocation != null){
                                lat = mLocation.getLatitude();
                                lng = mLocation.getLongitude();
                            }
                        }
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
        }
        return mLocation;
        }


    /**
     * Stops unnecessary gps-tracking
     */
    public void stopTrackingWithGPS(){
        if(mLocationManager != null){
            try {
                mLocationManager.removeUpdates(this);
            } catch (SecurityException se){
                se.printStackTrace();
            }
        }
    }

    /**
     * Gets current user location latitude
     * @return double
     */
    public double getLatitude(){
        if(mLocation != null){
            lat = mLocation.getLatitude();
        }
        return lat;
    }

    /**
     * Gets current user location longitude
     * @return double
     */
    public double getLongitude(){
        if(mLocation != null){
            lng = mLocation.getLongitude();
        }
        return lng;
    }

    /**
     * Checks are any location providers enabled
     * @return boolean
     */
    public boolean canGetLocation() {
        return canGetLocation;
    }

    /**
     * Shows AlertDialog in case GPS is disabled
     */
    public void showSettingsAlert() {
        final Context context = mContextWeakReference.get();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle(context.getString(R.string.gps_settings));

        // Setting Dialog Message
        alertDialog
                .setMessage(context.getString(R.string.gps_is_not_enabled));

        // On pressing Settings button
        alertDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }
                });

        // on pressing cancel button
        alertDialog.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onProviderDisabled(String provider) {
        isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!isGPSEnabled && !isNetworkEnabled){
            stopTrackingWithGPS();
        }
    }
}
