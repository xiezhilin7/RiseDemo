package com.example.risedemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;

import com.example.risedemo.service.MultiProcessPluginService;
import com.example.risedemo.util.DaemonUtil;
import com.facebook.stetho.Stetho;

public class RiseApplication extends Application {
    private static final String TAG = "Rise-RiseApplication";
    private static RiseApplication application;
    private Intent serviceMultiProcessIntent;

    @SuppressLint("LongLogTag")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: RiseApplication:" + DaemonUtil.getCurProcessName(this));
        MultiDex.install(this);
        application = this;
        enableStetho();
        registerActivityLifeCycle(this);
        startMultiProcessPluginService();

    }

    //ava.lang.RuntimeException: Unable to create application com.example.risedemo.RiseApplication:
    // java.lang.IllegalStateException: Not allowed to start service Intent { cmp=com.example.risedemo/.service.MultiProcessPluginService }:
    // app is in background uid UidRecord{3cbcdb1 u0a148 LAST idle procs:1 seq(0,0,0)}
    private void startMultiProcessPluginService() {
        serviceMultiProcessIntent = new Intent(application, MultiProcessPluginService.class);
        application.startService(serviceMultiProcessIntent);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //此处getApplicationContext会报空指针
        try {
            registerActivityLifeCycle(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enableStetho() {
        Stetho.InitializerBuilder initializerBuilder = Stetho.newInitializerBuilder(this);
        // Enable Chrome DevTools
        initializerBuilder.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this));
        // Enable command line interface
        initializerBuilder.enableDumpapp(Stetho.defaultDumperPluginsProvider(application));
        // Use the InitializerBuilder to generate an Initializer
        Stetho.Initializer initializer = initializerBuilder.build();
        // Initialize Stetho with the Initializer
        Stetho.initialize(initializer);

    }

    private void registerActivityLifeCycle(Context context) {
        if (context == null) {
            return;
        }
        boolean isMainProcess = DaemonUtil.isMainProcess(context);
        String curProcessName = DaemonUtil.getCurProcessName(context);
        Log.e(TAG, "registerActivityLifeCycle isMainProcess: " + isMainProcess + ",curProcessName:" + curProcessName);

        Application application;
        if (context instanceof Application) {
            application = (Application) context;
        } else {
            application = (Application) context.getApplicationContext();
        }
        if (application != null) {
            application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                    Log.e(TAG, "onActivityCreated: " + activity.toString());
                }

                @Override
                public void onActivityStarted(@NonNull Activity activity) {

                }

                @Override
                public void onActivityResumed(@NonNull Activity activity) {

                }

                @Override
                public void onActivityPaused(@NonNull Activity activity) {

                }

                @Override
                public void onActivityStopped(@NonNull Activity activity) {

                }

                @Override
                public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

                }

                @Override
                public void onActivityDestroyed(@NonNull Activity activity) {
                    Log.e(TAG, "onActivityDestroyed: " + activity.toString());
                    if (serviceMultiProcessIntent != null && activity instanceof MainActivity) {
                        application.stopService(serviceMultiProcessIntent);
                    }
                }
            });
        }
    }


}