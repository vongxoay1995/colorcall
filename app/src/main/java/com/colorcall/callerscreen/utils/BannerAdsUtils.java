package com.colorcall.callerscreen.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.RelativeLayout;

import com.applovin.mediation.AppLovinExtras;
import com.applovin.mediation.ApplovinAdapter;
import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.constan.Constant;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSize;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

public class BannerAdsUtils {
    private Context context;
    private AdView adviewGoogle;
    private String idGG;
    private RelativeLayout layoutBannerAds;
    private AdListener adListener;
    private com.facebook.ads.AdView adViewFb;

    public BannerAdsUtils(Context context, RelativeLayout viewContainer) {
        this.context = context;
        this.layoutBannerAds = viewContainer;
    }
    public void setIdAds(String idGG,String idFb) {
        if (BuildConfig.DEBUG) {
            this.idGG = Constant.ID_TEST_BANNER_ADMOD;
        } else {
            this.idGG = idGG;
        }
        adViewFb = new com.facebook.ads.AdView(context, idFb, AdSize.BANNER_HEIGHT_50);
        //this.idGG = idGG;
    }
    public void setAdListener(AdListener adListener){
        this.adListener = adListener;
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
        AdRequest bannerAdRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(ApplovinAdapter.class, aplovinExtras)
                .build();
        adviewGoogle.loadAd(bannerAdRequest);
        adviewGoogle.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                layoutBannerAds.removeAllViews();
                showAdsFB(layoutBannerAds,adListener);
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

    private void showAdsFB(RelativeLayout view,AdListener adLis) {
        com.facebook.ads.AdListener adListener = new com.facebook.ads.AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                if(adLis!=null){
                    adLis.onAdFailed();
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }
        };
        view.addView(adViewFb);
        adViewFb.loadAd(adViewFb.buildLoadAdConfig().withAdListener(adListener).build());
    }
    public void destroyFb() {
        if (adViewFb != null) {
            adViewFb.destroy();
        }
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
