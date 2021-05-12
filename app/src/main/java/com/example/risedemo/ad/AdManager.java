package com.example.risedemo.ad;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;

import java.util.concurrent.TimeUnit;

public class AdManager {

    private static final String TAG = "AdManager";
    private static volatile boolean mSdkInitialized = false;

    public interface AppLovinSdkInitCallBack {
        void onSdkInitialized();
    }

    public static void initAppLovinSdk(Context context, AppLovinSdkInitCallBack callBack) {
        // Please make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance(context.getApplicationContext()).setMediationProvider(AppLovinMediationProvider.MAX);
        AppLovinSdk.initializeSdk(context.getApplicationContext(), new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {
                if (configuration != null) {
                    Log.d(TAG, "sdk init success: CountryCode:" + configuration.getCountryCode()
                            + ",ConsentDialogStateName:" + configuration.getConsentDialogState() != null ? configuration.getConsentDialogState().name() : null);
                }
                mSdkInitialized = true;
                if (callBack != null) {
                    callBack.onSdkInitialized();
                }
            }
        });

//        AppLovinSdk.getInstance(context).showMediationDebugger();
    }

    private static int retryAttempt;
    private static MaxRewardedAd rewardedAd;

    public static void loadMaxRewardedAd(Activity activity) {
        //YOUR_AD_UNIT_ID 这里需要填写你的广告位ID
        if (rewardedAd == null) {
            rewardedAd = MaxRewardedAd.getInstance("YOUR_AD_UNIT_ID", activity);
            rewardedAd.setListener(new MaxRewardedAdListener() {
                // MAX Ad Listener
                @Override
                public void onAdLoaded(final MaxAd maxAd) {
                    // Rewarded ad is ready to be shown. rewardedAd.isReady() will now return 'true'

                    // Reset retry attempt
                    retryAttempt = 0;
                }

                @Override
                public void onAdLoadFailed(final String adUnitId, final int errorCode) {
                    // Rewarded ad failed to load
                    // We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)
                    //重试请求广告
                    retryAttempt++;
                    long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));

//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        rewardedAd.loadAd();
//                    }
//                }, delayMillis);

                }

                @Override
                public void onAdDisplayFailed(final MaxAd maxAd, final int errorCode) {
                    // Rewarded ad failed to display. We recommend loading the next ad
                    //重试请求广告
//                rewardedAd.loadAd();
                }

                @Override
                public void onAdDisplayed(final MaxAd maxAd) {
                }

                @Override
                public void onAdClicked(final MaxAd maxAd) {
                }

                @Override
                public void onAdHidden(final MaxAd maxAd) {
                    // rewarded ad is hidden. Pre-load the next ad
                    rewardedAd.loadAd();
                }

                @Override
                public void onRewardedVideoStarted(final MaxAd maxAd) {
                }

                @Override
                public void onRewardedVideoCompleted(final MaxAd maxAd) {
                }

                @Override
                public void onUserRewarded(final MaxAd maxAd, final MaxReward maxReward) {
                    // Rewarded ad was displayed and user should receive the reward
                }
            });
        }
        if (rewardedAd != null) {
            rewardedAd.loadAd();
        }
    }

    public static void showMaxRewardedAd() {
        if (rewardedAd.isReady()) {
            rewardedAd.showAd();
        }
    }


}
