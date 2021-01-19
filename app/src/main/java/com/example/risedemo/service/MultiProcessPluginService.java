package com.example.risedemo.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.risedemo.HeartBeatAidl;
import com.example.risedemo.IReceiverAidlInterface;
import com.example.risedemo.MultiProcessPluginServiceAidl;
import com.example.risedemo.receiver.AppInstallMonitorObserver;

/**
 * 子进程服务
 */
@SuppressLint("LongLogTag")
public class MultiProcessPluginService extends Service {
    private static final String TAG = "Rise-MultiProcessPluginService";
    private HeartBeatAidl mHeartBeatAidl;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        AppInstallMonitorObserver.getInstance(getApplicationContext()).addObserver(new AppInstallMonitorObserver.CallBack() {
            @Override
            public void update(String packageName) {
                Log.e(TAG, "onCreate when apk installed:" + packageName);
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return (IBinder) mDaemonAidIBinder;
    }

    private String mTestValue = "init value in MultiProcessPluginService";

    private final MultiProcessPluginServiceAidl mDaemonAidIBinder = new MultiProcessPluginServiceAidl.Stub() {
        @Override
        public String getValueInMultiProcess() throws RemoteException {
            Log.d(TAG, "aidl startService()");
            return mTestValue;
        }

        @Override
        public void transmitValueInMainProcess(String data) throws RemoteException {
            Log.e(TAG, "this in MultiProcessPluginService, transmitValueInMainProcess():" + data);
        }

        @Override
        public void notifyMultiProcessCanGetMainProcessValue() throws RemoteException {
            bindHeartBeatService();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy()");
        unbindHeartBeatService();
    }


    /**
     * 绑定主进程的service，获取主进程的值
     */
    private void bindHeartBeatService() {
        Log.e(TAG, "bindHeartBeatService()");
        bindService(new Intent(this, HeartBeatService.class), mHeartBeatServiceConnection, BIND_AUTO_CREATE);
    }

    private void unbindHeartBeatService() {
        Log.e(TAG, "unbindHeartBeatService()");
        try {
            if (mHeartBeatServiceConnection != null) {
                unbindService(mHeartBeatServiceConnection);
            }
        } catch (Exception e) {

        }
    }

    private ServiceConnection mHeartBeatServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mHeartBeatAidl = HeartBeatAidl.Stub.asInterface(service);
            try {
                mHeartBeatAidl.registerCallback(iReceiverAidlInterface);

                String value = mHeartBeatAidl.getValueInMainProcess();
                Log.e(TAG, "onServiceConnected: get value in multi process:" + value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected: RemoteService 断开连接，重新启动");
        }
    };

    private IReceiverAidlInterface iReceiverAidlInterface = new IReceiverAidlInterface.Stub() {

        @Override
        public void receiveMessage(int value) throws RemoteException {
            Log.e(TAG, "receiveMessage: print value from HeartBeat:" + value);
            mHeartBeatAidl.notifySuccess("I get HeartBeat success:" + value);
        }
    };

}
