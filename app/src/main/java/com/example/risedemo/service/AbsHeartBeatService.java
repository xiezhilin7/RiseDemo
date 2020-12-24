package com.example.risedemo.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.risedemo.DaemonAidl;
import com.example.risedemo.util.DaemonUtil;
import java.util.Timer;
import java.util.TimerTask;

public abstract class AbsHeartBeatService extends Service {
    private static final String TAG = "Rise HeartBeatService";

    private Timer timer = new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            onHeartBeat();
        }
    };

    private final DaemonAidl aidl = new DaemonAidl.Stub() {
        @Override
        public void startService() throws RemoteException {
            Log.d(TAG, "aidl startService()");
        }

        @Override
        public void stopService() throws RemoteException {
            Log.e(TAG, "aidl stopService()");
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected() 已绑定");
            try {
                service.linkToDeath(() -> {
                    Log.e(TAG, "onServiceConnected() linkToDeath");
                    try {
                        aidl.startService();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }, 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected() 已解绑");
            try {
                aidl.stopService();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onBindingDied(ComponentName name) {
            onServiceDisconnected(name);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        boolean isMainProcess = DaemonUtil.isMainProcess(this);
        String curProcessName = DaemonUtil.getCurProcessName(this);
        Log.e(TAG, "onCreate isMainProcess: " + isMainProcess + ",curProcessName:" + curProcessName);

        onStartService();
        if (getHeartBeatMillis() > 0) {
            timer.schedule(timerTask, getDelayExecutedMillis(), getHeartBeatMillis());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return (IBinder) aidl;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy()");
        onStopService();

        unbindService(serviceConnection);

        try {
            timer.cancel();
            timer.purge();
            timerTask.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void onStartService();

    public abstract void onStopService();

    public abstract long getDelayExecutedMillis();

    public abstract long getHeartBeatMillis();

    public abstract void onHeartBeat();
}
