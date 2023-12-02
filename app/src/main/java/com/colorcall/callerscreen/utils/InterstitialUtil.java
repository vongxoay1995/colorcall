package com.colorcall.callerscreen.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.constan.Constant;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Date;

public class InterstitialUtil {
    private String id_ads = "ca-app-pub-3134368447261649/2906807937";
    @SuppressLint("StaticFieldLeak")
    private static InterstitialUtil sInterstitial;
    private InterstitialAd interstitialAd;
    private AdCloseListener adCloseListener;
    private Context mContext;
    private String idInter;
    private long loadTime = 0;
    private boolean isLoading;
    private boolean isShowAds;
    public interface AdCloseListener {
        void onAdClose();
    }

    public static InterstitialUtil getInstance() {
        if (sInterstitial == null) {
            sInterstitial = new InterstitialUtil();
        }
        return sInterstitial;
    }

    public void init(Context context) {
        mContext = context;
        if (BuildConfig.DEBUG) {
            idInter = Constant.ID_INTER_TEST;
        } else {
            idInter = id_ads;
        }
        loadInterstitial(mContext,1);
    }

    public void showInterstitialAds(Activity activity, AdCloseListener adCloseListener) {
        this.adCloseListener = adCloseListener;
        if (canShowInterstitial()) {
            interstitialAd.show(activity);
        }else {
            loadInterstitial(activity,2);
            adCloseListener.onAdClose();
        }
    }
    public void showInterstitialAdsApply(Activity activity, AdCloseListener adCloseListener) {
        this.adCloseListener = adCloseListener;
        if (canShowInterstitial()) {
            interstitialAd.show(activity);
        } else {
            adCloseListener.onAdClose();
        }
    }
    private void loadInterstitial(Context context,int index) {
        if (isAdAvailable()) {
            return;
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, idInter, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        InterstitialUtil.this.interstitialAd = interstitialAd;
                        InterstitialUtil.this.loadTime = (new Date()).getTime();
                        InterstitialUtil.this.interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                InterstitialUtil.this.interstitialAd = null;
                                if (adCloseListener != null) {
                                    adCloseListener.onAdClose();
                                }
                                isShowAds = false;
                                loadInterstitial(mContext,3);
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                if (adCloseListener != null) {
                                    adCloseListener.onAdClose();
                                }
                                isShowAds = false;
                                // Called when fullscreen content failed to show.
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                isShowAds = true;

                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.

                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        //loadInterFacebook(context);
                    }
                });
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    public boolean isAdAvailable() {
        return interstitialAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }
    public boolean isShowAdsInter(){
        return isShowAds;
    }

    public boolean canShowInterstitial() {
        return interstitialAd != null;
    }
}
