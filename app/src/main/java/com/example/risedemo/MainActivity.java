package com.example.risedemo;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import com.example.risedemo.service.MessengerService;
import com.example.risedemo.observer.ApkDownloadedObserver;
import com.example.risedemo.service.MultiProcessPluginService;
import com.example.risedemo.workmanager.WorkManagerActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
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
            case R.id.bind_messenger_service:
                bindAsyncService();
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

    private MultiProcessPluginServiceAidl mMultiProcessPluginServiceAidl;
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


    /*********************************Test Messenger***********************************************/
    private void bindAsyncService() {
        Log.e(TAG, "bindAsyncService()");
        Intent intent = new Intent(this, MessengerService.class);
        bindService(intent, mMessengerServiceConnection, BIND_AUTO_CREATE);
    }

    private void unbindAsyncService() {
        Log.e(TAG, "unbindMultiProcessPluginService()");
        if (mMessengerServiceConnection != null) {
            unbindService(mMessengerServiceConnection);
        }
    }

    public static final int FROM_SERVER = 1;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FROM_SERVER:
                    Log.e(TAG, msg.getData().getString("reply"));
                    break;
                default:
                    break;
            }
        }
    };

    private Messenger clientMessenger = new Messenger(handler);
    private Messenger serverMessenger;

    private ServiceConnection mMessengerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serverMessenger = new Messenger(service);
            Message message = Message.obtain(handler, MessengerService.MSG_FROM_CLIENT);
            Bundle data = new Bundle();
            data.putString("msg", "I am from the client.");
            message.setData(data);
            message.replyTo = clientMessenger;
            try {
                serverMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, " mMessengerServiceConnection onServiceDisconnected()");
        }
    };

    /*********************************Test Messenger***********************************************/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: main activity");
        unbindMultiProcessPluginService();
        unbindAsyncService();
    }
}