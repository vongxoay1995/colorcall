package com.colorcall.callerscreen.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.FirebaseAnalystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.PermistionCallListener;
import com.colorcall.callerscreen.utils.PermistionFlashListener;
import com.colorcall.callerscreen.utils.PermistionUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends AppCompatActivity implements PermistionFlashListener, PermistionCallListener {
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.btnCheckUpdate)
    ImageView btnCheckUpdate;
    @BindView(R.id.btnPolicy)
    ImageView btnPolicy;
    @BindView(R.id.btnShareApp)
    ImageView btnShareApp;
    @BindView(R.id.btnVip)
    ImageView btnVip;
    @BindView(R.id.layoutBottom)
    LinearLayout layoutBottom;
    @BindView(R.id.layoutCheckUpdate)
    RelativeLayout layoutCheckUpdate;
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    @BindView(R.id.layoutPolicy)
    RelativeLayout layoutPolicy;
    @BindView(R.id.layoutShareApp)
    RelativeLayout layoutShareApp;
    @BindView(R.id.layoutVip)
    RelativeLayout layoutVip;
    @BindView(R.id.swStateApp)
    SwitchCompat swStateApp;
    @BindView(R.id.swFlash)
    SwitchCompat swFlash;
    @BindView(R.id.layoutFlash)
    RelativeLayout layoutFlash;
    private UnifiedNativeAd nativeAd;
    private FirebaseAnalystic firebaseAnalystic;
    private boolean isFlashState, isCallState;
    private boolean isResultDenyPermission, isResultDenyCallPermission;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        AppUtils.showFullHeader(this, layoutHead);
        firebaseAnalystic = FirebaseAnalystic.getInstance(this);
        swStateApp.setChecked(HawkHelper.isEnableColorCall());
        swFlash.setChecked(HawkHelper.isEnableFlash());
        loadAds();
        listener();
    }

    public void loadAds() {
        String idGG;
        String ID_ADS_GG = "ca-app-pub-3222539657172474/5477219704";
        if (BuildConfig.DEBUG) {
            idGG = Constant.ID_NATIVE_TEST;
        } else {
            idGG = ID_ADS_GG;
        }
        AdLoader adLoader = new AdLoader.Builder(this, idGG)
                .forUnifiedNativeAd(unifiedNativeAd -> {
                    if (nativeAd != null) {
                        nativeAd.destroy();
                    }
                    nativeAd = unifiedNativeAd;
                    FrameLayout frameLayout =
                            findViewById(R.id.fl_adplaceholder);
                    UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater()
                            .inflate(R.layout.ad_unified, null);
                    AppUtils.populateUnifiedNativeAdView(unifiedNativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // Handle the failure by logging, altering the UI, and so on.
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        String[] ggTestDevices = getResources().getStringArray(R.array.google_test_device);
        for (String testDevice : ggTestDevices) {
            adRequestBuilder.addTestDevice(testDevice);
        }
        adLoader.loadAd(adRequestBuilder.build());
    }

    @Override
    protected void onDestroy() {
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        super.onDestroy();
    }

    private void listener() {
        swStateApp.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isCallState = isChecked;
            if (!isResultDenyCallPermission) {
                PermistionUtils.checkPermissionCall(SettingActivity.this, this);
            } else {
                isResultDenyCallPermission = false;
            }
        });

        swFlash.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isFlashState = isChecked;
            if (!isResultDenyPermission) {
                PermistionUtils.checkPermissionFlash(SettingActivity.this, this);
            } else {
                isResultDenyPermission = false;
            }
        });
    }

    @OnClick({R.id.btnBack, R.id.layoutShareApp, R.id.layoutPolicy, R.id.layoutCheckUpdate, R.id.btnAds})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                firebaseAnalystic.trackEvent(ManagerEvent.settingBackClick());
                finish();
                return;
            case R.id.layoutCheckUpdate:
                firebaseAnalystic.trackEvent(ManagerEvent.settingCheckUpdateClick());
                Intent intentRate = new Intent("android.intent.action.VIEW");
                StringBuilder sb = new StringBuilder();
                sb.append(Constant.PLAY_STORE_LINK);
                sb.append(getPackageName());
                intentRate.setData(Uri.parse(sb.toString()));
                startActivity(intentRate);
                return;
            case R.id.layoutPolicy:
                firebaseAnalystic.trackEvent(ManagerEvent.settingPolicyClick());
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse(Constant.POLICY_URL)));
                return;
            case R.id.layoutShareApp:
                firebaseAnalystic.trackEvent(ManagerEvent.settingShareAppClick());
                Intent sendIntent = new Intent();
                sendIntent.setAction("android.intent.action.SEND");
                StringBuilder sb2 = new StringBuilder();
                sb2.append(Constant.PLAY_STORE_LINK);
                sb2.append(getPackageName());
                sendIntent.putExtra("android.intent.extra.TEXT", sb2.toString());
                sendIntent.setType(Constant.DATA_TYPE);
                startActivity(sendIntent);
                return;
            case R.id.btnAds:
                firebaseAnalystic.trackEvent(ManagerEvent.settingAdsClick());
                return;
            default:
                return;
        }
    }

    @Override
    protected void onResume() {
        firebaseAnalystic.trackEvent(ManagerEvent.settingOpen());
        super.onResume();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constant.PERMISSION_REQUEST_CODE_CAMERA) {
            if (grantResults.length > 0 && AppUtils.checkPermissionGrand(grantResults)) {
                HawkHelper.setFlash(isFlashState);
            } else {
                isResultDenyPermission = true;
                swFlash.setChecked(!isFlashState);
            }
        } else if (requestCode == Constant.PERMISSION_REQUEST_CODE_CALL_PHONE) {
            if (grantResults.length > 0 && AppUtils.checkPermissionGrand(grantResults)) {
                if (AppUtils.canDrawOverlays(this)) {
                    if (!AppUtils.checkNotificationAccessSettings(this)) {
                        resetStateCall();
                        AppUtils.showNotificationAccess(this);
                    }
                } else {
                    resetStateCall();
                    AppUtils.checkDrawOverlayApp(this);
                }
                /*if (!AppUtils.checkNotificationAccessSettings(this)) {
                    resetStateCall();
                    AppUtils.showNotificationAccess(this);
                }*/
            }
        }
    }

    public void resetStateCall() {
        isResultDenyCallPermission = true;
        swStateApp.setChecked(!isCallState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_OVERLAY) {
            if (AppUtils.canDrawOverlays(this)) {
                if (!AppUtils.checkNotificationAccessSettings(this)) {
                    isCallState = true;
                    resetStateCall();
                    AppUtils.showNotificationAccess(this);
                }
            }
        } else if (requestCode == Constant.REQUEST_NOTIFICATION_ACCESS) {
            if (AppUtils.checkNotificationAccessSettings(this)) {
                isCallState = true;
                swStateApp.setChecked(true);
                onHasCallPermistion();
            }
        }
    }

    @Override
    public void onHasFlashPermistion() {
        HawkHelper.setFlash(isFlashState);
    }

    @Override
    public void onHasCallPermistion() {
        HawkHelper.setStateColorCall(isCallState);
    }
}
