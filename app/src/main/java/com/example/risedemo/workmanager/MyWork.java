package com.example.risedemo.workmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.risedemo.receiver.AppInstallMonitorObserver;

public class MyWork extends Worker {

    private static final String TAB = "Rise-MyWork";

    public MyWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        Log.e(TAB,"Working in BackGround=================");
        AppInstallMonitorObserver.getInstance(getApplicationContext()).addObserver(new AppInstallMonitorObserver.CallBack() {
            @Override
            public void update(String packageName) {
                Log.e(TAB, "doWork when apk installed:" + packageName);
            }
        });

        return Result.success();
    }
}
