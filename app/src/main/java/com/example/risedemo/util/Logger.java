package com.example.risedemo.util;

import android.util.Log;

public final class Logger {

    public enum LEVEL {
        CUPID_LOG_LEVEL_DEBUG,
        CUPID_LOG_LEVEL_INFO,
        CUPID_LOG_LEVEL_WARNING,
        CUPID_LOG_LEVEL_ERROR,
        CUPID_LOG_LEVEL_NONE
    }

    public static LEVEL logLevel = LEVEL.CUPID_LOG_LEVEL_DEBUG;
    private static String TAG = "Rise ";
    private static final int MAX_SIZE = 3900;

    public static void d(String msg) {
        if (logLevel.compareTo(LEVEL.CUPID_LOG_LEVEL_DEBUG) <= 0) {
            if (null == msg) {
                return;
            }
            int length = msg.length();
            int curIndex = 0;
            while (curIndex + MAX_SIZE < length) {
                Log.d(TAG, msg.substring(curIndex, curIndex + MAX_SIZE));
                curIndex += MAX_SIZE;
            }
            Log.d(TAG, msg.substring(curIndex, length));
        }
    }

    public static void i(String msg) {
        if (logLevel.compareTo(LEVEL.CUPID_LOG_LEVEL_INFO) <= 0) {
            Log.i(TAG, msg);
        }
    }

    public static void w(String msg) {
        if (logLevel.compareTo(LEVEL.CUPID_LOG_LEVEL_WARNING) <= 0) {
            Log.w(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (logLevel.compareTo(LEVEL.CUPID_LOG_LEVEL_ERROR) <= 0) {
            Log.e(TAG, msg);
        }
    }

    public static void e(String msg, Throwable t) {
        if (logLevel.compareTo(LEVEL.CUPID_LOG_LEVEL_ERROR) <= 0) {
            Log.e(TAG, msg, t);
        }
    }
}