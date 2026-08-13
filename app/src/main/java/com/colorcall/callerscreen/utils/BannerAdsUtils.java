package com.colorcall.callerscreen.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;

import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.model.AdsConfig;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

public class BannerAdsUtils {
    private Context context;
    private AdView adviewGoogle;
    private String idGG;
    private RelativeLayout layoutBannerAds;
    private AdListener adListener;
    AdsConfig adsConfig;
    public BannerAdsUtils(Context context, RelativeLayout viewContainer) {
        this.context = context;
        this.layoutBannerAds = viewContainer;
        adsConfig = AdsConfigStorage.get();
    }
    public void setIdAds(String idGG) {
        if (BuildConfig.USE_TEST_ADS) {
            this.idGG = Constant.ID_TEST_BANNER_ADMOD;
        } else {
            this.idGG = idGG;
        }
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
        adviewGoogle.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                layoutBannerAds.removeAllViews();
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
    public void loadAds() {
        if (adsConfig != null && !adsConfig.getBanner_enable()) {
            if(adListener!=null){
                adListener.onAdFailed();
            }
            return;
        }
        adviewGoogle = new AdView(context);
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        com.google.android.gms.ads.AdSize adSize = getAdSize();
        adviewGoogle.setAdSize(adSize);
        adviewGoogle.setAdUnitId(idGG);
        adviewGoogle.loadAd(adRequestBuilder.build());
        adviewGoogle.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                layoutBannerAds.removeAllViews();
                layoutBannerAds.setVisibility(View.GONE);
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdLoaded() {
                layoutBannerAds.setVisibility(View.VISIBLE);
                layoutBannerAds.removeAllViews();
                layoutBannerAds.addView(adviewGoogle);
                super.onAdLoaded();
                if(adListener!=null){
                    adListener.onAdloaded();
                }
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();

            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
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
    private Boolean isCollapsible = false;
    private Boolean isLoading = false;

    public void loadCollapsibleBanner() {
        if (adsConfig != null && !adsConfig.getBanner_enable()){
            if(adListener!=null){
                adListener.onAdFailed();
            }
            return;
        }
        isCollapsible = true;
        if (isLoading) {
            return;
        }
        isLoading = true;
        adviewGoogle = new AdView(context);

        Bundle extras = new Bundle();
        extras.putString("collapsible", "bottom");


        AdSize adSize = getAdSize();  // Assuming adSize is a method or property
        adviewGoogle.setAdSize(adSize);

        if (BuildConfig.USE_TEST_ADS) {
            adviewGoogle.setAdUnitId(Constant.ID_TEST_BANNER_ADMOD);
        } else {
            adviewGoogle.setAdUnitId(idGG);
        }
        AdRequest adRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();
        adviewGoogle.loadAd(adRequest);
        adviewGoogle.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Log.e("TAN", "onAdFailedToLoad: "+loadAdError.getMessage() );
                isLoading = false;
                if(adListener!=null){
                    adListener.onAdFailed();
                }
            }

            @Override
            public void onAdLoaded() {
                Log.e("TAN", "onAdLoaded: " );
                isLoading = false;
                layoutBannerAds.setVisibility(View.VISIBLE);
                layoutBannerAds.removeAllViews();
                layoutBannerAds.addView(adviewGoogle);
                super.onAdLoaded();
                if(adListener!=null){
                    adListener.onAdloaded();
                }
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (isCollapsible) {
                    loadCollapsibleBanner();
                } else {
                    loadAds();
                }

            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }
        });
    }
}
