package com.example.risedemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.example.risedemo.observer.ApkDownloadedObserver;
import com.example.risedemo.workmanager.WorkManagerActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
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
                DaemonHolder.startService();
                break;
            case R.id.daemon_service_stop:
                DaemonHolder.stopService();
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

}