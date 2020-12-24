package com.example.risedemo.monitor;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.example.risedemo.util.DaemonUtil;
import com.example.risedemo.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.example.risedemo.monitor.AppInstallDBManager.AppInstallDBData;

public class AppInstallObserver extends BroadcastReceiver {

    private int mSource;
    private Context mContext;
    private ActivityManager mActivityManager;
    private AppInstallDataSource mAppInstallDataSource;
    private static volatile AppInstallObserver mInstance;
    private volatile AtomicBoolean isRegisterReceiver = new AtomicBoolean(false);
    private EmptyActivityLifecycleCallbacks mActivityLifecycleCallbacks;

    private AppInstallObserver(Context context) {
        if (context instanceof Application) {
            mContext = context;
        } else {
            mContext = context.getApplicationContext();
        }
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        createAppInstallDataSource(context);
    }

    public static AppInstallObserver getInstance(Context context) {
        if (null == mInstance) {
            synchronized (AppInstallObserver.class) {
                if (null == mInstance) {
                    mInstance = new AppInstallObserver(context);
                }
            }
        }
        return mInstance;
    }

    public static boolean isOpenSwitch(Context context) {
        return TextUtils.equals("open", getInstance(context).getReportAppInstalledSwitch());
    }

    private void createAppInstallDataSource(Context context) {
        mAppInstallDataSource = new AppInstallDataSource(context);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallbacks() {
        if (mContext == null || !isOpenSwitch(mContext) || !isMainProcess(mContext, mActivityManager)) {
            return;
        }
        try {
            final Application application = (Application) mContext;
            if (application != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    mActivityLifecycleCallbacks = new EmptyActivityLifecycleCallbacks() {
                        @Override
                        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                            unregisterInstallReceiver();
                            application.unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
                        }
                    };
                    application.registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
                }
            }
        } catch (Exception e) {
            Logger.d("AppInstallDBManager register activity life exception:" + e.getMessage());
        }
    }

    private void registerInstallReceiver(Context context) {
        if (context == null) {
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        context.registerReceiver(this, filter);
    }

    public void unregisterInstallReceiver() {
        if (mContext != null && isRegisterReceiver.get()) {
            mSource = 0;
            try {
                mContext.unregisterReceiver(this);
            } catch (Exception e) {
            }
        }
    }

    public void start(final int source) {
        if (!isOpenSwitch(mContext)) {
            return;
        }
        mSource = source;
        boolean isPluginProcessNotExist = isPluginProcessNotExist(mContext, mActivityManager);
        if (isPluginProcessNotExist && !isRegisterReceiver.getAndSet(true)) {
            registerInstallReceiver(mContext);
            sendTrackingWhenNotReportInstall(mSource);
        }
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private boolean isMainProcess(Context context, ActivityManager activityManager) {
        if (context == null || activityManager == null) {
            return false;
        }
        int pid = android.os.Process.myPid();
        String pkgName = "com.test.main.process";
        try {
            for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
                if (processInfo.pid == pid) {
                    return TextUtils.equals(pkgName, processInfo.processName);
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private boolean isPluginProcessNotExist(Context context, ActivityManager activityManager) {
        if (context == null || activityManager == null) {
            return false;
        }
        String pluginName = context.getPackageName() + ":plugin";
        try {
            for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
                if (processInfo != null && TextUtils.equals(pluginName, processInfo.processName)) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Uri url = intent.getData();
        if (url == null || !Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            return;
        }
        String packageName = url.getSchemeSpecificPart();
        if (!TextUtils.isEmpty(packageName) && mSource > 0) {
            sendTrackingByPackageName(packageName, mSource);
        }
    }

    private void sendTrackingByPackageName(final String packageName, final int source) {
        if (mAppInstallDataSource != null) {
            mAppInstallDataSource.select(packageName, new AppInstallDataSource.Callback<AppInstallDBData>() {
                @Override
                public void onResult(AppInstallDBData appInstallDBData) {
                    toSendInstalledTracking(appInstallDBData, source);
                }
            });

        }
    }

    private void sendTrackingWhenNotReportInstall(final int source) {
        if (mAppInstallDataSource != null) {
            mAppInstallDataSource.query(false, new AppInstallDataSource.Callback<List<AppInstallDBData>>() {
                @Override
                public void onResult(List<AppInstallDBData> result) {
                    if (result != null && !result.isEmpty()) {
                        for (AppInstallDBData appInstallDBData : result) {
                            if (appInstallDBDataEffective(appInstallDBData)
                                    && DaemonUtil.isAppInstalled(mContext, appInstallDBData.packageName)) {
                                toSendInstalledTracking(appInstallDBData, source);
                            }
                        }
                    }
                }
            });
        }
    }

    private void toSendInstalledTracking(AppInstallDBData appInstallDBData, int source) {
        if (appInstallDBDataEffective(appInstallDBData)) {
            //todo send
            mAppInstallDataSource.updateReportedInstall(appInstallDBData.packageName, true);
        }
    }

    public void onCallBackDownloadStart(String data, Map<Enum, String> extParam) {
        if (!isOpenSwitch(mContext)) {
            return;
        }

        AppInstallDBData appInstallDBData = wrapperAppInstallReportData(data, extParam);
        if (mAppInstallDataSource != null && appInstallDBDataEffective(appInstallDBData)) {
            mAppInstallDataSource.replace(appInstallDBData);
        }
    }

    public void onCallBackInstallFinished(String data, AppInstallDataSource.Callback<Boolean> callback) {
        if (!isOpenSwitch(mContext)) {
            callback.onResult(false);
            return;
        }
        String apkName = getAppPackageName(data);
        if (mAppInstallDataSource != null && !TextUtils.isEmpty(apkName)) {
            mAppInstallDataSource.isReportedInstallValue(apkName, callback);
        }
    }

    private boolean appInstallDBDataEffective(AppInstallDBData appInstallDBData) {
        return appInstallDBData != null && appInstallDBData.isEffective();
    }

    private String getAppPackageName(String data) {
        try {
            return new JSONObject(data).optString("name");
        } catch (JSONException e) {
            Logger.d("AppInstallDBManager getAppPackageName e:" + e.getMessage());
        }
        return null;
    }

    private AppInstallDBData wrapperAppInstallReportData(String data, Map<Enum, String> extParam) {
        AppInstallDBData appInstallDBData = new AppInstallDBData();
        try {
            JSONObject dataJSONObject = new JSONObject(data);
            JSONObject res = new JSONObject();
            //extParam
            JSONObject ext = new JSONObject();
            res.put("extra", ext);
            if (extParam != null && !extParam.isEmpty()) {
                for (Map.Entry<Enum, String> entry : extParam.entrySet()) {
                    if (entry == null || entry.getKey() == null) {
                        continue;
                    }
                    String key = entry.getKey().name();
                    String value = entry.getValue();
                    ext.put(key, value);
                }
            }
            appInstallDBData.packageName = "name";
            appInstallDBData.reportedInstall = false;
            appInstallDBData.data = res.toString();
            return appInstallDBData;
        } catch (JSONException e) {
            Logger.d("AppInstallDBManager wrapperAppInstallReportData e:" + e.getMessage());
        }
        return null;
    }

    private Map<Enum, String> getReportExtParams(String data, int source) {
        Map<Enum, String> extParamMap = new HashMap<>();
        try {
            JSONObject ext = new JSONObject(data).optJSONObject("extra");
            Iterator keys = ext.keys();
            while (keys.hasNext()) {
                String key = keys.next().toString();
                String value = ext.optString(key);
            }
        } catch (JSONException e) {
            Logger.d("AppInstallDBManager getReportExtParams e:" + e.getMessage());
            extParamMap.clear();
        }
        return extParamMap;
    }

    public void saveReportAppInstalledSwitch(String value) {
        if (mAppInstallDataSource != null) {
            mAppInstallDataSource.saveAppInstalledSwitch(value);
        }
    }

    private String getReportAppInstalledSwitch() {
        return mAppInstallDataSource != null ?
                mAppInstallDataSource.getAppInstalledSwitch("key") : null;
    }

}
