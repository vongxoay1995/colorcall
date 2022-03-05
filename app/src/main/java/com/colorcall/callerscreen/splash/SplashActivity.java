package com.colorcall.callerscreen.splash;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.main.MainActivity;
import com.colorcall.callerscreen.update.UpdateManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.facebook.ads.NativeAdLayout;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.play.core.install.model.AppUpdateType;

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
    RelativeLayout layoutLoading;
    @BindView(R.id.layoutSeekbar)
    LinearLayout layoutSeekbar;
    @BindView(R.id.layout_skip)
    LinearLayout layoutSkip;
    @BindView(R.id.btnStart)
    TextView btnStart;
    private int progress;
    private String ID_ADS = "ca-app-pub-3222539657172474/5177481580";
    private String ID_FB = "1205962693239181_1286419175193532";
    private String ID_FB_TEST = "YOUR_PLACEMENT_ID";
    private InterstitialAd mInterstitialAd;
    private Analystic analystic;
    private Disposable disposable;
    private boolean fullAdsLoaded = false;
    private boolean loadFailed = false;
    private boolean loadFailedFB = false;
    private boolean endTimeTick;
    private String idInter;
    private UpdateManager mUpdateManager;
    @BindView(R.id.native_ad_container)
    NativeAdLayout nativeAdLayout;
    private com.facebook.ads.NativeAd nativeAdFB;
    private LinearLayout adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        Glide.with(getApplicationContext())
                .load(R.drawable.ic_bg_splash)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .thumbnail(0.1f)
                .into(imgBgSplash);
        mUpdateManager = UpdateManager.Builder(this);
        analystic = Analystic.getInstance(this);
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
        checkShowAds();
    }

    public void callFlexibleUpdate() {
        // Start a Flexible Update
        mUpdateManager.mode(AppUpdateType.FLEXIBLE).start();
    }

    public void checkShowAds() {
        if (AppUtils.isNetworkConnected(this)) {
            loadAds();
            startTimeLeft();
        } else {
            skip();
        }
    }

    public void loadAds() {
        if (BuildConfig.DEBUG) {
            idInter = Constant.ID_INTER_TEST;
        } else {
            idInter = ID_ADS;
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
                                skip();
                                // Called when fullscreen content is dismissed.
                                Log.e("TAG", "The ad was dismissed.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                skip();
                                Log.d("TAG", "The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                mInterstitialAd = null;
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

    private boolean allowAdsShow;

    private void showAds() {
        if (mInterstitialAd != null && allowAdsShow) {
            mInterstitialAd.show(this);
        }
    }

    public void hideLoading() {
        layoutLoading.setVisibility(View.INVISIBLE);
        progress = 100;
    }

    public void hideLoadingSeekbar() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutSeekbar.setVisibility(View.INVISIBLE);
    }

    private void countTimer() {
        Log.e("TAN", "countTimer: " + seekbar.getProgress());
        if (seekbar.getProgress() < 100) {
            if (fullAdsLoaded) {
                showAds();
                hideLoading();
                if (disposable != null) {
                    disposable.dispose();
                }
            } /*else if (loadFailed) {
                if (loadFailedFB) {
                    hideLoading();
                    endTimeTick = true;
                    if (disposable != null) {
                        disposable.dispose();
                    }
                    if (allowAdsShow) {
                        skip();
                    }
                } else if (nativeAdFB.isAdLoaded()) {
                    inflateAd(nativeAdFB);
                    hideLoadingSeekbar();
                    if (disposable != null) {
                        disposable.dispose();
                    }
                }
            }*/
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
        analystic.trackEvent(ManagerEvent.splashOpen());
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
    protected void onPause() {
        super.onPause();
        allowAdsShow = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        allowAdsShow = false;
    }

    @OnClick({R.id.btnStart, R.id.layout_skip})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnStart:
            case R.id.layout_skip:
                skip();
                break;
        }
        analystic.trackEvent(ManagerEvent.splashStart());

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
