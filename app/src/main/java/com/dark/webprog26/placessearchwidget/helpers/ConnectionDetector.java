package com.dark.webprog26.placessearchwidget.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.lang.ref.WeakReference;

/**
 * Created by webpr on 28.02.2017.
 */

public class ConnectionDetector {

    private final WeakReference<Context> mContextWeakReference;

    public ConnectionDetector(Context context) {
        this.mContextWeakReference = new WeakReference<Context>(context);
    }

    @SuppressWarnings("deprecation")
    public boolean isConnectedToInternet(){
        ConnectivityManager connectivityManager = (ConnectivityManager) mContextWeakReference.get()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null){
            NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
            if(networkInfos != null){
                for(NetworkInfo networkInfo: networkInfos){
                    if(networkInfo.getState() == NetworkInfo.State.CONNECTED){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
