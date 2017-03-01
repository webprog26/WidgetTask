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

        mFbSearchByRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mConnectionDetector.isConnectedToInternet()){
                    String mRequestString = mEtRequest.getText().toString();
                    if(mRequestString.length() > 0){
                        mSharedPreferences.edit().putString(PREFS_LAST_SEARCH_REQUEST, mRequestString).apply();
                        Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
                        mapIntent.putExtra(NEW_REQUEST, NEW_REQUEST_MODE);

                        Bundle extras = getIntent().getExtras();
                        if(extras != null){
                            mWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                        }

                        if(mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID){
                            mapIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
                        }
                        startActivity(mapIntent);
                    }
                }
               hideKeyboard();
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
