package com.example.risedemo.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

/**
 *
 */
public class DaemonUtil {
    private static final long INTERVAL_TIME = 5 * 1000;
    private static final String BRAND = Build.BRAND.toLowerCase();
    private static ActivityManager activityManager;

    public static ActivityManager getActivityManager(Context context) {
        if (activityManager == null) {
            activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        return activityManager;
    }

    public static boolean isServiceRunning(Context context, String serviceName) {
        if (TextUtils.isEmpty(serviceName)) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo serviceInfo : getActivityManager(context).getRunningServices(100)) {
            if (serviceInfo.service.getClassName().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isProcessRunning(Context context, String processName) {
        for (ActivityManager.RunningAppProcessInfo processInfo : getActivityManager(context).getRunningAppProcesses()) {
            if (processInfo.processName.equals(processName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isProcessRunning(Context context) {
        return isProcessRunning(context, getProcessName(context));
    }

    public static String getProcessName(Context context, int pid) {
        for (ActivityManager.RunningAppProcessInfo processInfo : getActivityManager(context).getRunningAppProcesses()) {
            if (processInfo != null && processInfo.pid == pid) {
                return processInfo.processName;
            }
        }
        return null;
    }

    public static String getProcessName(Context context) {
        return getProcessName(context, android.os.Process.myPid());
    }

    public static boolean isMainProcess(Context context) {
        if (context == null) {
            return false;
        }
        String pkgName = context.getPackageName();
        try {
            String currentProcessName = getCurProcessName(context);
            return pkgName.equals(currentProcessName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        if (mActivityManager == null || mActivityManager.getRunningAppProcesses() == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    public static long getIntervalTime() {
        return INTERVAL_TIME;
    }

    public static boolean isAppInstalled(Context context, String pkgName) {
        if (context == null) {
            return false;
        }
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (Exception e) {
            Log.e("Exception:", e.getMessage());
            return false;
        }
        return pi != null;
    }


}
