package com.colorcall.callerscreen.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;

import com.applovin.mediation.AppLovinExtras;
import com.applovin.mediation.ApplovinAdapter;
import com.colorcall.callerscreen.R;
import com.google.ads.mediation.unity.UnityAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

public class BannerAdsUtils {
    private Context context;
    private AdView adviewGoogle;
    private String idGG;
    private RelativeLayout layoutBannerAds;
    private AdListener adListener;
    public BannerAdsUtils(Context context, RelativeLayout viewContainer) {
        this.context = context;
        this.layoutBannerAds = viewContainer;
    }
    public void setIdAds(String idGG) {
        this.idGG = idGG;
    }
    public void setAdListener(AdListener adListener){
        this.adListener = adListener;
    }
    public void showAds() {
        adsBannerGoogle();
    }
    public void showMediationBannerAds() {
        adviewGoogle = new com.google.android.gms.ads.AdView(context);
        com.google.android.gms.ads.AdSize adSize = getAdSize();
        adviewGoogle.setAdSize(adSize);
        adviewGoogle.setAdUnitId(idGG);
        layoutBannerAds.addView(adviewGoogle);
        Bundle aplovinExtras = new AppLovinExtras.Builder()
                .setMuteAudio(true)
                .build();
        Bundle utityExtras = new Bundle();
        AdRequest bannerAdRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(ApplovinAdapter.class, aplovinExtras)
                .addNetworkExtrasBundle(UnityAdapter.class, utityExtras)
                .build();
        adviewGoogle.loadAd(bannerAdRequest);
        adviewGoogle.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                if(adListener!=null){
                    adListener.onAdFailed();
                }
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdLoaded() {
                if(adListener!=null){
                    adListener.onAdloaded();
                }
                super.onAdLoaded();
            }
        });
    }
    public void adsBannerGoogle() {
        adviewGoogle = new com.google.android.gms.ads.AdView(context);
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        String[] ggTestDevices = context.getResources().getStringArray(R.array.google_test_device);
        for (String testDevice : ggTestDevices) {
            adRequestBuilder.addTestDevice(testDevice);
        }
        com.google.android.gms.ads.AdSize adSize = getAdSize();
        adviewGoogle.setAdSize(adSize);
        adviewGoogle.setAdUnitId(idGG);
        layoutBannerAds.addView(adviewGoogle);
        adviewGoogle.loadAd(adRequestBuilder.build());
        adviewGoogle.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                layoutBannerAds.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded() {
                layoutBannerAds.setVisibility(View.VISIBLE);
                super.onAdLoaded();
            }
        });
    }
    private com.google.android.gms.ads.AdSize getAdSize() {
        Display display =((Activity)context).getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        return com.google.android.gms.ads.AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }
}
