package com.example.risedemo.service;

import android.os.Looper;
import android.util.Log;

public class HeartBeatService extends AbsHeartBeatService {
    private static final String TAG = "Rise HeartBeatService";
    private static final android.os.Handler mainThreadHandler = new android.os.Handler(Looper.getMainLooper());

    @Override
    public void onStartService() {
        Log.d(TAG, "onStartService()");
    }

    @Override
    public void onStopService() {
        Log.e(TAG, "onStopService()");
    }

    @Override
    public long getDelayExecutedMillis() {
        return 0;
    }

    @Override
    public long getHeartBeatMillis() {
        return 10 * 1000;
    }

    @Override
    public void onHeartBeat() {
        Log.d(TAG, "onHeartBeat()");
    }
}
