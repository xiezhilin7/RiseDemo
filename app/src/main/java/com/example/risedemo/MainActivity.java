package com.example.risedemo;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import com.example.risedemo.observer.ApkDownloadedObserver;
import com.example.risedemo.service.MultiProcessPluginService;
import com.example.risedemo.workmanager.WorkManagerActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;


@SuppressLint("LongLogTag")
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Rise-MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: main activity");
        findViewById(R.id.work_manager_button).setOnClickListener(this::onClick);
        findViewById(R.id.file_observer_button).setOnClickListener(this::onClick);
        findViewById(R.id.daemon_service_start).setOnClickListener(this::onClick);
        findViewById(R.id.daemon_service_stop).setOnClickListener(this::onClick);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.work_manager_button:
                startActivity(new Intent(this, WorkManagerActivity.class));
                break;
            case R.id.file_observer_button:
                startFileObserverWatching();
                break;
            case R.id.daemon_service_start:
                bindMultiProcessPluginService();
                break;
            case R.id.daemon_service_stop:
                notifyMultiProcessGetMainProcessValue();
                break;
            default:
                break;
        }
    }

    /*********************************Test FileObserver***********************************************/
    ApkDownloadedObserver mApkDownloadedObserver;

    private void startFileObserverWatching() {
        String path = Environment.getExternalStorageDirectory().getPath();
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        Log.d(TAG, "startFileObserverWatching path:" + path);
        mApkDownloadedObserver = new ApkDownloadedObserver(path);
        mApkDownloadedObserver.startWatching();
    }
    /*********************************Test FileObserver***********************************************/


    /*********************************Test MultiProcessPluginService***********************************************/
    private void bindMultiProcessPluginService() {
        Log.e(TAG, "bindMultiProcessPluginService()");
        bindService(new Intent(this, MultiProcessPluginService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    private void notifyMultiProcessGetMainProcessValue() {
        Log.e(TAG, "notifyMultiProcessGetMainProcessValue() " + (mMultiProcessPluginServiceAidl != null));
        if (mMultiProcessPluginServiceAidl != null) {
            try {
                mMultiProcessPluginServiceAidl.notifyMultiProcessCanGetMainProcessValue();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void unbindMultiProcessPluginService() {
        Log.e(TAG, "unbindMultiProcessPluginService()");
        if (mServiceConnection != null) {
            unbindService(mServiceConnection);
        }
    }

    MultiProcessPluginServiceAidl mMultiProcessPluginServiceAidl;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMultiProcessPluginServiceAidl = MultiProcessPluginServiceAidl.Stub.asInterface(service);
            try {
                String value = mMultiProcessPluginServiceAidl.getValueInMultiProcess();
                Log.e(TAG, "onServiceConnected: get value in multi process:" + value);
                mMultiProcessPluginServiceAidl.transmitValueInMainProcess("this value from main process read sp");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected: RemoteService 断开连接，重新启动");
        }
    };

    /*********************************Test MultiProcessPluginService***********************************************/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: main activity");
        unbindMultiProcessPluginService();
    }
}