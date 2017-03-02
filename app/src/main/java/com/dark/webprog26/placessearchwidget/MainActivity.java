package com.dark.webprog26.placessearchwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.dark.webprog26.placessearchwidget.helpers.ConnectionDetector;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity_TAG";


    public static final String PREFS_LAST_SEARCH_REQUEST = "com.dark.webprog26.placessearchwidget.prefs_last_search_request";

    public static final String NEW_REQUEST = "com.dark.webprog26.placessearchwidget.new_request";
    public static final int NEW_REQUEST_MODE = 102;


    private SharedPreferences mSharedPreferences;

    @BindView(R.id.etRequest)
    EditText mEtRequest;
    @BindView(R.id.fbSearchByRequest)
    FloatingActionButton mFbSearchByRequest;

    private ConnectionDetector mConnectionDetector;
    private int mWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mConnectionDetector = new ConnectionDetector(this);

        //Entry point
        mFbSearchByRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Internet connection is necessary, so gonna check for it
                if(mConnectionDetector.isConnectedToInternet()){
                    //Got connected. Check for request is not null and it's length > 0
                    String mRequestString = mEtRequest.getText().toString();
                    if(mRequestString.length() > 0){
                        //Save user request via SharedPreferences
                        mSharedPreferences.edit().putString(PREFS_LAST_SEARCH_REQUEST, mRequestString).apply();
                        //Make intent to start MapActivity
                        Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
                        //Let MapsActivity know that we want it to start in new request mode, from MainActivity, not from the widget
                        mapIntent.putExtra(NEW_REQUEST, NEW_REQUEST_MODE);

                        //If MainActivity was started from widget, we should send widget's id to MapsActivity
                        Bundle extras = getIntent().getExtras();
                        if(extras != null){
                            mWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                        }

                        if(mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID){
                            mapIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
                        }
                        //start MapsActivity
                        startActivity(mapIntent);
                    }
                } else {
                    //Internet connection is missing! Show the message
                    Toast.makeText(MainActivity.this, getString(R.string.internet_connection_is_missing), Toast.LENGTH_SHORT).show();
                }
               //Hide soft keyboard anyway
               hideKeyboard();
            }
        });
    }

    /**
     * Hides soft keyboard
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
