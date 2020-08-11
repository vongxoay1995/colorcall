package com.colorcall.callerscreen.splash;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.FirebaseAnalystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.main.MainActivity;
import com.colorcall.callerscreen.utils.AppUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SplashActivity extends AppCompatActivity {

    @BindView(R.id.imgBgSplash)
    ImageView imgBgSplash;
    @BindView(R.id.progress)
    ProgressBar progress;
    private String ID_ADS = "ca-app-pub-3222539657172474/5177481580";
    private InterstitialAd mInterstitialAd;
    private FirebaseAnalystic firebaseAnalystic ;
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
        if(AppUtils.isNetworkConnected(this)){
            loadAds();
        }else {
           skip();
        }
    }
    public void loadAds(){
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(ID_ADS);
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        String[] ggTestDevices = getResources().getStringArray(R.array.google_test_device);
        for (String testDevice : ggTestDevices) {
            adRequestBuilder.addTestDevice(testDevice);
        }
        mInterstitialAd.loadAd(adRequestBuilder.build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                progress.setVisibility(View.GONE);
                showAds();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.e("TAN", "onAdFailedToLoad: ");
                progress.setVisibility(View.GONE);
                skip();
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
        if(mInterstitialAd!=null&&mInterstitialAd.isLoaded()&&allowAdsShow){
            mInterstitialAd.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        allowAdsShow = true;
        showAds();
        firebaseAnalystic.trackEvent(ManagerEvent.splashOpen());
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
    public void skip(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
