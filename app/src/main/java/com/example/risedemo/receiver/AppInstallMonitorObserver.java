package com.example.risedemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

/**
 * 通过BroadcastReceiver监听PACKAGE_ADDED
 */
public class AppInstallMonitorObserver extends BroadcastReceiver {

    private static volatile AppInstallMonitorObserver mInstance;
    private CallBack mCallBack;

    public static AppInstallMonitorObserver getInstance(Context context) {
        if (null == mInstance) {
            synchronized (AppInstallMonitorObserver.class) {
                if (null == mInstance) {
                    mInstance = new AppInstallMonitorObserver(context);
                }
            }
        }
        return mInstance;
    }

    private AppInstallMonitorObserver(Context context) {
        registerInstallReceiver(context);
    }

    public void addObserver(CallBack callBack) {
        mCallBack = callBack;
    }

    private void registerInstallReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addDataScheme("package");
        context.getApplicationContext().registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Uri url = intent.getData();
        if (url == null || !Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            return;
        }
        String packageName = url.getSchemeSpecificPart();
        if (mCallBack != null) {
            mCallBack.update(packageName);
        }
    }

    public interface CallBack {
        void update(String packageName);
    }

}
