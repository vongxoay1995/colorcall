package com.colorcall.callerscreen.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.constan.Constant;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class InterstitialUtil {
    private String id_ads = "ca-app-pub-3222539657172474/2357386636";
    private static InterstitialUtil sInterstitial;
    private InterstitialAd interstitialAd;
    private AdCloseListener adCloseListener;
    private boolean isReload;
    private long limitTime;
    private Context mContext;

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
        interstitialAd = new InterstitialAd(context);
        getLimitTime();
        if (BuildConfig.DEBUG) {
            interstitialAd.setAdUnitId(Constant.ID_INTER_TEST);
        } else {
            interstitialAd.setAdUnitId(id_ads);
        }
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                if (adCloseListener != null) {
                    adCloseListener.onAdClose();
                }
                loadInterstitial(mContext);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                if (!isReload) {
                    isReload = true;
                    loadInterstitial(mContext);
                }
            }
        });
        loadInterstitial(mContext);
    }

    public void showInterstitialAds(AdCloseListener adCloseListener) {
        if (canShowInterstitial()) {
            this.adCloseListener = adCloseListener;
            if (System.currentTimeMillis() - HawkHelper.getLastTimeShowInter() > limitTime * 1000) {
                isReload = false;
                interstitialAd.show();
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
                .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            limitTime = FirebaseRemoteConfig.getInstance().getLong("interstitial_interval");
                            Log.e("TAN", "onComplete: " + limitTime);
                        } else {
                            Log.e("TAN", "Loi r: ");
                        }
                    }
                });
    }

    private void loadInterstitial(Context context) {
        if (interstitialAd != null && !interstitialAd.isLoading() && !interstitialAd.isLoaded()) {
            AdRequest.Builder adRequest = new AdRequest.Builder();
            String[] ggTestDevices = context.getResources().getStringArray(R.array.google_test_device);
            for (String testDevice : ggTestDevices) {
                adRequest.addTestDevice(testDevice);
            }
            interstitialAd.loadAd(adRequest.build());
        }
    }

    public boolean canShowInterstitial() {
        return interstitialAd != null && interstitialAd.isLoaded();
    }
}
