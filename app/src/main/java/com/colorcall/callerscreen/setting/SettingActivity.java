package com.colorcall.callerscreen.setting;

import static com.colorcall.callerscreen.utils.ConstantAds.setting_banner_admob2;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.Event;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.application.ColorCallApplication;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.service.PhoneService;
import com.colorcall.callerscreen.utils.AppOpenManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.GoogleMobileAdsConsentManager;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.PermistionCallListener;
import com.colorcall.callerscreen.utils.PermistionFlashListener;
import com.colorcall.callerscreen.utils.PermistionUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends AppCompatActivity implements PermistionFlashListener, PermistionCallListener , AppOpenManager.AppOpenManagerObserver {
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
    @BindView(R.id.layoutUMP)
    RelativeLayout layoutUmp;
    @BindView(R.id.fl_adplaceholder)
    FrameLayout frameLayout;
    private LinearLayout adView;
    private NativeAd nativeAd;
    private Analystic analystic;
    private boolean isFlashState, isCallState;
    private boolean isResultDenyPermission, isResultDenyCallPermission;
    private AppOpenManager appOpenManager;
    private boolean isRequestPermission = false;
    public GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        AppUtils.changeStatusBarColor(this, R.color.colorHeaderMain);

        //AppUtils.showFullHeader(this, layoutHead);
        analystic = Analystic.getInstance(this);
        swStateApp.setChecked(HawkHelper.isEnableColorCall());
        swFlash.setChecked(HawkHelper.isEnableFlash());
        loadAds();
        listener();
        analystic.trackEvent(ManagerEvent.settingOpen());
        appOpenManager = ((ColorCallApplication) getApplication()).getAppOpenManager();
        googleMobileAdsConsentManager =
                GoogleMobileAdsConsentManager.getInstance(getApplicationContext());
        if (googleMobileAdsConsentManager.isPrivacyOptionsRequired()){
            layoutUmp.setVisibility(View.VISIBLE);
        }
        ///appOpenManager.registerObserver(this);
    }

    public void loadAds() {
        String idGG;
        String ID_ADS_GG = "ca-app-pub-3222539657172474/5477219704";
        if (BuildConfig.DEBUG) {
            idGG = Constant.ID_NATIVE_TEST;
        } else {
            idGG = setting_banner_admob2;
        }
        AdLoader.Builder builder = new AdLoader.Builder(this, idGG)
                .forNativeAd(nativeAd -> {
                    boolean isDestroyed = isDestroyed();
                    if (isDestroyed || isFinishing() || isChangingConfigurations()) {
                        nativeAd.destroy();
                        return;
                    }
                    // otherwise you will have a memory leak.
                    if (SettingActivity.this.nativeAd != null) {
                        SettingActivity.this.nativeAd.destroy();
                    }
                    SettingActivity.this.nativeAd = nativeAd;
                    NativeAdView adView =
                            (NativeAdView) getLayoutInflater().inflate(R.layout.ad_unified, null);
                    AppUtils.populateNativeAdView(nativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        frameLayout.setVisibility(View.GONE);
                    }
                });
        AdLoader adLoader = builder.build();
        adLoader.loadAd(new AdRequest.Builder().build());
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
        layoutUmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMobileAdsConsentManager.showPrivacyOptionsForm(
                        SettingActivity.this,
                        formError -> {
                            if (formError != null) {
                                Toast.makeText(SettingActivity.this, formError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @OnClick({R.id.btnBack, R.id.layoutShareApp, R.id.layoutPolicy, R.id.layoutCheckUpdate, R.id.layoutRateApp, R.id.btnAds})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                analystic.trackEvent(ManagerEvent.settingBackClick());
                finish();
                return;
            case R.id.layoutCheckUpdate:
                analystic.trackEvent(ManagerEvent.settingCheckUpdateClick());
                Intent intentRate = new Intent("android.intent.action.VIEW");
                StringBuilder sb = new StringBuilder();
                sb.append(Constant.PLAY_STORE_LINK);
                sb.append(getPackageName());
                intentRate.setData(Uri.parse(sb.toString()));
                startActivity(intentRate);
                return;
            case R.id.layoutPolicy:
                analystic.trackEvent(ManagerEvent.settingPolicyClick());
                openWebPage(Constant.POLICY_URL);
                return;
            case R.id.layoutRateApp:
                rateApp();
                return;
            case R.id.layoutShareApp:
                analystic.trackEvent(ManagerEvent.settingShareAppClick());
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
                analystic.trackEvent(ManagerEvent.settingAdsClick());
                return;
            default:
        }
    }

    private void rateApp() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public void openWebPage(String url) {
        try {
            Intent intentUpdate = new Intent(Intent.ACTION_VIEW);
            intentUpdate.setData(Uri.parse(url));
            startActivity(intentUpdate);
        } catch (ActivityNotFoundException anfe) {
            anfe.printStackTrace();
        }
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
                if (AppUtils.checkDrawOverlayApp2(this)) {
                    analystic.trackEvent(new Event("Permission_Dialog_DrawOver_Setting_Granted",new Bundle()));
                    if (!AppUtils.checkNotificationAccessSettings(this)) {
                        resetStateCall();
                        isRequestPermission = true;
                        AppUtils.showNotificationAccess(this);
                    }
                } else {
                    resetStateCall();
                    AppUtils.showDrawOverlayApp(this);
                }
            } else {
                resetStateCall();
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
            if (AppUtils.checkDrawOverlayApp2(this)) {
                analystic.trackEvent(new Event("Per_Dlg_DrawOver_Setting_Result_Granted",new Bundle()));
                if (!AppUtils.checkNotificationAccessSettings(this)) {
                    isCallState = true;
                    isRequestPermission = true;
                    resetStateCall();
                    AppUtils.showNotificationAccess(this);
                }
            }
        } else if (requestCode == Constant.REQUEST_NOTIFICATION_ACCESS) {
            if (AppUtils.checkNotificationAccessSettings(this)) {
                isCallState = true;
                swStateApp.setChecked(true);
                new Handler().postDelayed(() -> isRequestPermission = false,500);
                analystic.trackEvent(new Event("Per_Dlg_Setting_Use_App_Granted",new Bundle()));
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
        if (isCallState){
            PhoneService.startService(this);
        }else {
            PhoneService.stopService(this);
        }
        HawkHelper.setStateColorCall(isCallState);
    }
    @Override
    protected void onStart() {
        super.onStart();
        appOpenManager.registerObserver(this);
        Log.e("TAN", "onStart: setting"+appOpenManager);


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        appOpenManager.unregisterObserver();
    }
    @Override
    public void lifecycleStart(@NonNull AppOpenAd appOpenAd, @NonNull AppOpenManager appOpenManager) {
        Log.e("TAN", "lifecycleStart:setting "+isRequestPermission);
       /* if (hasActive() &&  !isRequestPermission && PermistionUtils.checkHasPermissionCall(this)) {
            appOpenAd.show(this);
        }*/
    }

    @Override
    public void lifecycleShowAd() {

    }

    @Override
    public void lifecycleStop() {

    }
    private boolean hasActive() {
        return !isFinishing() && !isDestroyed();
    }
}
