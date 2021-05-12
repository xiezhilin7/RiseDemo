package com.example.risedemo.ad;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.risedemo.R;
import com.example.risedemo.workmanager.MyPeriodicWork;
import com.example.risedemo.workmanager.MyWork;
import com.example.risedemo.workmanager.MyWorkA;
import com.example.risedemo.workmanager.MyWorkB;
import com.example.risedemo.workmanager.MyWorkC;
import com.example.risedemo.workmanager.MyWorkWithData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 本activity用来测试applovin广告接入
 */
public class AdTestActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Rise-AppLovin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applovin);
        findViewById(R.id.load_applovin_button).setOnClickListener(this);
        findViewById(R.id.show_applovin_button).setOnClickListener(this);
        //可以在activity中init，也可以放到application中去init
        AdManager.initAppLovinSdk(this, callBack);
    }

    private AdManager.AppLovinSdkInitCallBack callBack = new AdManager.AppLovinSdkInitCallBack() {
        @Override
        public void onSdkInitialized() {
            loadAd();
        }
    };

    public void loadAd() {
        AdManager.loadMaxRewardedAd(AdTestActivity.this);
    }

    private void showAd() {
        AdManager.showMaxRewardedAd();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.load_applovin_button:
                loadAd();
                break;
            case R.id.show_applovin_button:
                showAd();
                break;
            default:
                break;
        }
    }
}
