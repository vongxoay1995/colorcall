package com.colorcall.callerscreen.splash;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.databinding.ActivitySplashBinding;
import com.colorcall.callerscreen.main.MainActivity;
import com.colorcall.callerscreen.onboarding.OnboardingActivity;
import com.colorcall.callerscreen.paywall.PayWallActivity;
import com.colorcall.callerscreen.update.UpdateManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.ConstantAds;
import com.colorcall.callerscreen.utils.GoogleMobileAdsConsentManager;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.JobScreen;
import com.colorcall.callerscreen.utils.billing.BillingHelper;
import com.colorcall.callerscreen.utils.billing.BillingListener;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.ump.FormError;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity implements JobScreen.JobProgress {
    private ActivitySplashBinding binding;

    private int progress;
    private Analystic analystic;
    private UpdateManager mUpdateManager;
    private JobScreen jobScreen;
    public AppOpenAd appOpenAds;
    private boolean isLoadAdError = false;
    private boolean isShowAds = false;
    public GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    private InterstitialAd mInterstitialAd;
    private boolean fullAdsLoaded = false;
    private boolean isShowingInter = false;
    private boolean loadFailed = false;
    BillingHelper billingHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        jobScreen = new JobScreen();
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        int countOpenApp = HawkHelper.getCountOpenApp();
        countOpenApp++;
        HawkHelper.setCountOpenApp(countOpenApp);
        new Thread(this::configFirebaseRemote).start();
        Glide.with(getApplicationContext())
                .load(R.drawable.ic_bg_splash)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .thumbnail(0.1f)
                .into(binding.imgBgSplash);
        mUpdateManager = UpdateManager.Builder(this);
        analystic = Analystic.getInstance(this);
        analystic.trackEvent(ManagerEvent.splashOpen());
        Hawk.put(ConstantAds.BEFORE_TIME, 0L);
        googleMobileAdsConsentManager =
                GoogleMobileAdsConsentManager.getInstance(getApplicationContext());
        //int countUpdate = HawkHelper.getCountShowDialogUpdate();
        // countUpdate++;
        // HawkHelper.setCountRate(countUpdate);
       /* if (countUpdate == 1 || countUpdate == 3 || countUpdate == 55) {
            callFlexibleUpdate();
        } else {
            checkShowAds();
        }*/
       /* mUpdateManager.addFlexibleUpdateDownloadListener(new UpdateManager.FlexibleUpdateDownloadListener() {
            @Override
            public void onDownloadProgress(final long bytesDownloaded, final long totalBytes) {
                Log.e("TAN", "onDownloadProgress: "+ bytesDownloaded + " / " + totalBytes);
            }
        });*/
        initBilling();
        if (!HawkHelper.isPayed()) {
            loadConsentForm();
            checkIAP();
        } else skip();
    }

    private void initBilling() {
        billingHelper = new BillingHelper(this);
        billingHelper.init();
        billingHelper.setListener(new BillingListener() {
            @Override
            public void onPurchaseUpdatedV5Below(@Nullable List<? extends SkuDetails> list) {

            }

            @Override
            public void onPurchaseUpdatedV5(@Nullable List<? extends Purchase> list) {

            }

            @Override
            public void setupBillingDone() {
                for (int i = 0; i < billingHelper.getProductDetails().size(); i++) {
                    ProductDetails productDetails = billingHelper.getProductDetails().get(i);
                    Log.e("TAN", "ProductDetails: " + productDetails);
                    if (productDetails.getProductId().equals(Constant.WEEK_LY)) {
                        if (productDetails.getSubscriptionOfferDetails() != null &&
                                !productDetails.getSubscriptionOfferDetails().isEmpty() &&
                                productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases() != null &&
                                productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().size() > 1) {
                            if (productDetails.getSubscriptionOfferDetails().get(0).getOfferId() != null && productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getPriceAmountMicros() == 0) {
                                Log.e("TAN", "setupBillingDone: HAS TRIAL");
                            }
                        }
                        break;
                    }
                }
            }

            @Override
            public void onUserCanceled() {
            }

            @Override
            public void setupBillingFailed(String s) {
                Log.e("TAN", "setupBillingFailed: " + s);
            }
        });
    }

    private void moveOnboarding() {
        try {
            if (isActive()) {
                stopJobScreen();
                startActivity(new Intent(this, OnboardingActivity.class));
                HawkHelper.setShowedOb(true);
                finish();
            }

        } catch (Exception e) {
            skip();
            e.printStackTrace();
        }

    }

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    private void configFirebaseRemote() {
        long cacheExpiration;
        if (BuildConfig.DEBUG) {
            cacheExpiration = 0;
        } else {
            cacheExpiration = 10; // 10 s same as the default value
        }
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(
                cacheExpiration).build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        fetchDataFromFirebase();
    }


    private void fetchDataFromFirebase() {
        mFirebaseRemoteConfig.fetch().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mFirebaseRemoteConfig.activate()
                        .addOnCompleteListener(task12 -> task12.addOnCompleteListener(task1 -> createAndPostFirebaseEvent())
                                .addOnCanceledListener(() -> createAndPostFirebaseEvent()))
                        .addOnCanceledListener(() -> createAndPostFirebaseEvent())
                        .addOnFailureListener(e -> createAndPostFirebaseEvent());
            } else {
                createAndPostFirebaseEvent();
            }
        }).addOnCanceledListener(this::createAndPostFirebaseEvent);
    }
    List<Integer> outputList = new ArrayList<>();
    private void createAndPostFirebaseEvent() {
        Long time = mFirebaseRemoteConfig.getLong(ConstantAds.TIME_BETWEEN_ADS);
        String numberShowPayWall = mFirebaseRemoteConfig.getString(ConstantAds.NUM_SHOW_PAYWALL);

        String[] stringArray = numberShowPayWall.split(",");

        for (String s : stringArray) {
            outputList.add(Integer.parseInt(s));
        }
        Log.e("TAN", "outputList: "+outputList );
        Hawk.put(ConstantAds.TIME_BETWEEN_ADS, time);
        Hawk.put("NumShowPayWall", outputList);
    }

    public void callFlexibleUpdate() {
        // Start a Flexible Update
        mUpdateManager.mode(AppUpdateType.FLEXIBLE).start();
    }

    private AppOpenAd.AppOpenAdLoadCallback loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            super.onAdFailedToLoad(loadAdError);
            if (isActive()) {
                isLoadAdError = true;
            }
        }

        @Override
        public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
            super.onAdLoaded(appOpenAd);
            if (isActive()) {
                isLoadAdError = false;
                appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
                appOpenAds = appOpenAd;
            }
        }
    };
    private FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
        @Override
        public void onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent();
            isShowAds = false;
            Hawk.put(ConstantAds.BEFORE_TIME, System.currentTimeMillis());
            skip();
        }

        @Override
        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
            super.onAdFailedToShowFullScreenContent(adError);
            isShowAds = false;
            Log.e("TAN", "onAdFailedToShowFullScreenContent: splash app open " + adError.getMessage());
            skip();
        }

        @Override
        public void onAdShowedFullScreenContent() {
            super.onAdShowedFullScreenContent();
            isShowAds = true;
        }
    };

    private boolean isActive() {
        return !isFinishing() && !isDestroyed();
    }


    public void hideLoading() {
        binding.layoutLoading.setVisibility(View.INVISIBLE);
        progress = 100;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (jobScreen != null) {
            jobScreen.startJob(this);
        }
    }

    private void stopJobScreen() {
        if (jobScreen != null) {
            jobScreen.stopJob();
        }
    }

    public void skip() {
        try {
            if (isActive()) {
                stopJobScreen();
                Intent failedIntent = new Intent(this, MainActivity.class);
                failedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                failedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(failedIntent);
                finish();
            }
        } catch (Exception e) {
            analystic.trackEvent("Error_Skip_Splash_To_Main");
            e.printStackTrace();
        }

    }

    private void loadConsentForm() {
        googleMobileAdsConsentManager.gatherConsent(
                this,
                new GoogleMobileAdsConsentManager.OnConsentGatheringCompleteListener() {
                    @Override
                    public void consentGatheringComplete(FormError error) {
                    }

                    @Override
                    public void conSentShow() {
                    }

                    @Override
                    public void conSentDismiss() {
                    }
                });
    }

    @Override
    protected void onDestroy() {
        stopJobScreen();
        billingHelper.destroy();
        super.onDestroy();
    }

    boolean actionMovePayWall = false;

    private void checkIAP() {
        if (AppUtils.isNetworkConnected(this)) {
            if (!HawkHelper.isShowedOb()) {
                loadInterAds();
            } else {
                AdRequest request = new AdRequest.Builder().build();
                String id_ads = "";
                if (BuildConfig.DEBUG) {
                    id_ads = ConstantAds.id_ads_open_test;
                } else {
                    id_ads = ConstantAds.id_splash_open_tk_cu;
                }
                if (Hawk.get("NumShowPayWall", new ArrayList<>()).contains(HawkHelper.getCountOpenApp())) {
                    actionMovePayWall = true;
                } else {
                    AppOpenAd.load(this, id_ads, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
                }
            }

            jobScreen.startJob(this);
        } else {
            Log.e("TAN", "checkIAP: no net work");
            skip();
        }
    }

    public void loadInterAds() {
        isShowingInter = true;
        String idInter;
        if (BuildConfig.DEBUG) {
            idInter = Constant.ID_INTER_TEST;
        } else {
            idInter = ConstantAds.inter_splash_cu;
        }
        //idInter = ID_ADS;
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, idInter, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        fullAdsLoaded = true;
                        mInterstitialAd = interstitialAd;
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                moveOnboarding();
                                // Called when fullscreen content is dismissed.
                                Log.e("TAG", "The ad was dismissed.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                moveOnboarding();
                                Log.d("TAG", "The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                mInterstitialAd = null;
                                isShowAds = true;
                                Log.e("TAG", "The ad was shown.");
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        loadFailed = true;
                        fullAdsLoaded = false;
                        mInterstitialAd = null;
                    }
                });
    }

    @Override
    public void onProgress(int count) {
        Log.e("TAN", "onProgress: count = " + count);
        if (!isActive() || isShowAds) {
            return;
        }
        progress++;
        if (isShowingInter) {
            if (mInterstitialAd != null) {
                stopJobScreen();
                isShowAds = true;
                mInterstitialAd.show(this);
                hideLoading();
            } else if ((!isShowAds && jobScreen.isProgressMax()) || isLoadAdError) {
                Log.e("TAN", "onProgress: skip");
                moveOnboarding();
            }
        } else {
            if (appOpenAds != null) {
                stopJobScreen();
                isShowAds = true;
                appOpenAds.show(this);
                hideLoading();
            } else if (actionMovePayWall && progress > 9) {
                stopJobScreen();
                hideLoading();
                movePayWall();
            } else if ((!isShowAds && jobScreen.isProgressMax()) || isLoadAdError) {
                Log.e("TAN", "onProgress: skip");
                skip();
            }
        }

    }

    private void movePayWall() {
        Intent intent = new Intent(this, PayWallActivity.class);
        intent.putExtra("from_scr","Splash");
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopJobScreen();
    }
}