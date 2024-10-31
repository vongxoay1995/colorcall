package com.colorcall.callerscreen.splash;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.databinding.ActivitySplashBinding;
import com.colorcall.callerscreen.main.MainActivity;
import com.colorcall.callerscreen.update.UpdateManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.ConstantAds;
import com.colorcall.callerscreen.utils.GoogleMobileAdsConsentManager;
import com.colorcall.callerscreen.utils.JobScreen;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.ump.FormError;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.orhanobut.hawk.Hawk;

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

        loadConsentForm();
        checkIAP();
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

    private void createAndPostFirebaseEvent() {
        Long time = mFirebaseRemoteConfig.getLong(ConstantAds.TIME_BETWEEN_ADS);
        Hawk.put(ConstantAds.TIME_BETWEEN_ADS, time);
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
        if (isActive()) {
            stopJobScreen();
            Intent failedIntent = new Intent(SplashActivity.this, MainActivity.class);
            failedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            failedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(failedIntent);
            finish();
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
        super.onDestroy();
    }


    private void checkIAP() {
        if (AppUtils.isNetworkConnected(this)) {
            AdRequest request = new AdRequest.Builder().build();
            String id_ads = "";
            if (BuildConfig.DEBUG) {
                id_ads = ConstantAds.id_ads_open_test;
            } else {
                id_ads = ConstantAds.id_splash_open_admob2;
            }
            AppOpenAd.load(this, id_ads, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
            jobScreen.startJob(this);
        } else {
            Log.e("TAN", "checkIAP: no net work");
            skip();
        }
    }

    @Override
    public void onProgress(int count) {
        Log.e("TAN", "onProgress: count = " + count);
        if (!isActive() || isShowAds) {
            return;
        }
        progress++;
        binding.seekbar.setProgress(progress);
        if (appOpenAds != null) {
            stopJobScreen();
            isShowAds = true;
            appOpenAds.show(this);
            hideLoading();
        } else if ((!isShowAds && jobScreen.isProgressMax()) || isLoadAdError) {
            Log.e("TAN", "onProgress: skip");
            skip();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopJobScreen();
    }
}