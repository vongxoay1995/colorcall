package com.colorcall.callerscreen.utils;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.constan.Constant;
import com.facebook.ads.Ad;
import com.facebook.ads.AdSettings;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Date;

public class InterstitialUtil {
    private String id_ads = "ca-app-pub-3222539657172474/2357386636";
    private String id_fb = "1205962693239181_1205994856569298";
    private static InterstitialUtil sInterstitial;
    private InterstitialAd interstitialAd;
    private com.facebook.ads.InterstitialAd interstitialAdFb;

    private AdCloseListener adCloseListener;
    private boolean isReload;
    private Context mContext;
    private String idInter;
    private long loadTime = 0;
    private long loadTimeFb = 0;
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
        loadInterstitial(mContext);
    }

    public void showInterstitialAds(Activity activity, AdCloseListener adCloseListener) {
        this.adCloseListener = adCloseListener;
        if (canShowInterstitial()) {
            isReload = false;
            interstitialAd.show(activity);
        } else if (canShowInterFb()) {
            isReload = false;
            interstitialAdFb.show();
        } else {
            loadInterstitial(mContext);
            adCloseListener.onAdClose();
        }
    }

    private void loadInterstitial(Context context) {
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
                                if (adCloseListener != null) {
                                    adCloseListener.onAdClose();
                                }
                                loadInterstitial(mContext);
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                if (adCloseListener != null) {
                                    adCloseListener.onAdClose();
                                }
                                // Called when fullscreen content failed to show.
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                InterstitialUtil.this.interstitialAd = null;
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
                            return;
                        }
                        loadInterFacebook(context);
                    }
                });
    }

    private void loadInterFacebook(Context context) {
        if (isAdAvailableFb()) {
            return;
        }
        AdSettings.addTestDevice("6828bd47-b305-4dae-9d34-a2b6f5733a1a");
        interstitialAdFb = new com.facebook.ads.InterstitialAd(context, id_fb);
        // Create listeners for the Interstitial Ad
        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                InterstitialUtil.this.interstitialAdFb = null;
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                if (adCloseListener != null) {
                    adCloseListener.onAdClose();
                }
                loadInterFacebook(mContext);
            }

            @Override
            public void onError(Ad ad, com.facebook.ads.AdError adError) {
                InterstitialUtil.this.interstitialAdFb = null;
            }

            @Override
            public void onAdLoaded(Ad ad) {
                InterstitialUtil.this.loadTimeFb = (new Date()).getTime();
                // Interstitial ad is loaded and ready to be displayed
                // Show the ad
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
            }
        };
        interstitialAdFb.loadAd(
                interstitialAdFb.buildLoadAdConfig()
                        .withAdListener(interstitialAdListener)
                        .build());
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }
    private boolean wasLoadTimeLessThanNHoursAgoFb(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTimeFb;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    public boolean isAdAvailable() {
        return interstitialAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }

    public boolean isAdAvailableFb() {
        return interstitialAdFb != null && wasLoadTimeLessThanNHoursAgoFb(4);
    }

    public boolean canShowInterstitial() {
        return interstitialAd != null;
    }
    public boolean canShowInterFb() {
        return interstitialAdFb != null&&interstitialAdFb.isAdLoaded();
    }
    public void onDestroy() {
        if (interstitialAdFb != null) {
            interstitialAdFb.destroy();
        }
    }
}
