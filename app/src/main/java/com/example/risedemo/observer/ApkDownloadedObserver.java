package com.example.risedemo.observer;

import android.annotation.SuppressLint;
import android.os.FileObserver;
import android.util.Log;

/**
 * Android10下可用
 * https://developer.android.google.cn/reference/android/os/FileObserver
 */
@SuppressLint("LongLogTag")
public class ApkDownloadedObserver extends FileObserver {
    private static final String TAG = "Rise-ApkDownloadedObserver";

    public ApkDownloadedObserver(String path) {
        super(path, FileObserver.DELETE);
    }

    @Override
    public void onEvent(int event, String path) {
        Log.d(TAG, "onEvent path:" + path + ",event:" + event);
    }
}