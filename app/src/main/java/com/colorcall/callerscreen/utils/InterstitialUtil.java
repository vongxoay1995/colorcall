package com.colorcall.callerscreen.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.constan.Constant;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class InterstitialUtil {
    private String id_ads = "ca-app-pub-3222539657172474/2357386636";
    private static InterstitialUtil sInterstitial;
    private InterstitialAd interstitialAd;
    private AdCloseListener adCloseListener;
    private boolean isReload;
    private long limitTime = 30;
    private Context mContext;
    private String idInter;

    public interface AdCloseListener {
        void onAdClose();

        void onMove();
    }

    public static InterstitialUtil getInstance() {
        if (sInterstitial == null) {
            sInterstitial = new InterstitialUtil();
        }
        return sInterstitial;
    }

    public void init(Context context) {
        mContext = context;
        getLimitTime();
        if (BuildConfig.DEBUG) {
            idInter = Constant.ID_INTER_TEST;
        } else {
            idInter = id_ads;
        }
        loadInterstitial(mContext);
    }

    public void showInterstitialAds(Activity activity,AdCloseListener adCloseListener) {
        if (canShowInterstitial()) {
            this.adCloseListener = adCloseListener;
            if (System.currentTimeMillis() - HawkHelper.getLastTimeShowInter() > limitTime * 1000) {
                isReload = false;
                interstitialAd.show(activity);
                HawkHelper.setLastTimeShowInter(System.currentTimeMillis());
            } else {
                adCloseListener.onMove();
            }
        } else {
            loadInterstitial(mContext);
            adCloseListener.onAdClose();
        }
    }

    private void getLimitTime() {
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        config.fetchAndActivate()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        limitTime = FirebaseRemoteConfig.getInstance().getLong("interstitial_interval");
                    }
                });
    }

    private void loadInterstitial(Context context) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, idInter, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        InterstitialUtil.this.interstitialAd = interstitialAd;
                        InterstitialUtil.this.interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                if (adCloseListener != null) {
                                    adCloseListener.onAdClose();
                                }
                                loadInterstitial(mContext);
                                // Called when fullscreen content is dismissed.
                                Log.e("TAG", "The ad was dismissed.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                Log.d("TAG", "The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                InterstitialUtil.this.interstitialAd = null;
                                Log.e("TAG", "The ad was shown.");
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        interstitialAd = null;
                        // Handle the error
                        if (!isReload) {
                            isReload = true;
                            loadInterstitial(mContext);
                        }
                    }
                });
    }

    public boolean canShowInterstitial() {
        return interstitialAd != null;
    }
}
