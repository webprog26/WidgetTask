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

    private static final long MIN_UPDATE_DISTANCE = 100;//100 meters
    private static final long MIN_UPDATE_TIME = 1000 * 60 * 10;//10 minutes

    protected LocationManager mLocationManager;

    public GPSTracker(Context context) {
        this.mContextWeakReference = new WeakReference<Context>(context);
        getLocation();
    }

    public Location getLocation(){
        try{
            Context context = mContextWeakReference.get();
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(!isGPSEnabled && !isNetworkEnabled){
                Toast.makeText(context, "Can't define your current location", Toast.LENGTH_SHORT).show();
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


    public void stopTrackingWithGPS(){
        if(mLocationManager != null){
            try {
                mLocationManager.removeUpdates(this);
            } catch (SecurityException se){
                se.printStackTrace();
            }
        }
    }

    public double getLatitude(){
        if(mLocation != null){
            lat = mLocation.getLatitude();
        }
        return lat;
    }

    public double getLongitude(){
        if(mLocation != null){
            lng = mLocation.getLongitude();
        }
        return lng;
    }

    public boolean canGetLocation() {
        return canGetLocation;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContextWeakReference.get());

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog
                .setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContextWeakReference.get().startActivity(intent);
                    }
                });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",
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

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
