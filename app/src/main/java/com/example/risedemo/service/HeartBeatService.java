package com.example.risedemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.risedemo.HeartBeatAidl;
import com.example.risedemo.IReceiverAidlInterface;
import com.example.risedemo.util.DaemonUtil;

import java.util.Timer;
import java.util.TimerTask;

public class HeartBeatService extends Service {
    private static final String TAG = "Rise-HeartBeatService";

    private Timer timer = new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            onHeartBeat();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        boolean isMainProcess = DaemonUtil.isMainProcess(this);
        String curProcessName = DaemonUtil.getCurProcessName(this);
        Log.e(TAG, "onCreate isMainProcess: " + isMainProcess + ",curProcessName:" + curProcessName);
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
        return (IBinder) mHeartBeatAidl;
    }

    private IReceiverAidlInterface mIReceiverAidlInterface;
    private final HeartBeatAidl mHeartBeatAidl = new HeartBeatAidl.Stub() {
        @Override
        public String getValueInMainProcess() throws RemoteException {
            return String.valueOf(value);
        }

        @Override
        public void notifySuccess(String data) throws RemoteException {
            Log.d(TAG, "in HeartBeatService notifySuccess(), data:" + data);
        }

        @Override
        public void registerCallback(IReceiverAidlInterface cb) throws RemoteException {
            mIReceiverAidlInterface = cb;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy()");
        try {
            timer.cancel();
            timer.purge();
            timerTask.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getDelayExecutedMillis() {
        return 0;
    }

    public long getHeartBeatMillis() {
        return 10 * 1000;
    }

    private int value = 1;

    public void onHeartBeat() {
        ++value;
        Log.d(TAG, "onHeartBeat() " + value);
        if (mIReceiverAidlInterface != null) {
            try {
                mIReceiverAidlInterface.receiveMessage(value);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
