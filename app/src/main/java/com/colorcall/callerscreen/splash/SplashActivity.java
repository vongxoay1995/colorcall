package com.colorcall.callerscreen.splash;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.FirebaseAnalystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.main.MainActivity;
import com.colorcall.callerscreen.utils.AppUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SplashActivity extends AppCompatActivity {

    @BindView(R.id.imgBgSplash)
    ImageView imgBgSplash;
    @BindView(R.id.seekbar)
    SeekBar seekbar;
    @BindView(R.id.layout_loading)
    LinearLayout layoutLoading;
    private int progress;
    private String ID_ADS = "ca-app-pub-3222539657172474/5177481580";
    private InterstitialAd mInterstitialAd;
    private FirebaseAnalystic firebaseAnalystic;
    private Disposable disposable;
    private boolean fullAdsLoaded = false;
    private boolean endTimeTick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        Glide.with(this).load(R.drawable.ic_bg_splash).into(imgBgSplash);
        firebaseAnalystic = FirebaseAnalystic.getInstance(this);
        if (AppUtils.isNetworkConnected(this)) {
            loadAds();
            startTimeLeft();
        } else {
            skip();
        }
    }

    public void loadAds() {
        mInterstitialAd = new InterstitialAd(this);
        if (BuildConfig.DEBUG) {
            mInterstitialAd.setAdUnitId(Constant.ID_INTER_TEST);
        } else {
            mInterstitialAd.setAdUnitId(ID_ADS);
        }
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        String[] ggTestDevices = getResources().getStringArray(R.array.google_test_device);
        for (String testDevice : ggTestDevices) {
            adRequestBuilder.addTestDevice(testDevice);
        }
        mInterstitialAd.loadAd(adRequestBuilder.build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                fullAdsLoaded = true;
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                fullAdsLoaded = false;
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {

            }

            @Override
            public void onAdClosed() {
                skip();
            }
        });
    }

    private boolean allowAdsShow;

    private void showAds() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded() && allowAdsShow) {
            mInterstitialAd.show();
        }
    }

    public void hideLoading() {
        layoutLoading.setVisibility(View.INVISIBLE);
        progress = 100;
    }

    private void countTimer() {
        if (seekbar.getProgress() < 100) {
            if (fullAdsLoaded) {
                showAds();
                hideLoading();
                if (disposable != null) {
                    disposable.dispose();
                }
            }
        } else {
            hideLoading();
            endTimeTick = true;
            if (disposable != null) {
                disposable.dispose();
            }
            if (allowAdsShow) {
                skip();
            }
        }
    }

    private void startTimeLeft() {
        disposable = Observable.interval(150, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(tick -> {
                    progress += 2;
                    seekbar.setProgress(progress);
                    countTimer();
                }, throwable -> skip());
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAnalystic.trackEvent(ManagerEvent.splashOpen());
        allowAdsShow = true;
        if (endTimeTick) {
            skip();
            return;
        }
        if (fullAdsLoaded) {
            showAds();
        }
    }

    @Override
    protected void onStop() {
        allowAdsShow = false;
        super.onStop();
    }

    @OnClick(R.id.btnSplash)
    public void onViewClicked() {
        firebaseAnalystic.trackEvent(ManagerEvent.splashStart());
    }

    public void skip() {
        if (disposable != null) {
            disposable.dispose();
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (progress >= 100) {
            skip();
        }
        super.onBackPressed();
    }
}
