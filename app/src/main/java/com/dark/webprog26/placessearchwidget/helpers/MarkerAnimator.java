package com.dark.webprog26.placessearchwidget.helpers;

import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by webpr on 28.02.2017.
 */

public class MarkerAnimator {

    public static void animateMarker(final GoogleMap map, final Marker marker, final LatLng targetLocation){
        final long duration = 400;
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();

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
