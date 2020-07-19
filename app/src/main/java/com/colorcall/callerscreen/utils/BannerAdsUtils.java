package com.colorcall.callerscreen.utils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colorcall.callerscreen.R;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

public class BannerAdsUtils {
    private Context context;
    private AdView adviewGoogle;
    private String idGG;
    private RelativeLayout layoutBannerAds;
    public BannerAdsUtils(Context context, RelativeLayout viewContainer) {
        this.context = context;
        this.layoutBannerAds = viewContainer;
    }
    public void setIdAds(String idGG) {
        this.idGG = idGG;
    }

    public void showAds() {
        adsBannerGoogle();
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
    public void showNativeAdsGG(String idGG) {
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        String[] ggTestDevices = context.getResources().getStringArray(R.array.google_test_device);
        for (String testDevice : ggTestDevices) {
            adRequestBuilder.addTestDevice(testDevice);
        }
        AdLoader adLoader = new AdLoader.Builder(context,idGG)
                .forUnifiedNativeAd(unifiedNativeAd -> {
                    UnifiedNativeAdView unifiedNativeAdView = (UnifiedNativeAdView) ((Activity)context).getLayoutInflater().inflate(R.layout.small_native_ad_layout, null);
                    mapUnifiedNativeAdToLayout(unifiedNativeAd, unifiedNativeAdView);
                    layoutBannerAds.removeAllViews();
                    layoutBannerAds.addView(unifiedNativeAdView);
                }).withAdListener(new com.google.android.gms.ads.AdListener() {
                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        layoutBannerAds.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAdFailedToLoad(int i) {
                        layoutBannerAds.removeAllViews();
                        super.onAdFailedToLoad(i);
                    }
                })
                .build();
        adLoader.loadAd(adRequestBuilder.build());
    }
    public void mapUnifiedNativeAdToLayout(UnifiedNativeAd adFromGoogle, UnifiedNativeAdView myAdView) {
        myAdView.setHeadlineView(myAdView.findViewById(R.id.ad_headline));
        myAdView.setBodyView(myAdView.findViewById(R.id.ad_body));
        myAdView.setCallToActionView(myAdView.findViewById(R.id.ad_call_to_action));
        myAdView.setIconView(myAdView.findViewById(R.id.ad_icon));
        myAdView.setPriceView(myAdView.findViewById(R.id.ad_price));
        myAdView.setStarRatingView(myAdView.findViewById(R.id.ad_rating));
        myAdView.setStoreView(myAdView.findViewById(R.id.ad_store));
        myAdView.setAdvertiserView(myAdView.findViewById(R.id.ad_advertiser));
        setListener(myAdView.findViewById(R.id.ad_headline));
        setListener(myAdView.findViewById(R.id.ad_body));
        setListener(myAdView.findViewById(R.id.ad_price));
        setListener(myAdView.findViewById(R.id.ad_store));
        ((TextView) myAdView.getBodyView()).setMaxLines(1);
        ((TextView) myAdView.getBodyView()).setEllipsize(TextUtils.TruncateAt.END);


        ((TextView) myAdView.getHeadlineView()).setMaxLines(1);
        ((TextView) myAdView.getHeadlineView()).setEllipsize(TextUtils.TruncateAt.END);

        ((TextView) myAdView.getHeadlineView()).setText(adFromGoogle.getHeadline());

        if (adFromGoogle.getBody() == null) {
            myAdView.getBodyView().setVisibility(View.GONE);
        } else {
            myAdView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) myAdView.getBodyView()).setText(adFromGoogle.getBody());
        }

        if (adFromGoogle.getCallToAction() == null) {
            myAdView.getCallToActionView().setVisibility(View.GONE);
        } else {
            ((Button) myAdView.getCallToActionView()).setText(adFromGoogle.getCallToAction());
        }

        if (adFromGoogle.getIcon() == null) {
            myAdView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) myAdView.getIconView()).setImageDrawable(adFromGoogle.getIcon().getDrawable());
        }

        if (adFromGoogle.getPrice() == null) {
            myAdView.getPriceView().setVisibility(View.GONE);
        } else {
            if (adFromGoogle.getPrice().equals("")) {
                myAdView.getPriceView().setVisibility(View.GONE);
            } else {
                myAdView.getPriceView().setVisibility(View.VISIBLE);
                ((TextView) myAdView.getPriceView()).setText(adFromGoogle.getPrice());
            }
        }

        if (adFromGoogle.getStarRating() == null) {
            myAdView.getStarRatingView().setVisibility(View.GONE);
        } else {
            myAdView.getStarRatingView().setVisibility(View.VISIBLE);
            ((RatingBar) myAdView.getStarRatingView()).setRating(adFromGoogle.getStarRating().floatValue());
        }

        if (adFromGoogle.getStore() == null) {
            myAdView.getStoreView().setVisibility(View.GONE);
        } else {
            myAdView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) myAdView.getStoreView()).setText(adFromGoogle.getStore());
        }

        if (adFromGoogle.getAdvertiser() == null) {
            myAdView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) myAdView.getAdvertiserView()).setText(adFromGoogle.getAdvertiser());
        }

        myAdView.setNativeAd(adFromGoogle);
    }
    private void setListener(View viewById) {
        viewById.setOnClickListener(v -> {

        });
    }
}
