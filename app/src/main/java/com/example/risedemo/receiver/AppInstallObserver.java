package com.example.risedemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

/**
 * 通过BroadcastReceiver监听PACKAGE_ADDED
 */
public class AppInstallObserver extends BroadcastReceiver {

    private static volatile AppInstallObserver mInstance;
    private CallBack mCallBack;

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

    private AppInstallObserver(Context context) {
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
