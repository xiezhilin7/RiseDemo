package com.example.risedemo;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

public class RiseApplication extends Application {
    private static final String TAG = "TileService-RiseApplication";
    private static RiseApplication application;

    @SuppressLint("LongLogTag")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: RiseApplication");
        application = this;
    }

}