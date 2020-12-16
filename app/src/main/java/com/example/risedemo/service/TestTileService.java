package com.example.risedemo.service;

import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.service.quicksettings.TileService;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.risedemo.receiver.AppInstallMonitorObserver;

/**
 * https://developer.android.google.cn/reference/android/service/quicksettings/TileService
 * <p>
 * <p>
 * If your device is rooted and under SU admin, you should not need to go through this step. If for some reason there is an error, do go through this step.
 * <p>
 * Using ADB, you must grant following permissions to the app:
 * <p>
 * adb shell pm grant com.example.risedemo android.permission.WRITE_SECURE_SETTINGS
 * adb shell pm grant com.example.risedemo android.permission.SET_ALWAYS_FINISH
 * These permissions lets this app to access developer options without being system applications.
 * <p>
 * You can now add quick setting tile by pulling down the quick setting menu and add developer option tiles.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class TestTileService extends TileService {
    private static final String TAG = "Rise-TestTileService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind intent:" + intent.toString());
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind intent:" + intent.toString());
        return super.onUnbind(intent);
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Log.e(TAG, "onTileAdded");
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        Log.e(TAG, "onTileRemoved");
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Log.e(TAG, "onStartListening");
        AppInstallMonitorObserver.getInstance(getApplicationContext()).addObserver(new AppInstallMonitorObserver.CallBack() {
            @Override
            public void update(String packageName) {
                Log.e(TAG, "onStartListening when apk installed:" + packageName);
            }
        });
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        Log.e(TAG, "onStopListening");
    }

    @Override
    public void onClick() {
        super.onClick();
        Log.e(TAG, "onClick");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }
}
