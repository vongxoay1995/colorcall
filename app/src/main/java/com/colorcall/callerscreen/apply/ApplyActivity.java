package com.colorcall.callerscreen.apply;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.FirebaseAnalystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.custom.CustomVideoView;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.listener.DialogDeleteListener;
import com.colorcall.callerscreen.model.Background;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.Manifest.permission.ANSWER_PHONE_CALLS;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ApplyActivity extends AppCompatActivity implements DialogDeleteListener {
    @BindView(R.id.img_background_call)
    ImageView imgBackgroundCall;
    @BindView(R.id.vdo_background_call)
    CustomVideoView vdoBackgroundCall;
    @BindView(R.id.imgDelete)
    ImageView imgDelete;
    @BindView(R.id.layoutApply)
    RelativeLayout layoutApply;
    @BindView(R.id.txtApply)
    TextView txtApply;
    @BindView(R.id.btnAccept)
    ImageView btnAccept;
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    private Background background;
    private Background backgroundCurrent;
    private String ID_ADS = "ca-app-pub-3222539657172474/5724276494";
    private InterstitialAd mInterstitialAd;
    private FirebaseAnalystic firebaseAnalystic;
    private boolean allowAdsShow, allowPermission;
    private boolean isClickedApply;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);
        ButterKnife.bind(this);
        AppUtils.showFullHeader(this, layoutHead);
        firebaseAnalystic = FirebaseAnalystic.getInstance(this);
        checkInforTheme();
        loadAds();
    }

    private void loadAds() {
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

            }

            @Override
            public void onAdFailedToLoad(int errorCode) {

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
                finish();
            }
        });
    }

    private void checkInforTheme() {
        if (getIntent().getBooleanExtra(Constant.SHOW_IMG_DELETE, false)) {
            imgDelete.setVisibility(View.VISIBLE);
        } else {
            imgDelete.setVisibility(View.GONE);
        }
        Gson gson = new Gson();
        background = gson.fromJson(getIntent().getStringExtra(Constant.BACKGROUND), Background.class);
        backgroundCurrent = HawkHelper.getBackgroundSelect();
        if (backgroundCurrent != null) {
            if (background.getPathItem().equals(backgroundCurrent.getPathItem())) {
                layoutApply.setEnabled(false);
                layoutApply.setBackground(getResources().getDrawable(R.drawable.bg_gray_apply));
                txtApply.setText(getString(R.string.applied));
                txtApply.setTextColor(Color.BLACK);
            } else {
                layoutApply.setEnabled(true);
                txtApply.setText(getString(R.string.applyContact));
                layoutApply.setBackground(getResources().getDrawable(R.drawable.bg_green_radius_60));
                txtApply.setTextColor(Color.WHITE);
            }
        }
        String uriPath = "android.resource://" + getPackageName() + background.getPathItem();
        if (background.getType() == Constant.TYPE_VIDEO) {
            imgBackgroundCall.setVisibility(View.GONE);
            vdoBackgroundCall.setVisibility(View.VISIBLE);
            if (background.getPathItem().contains("storage")) {
                vdoBackgroundCall.setVideoPath(background.getPathItem());
            } else {
                vdoBackgroundCall.setVideoURI(Uri.parse(uriPath));
            }
            vdoBackgroundCall.setOnPreparedListener(mediaPlayer -> mediaPlayer.setLooping(true));
            vdoBackgroundCall.start();
        } else {
            imgBackgroundCall.setVisibility(View.VISIBLE);
            if (background.getPathItem().contains("storage")) {
                Glide.with(getApplicationContext())
                        .load(background.getPathItem())
                        .apply(RequestOptions.placeholderOf(R.drawable.bg_gradient_green))
                        .into(imgBackgroundCall);
            } else {

            }
            vdoBackgroundCall.setVisibility(View.GONE);
        }
    }

    public void deleteTheme(Background background) {
        DataManager.query().getBackgroundDao().delete(background);
    }

    @Override
    protected void onResume() {
        firebaseAnalystic.trackEvent(ManagerEvent.applyOpen());
        vdoBackgroundCall.start();
        startAnimation();
        allowAdsShow = true;
        showAds();
        super.onResume();
    }

    @OnClick({R.id.btnBack, R.id.imgDelete, R.id.layoutApply, R.id.btnAds})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                firebaseAnalystic.trackEvent(ManagerEvent.applyBackClick());
                finish();
                break;
            case R.id.layoutApply:
                firebaseAnalystic.trackEvent(ManagerEvent.applyApplyClick());
                isClickedApply = true;
                checkPermission();
                break;
            case R.id.imgDelete:
                firebaseAnalystic.trackEvent(ManagerEvent.applyBinClick());
                AppUtils.showDialogDelete(this, this);
                break;
            case R.id.btnAds:
                firebaseAnalystic.trackEvent(ManagerEvent.applyAdsClick());
                AppUtils.showDialogDelete(this, this);
                break;
        }
    }

    @Override
    public void onDelete() {
        deleteTheme(background);
        Intent intent = new Intent();
        intent.putExtra(Constant.IS_DELETE_BG, true);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void checkPermission() {
        String[] permistion;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            permistion = new String[]{
                    READ_PHONE_STATE,
                    CALL_PHONE,
                    READ_CONTACTS
            };
        } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P){
            permistion = new String[]{
                    ANSWER_PHONE_CALLS,
                    READ_PHONE_STATE,
                    CALL_PHONE,
                    READ_CONTACTS
            };
        }else {
            permistion = new String[]{
                    ANSWER_PHONE_CALLS,
                    READ_PHONE_STATE,
                    CALL_PHONE,
                    READ_CONTACTS
            };
        }

        if (!AppUtils.checkPermission(this, permistion)) {
            ActivityCompat.requestPermissions(this, permistion, Constant.PERMISSION_REQUEST_CODE_CALL_PHONE);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!AppUtils.canDrawOverlays(this)) {
                    AppUtils.showDrawOverlayPermissionDialog(this);
                } else if (!AppUtils.checkNotificationAccessSettings(this)) {
                    AppUtils.showNotificationAccess(this);
                } else {
                    allowPermission = true;
                    applyBgCall();
                }
            } else {
                allowPermission = true;
                applyBgCall();
            }
        }
    }

    private void applyBgCall() {
        ProgressDialog dialog = ProgressDialog.show(this, "",
                getString(R.string.applying), true);
        dialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                showAds();
                HawkHelper.setBackgroundSelect(background);
                Toast.makeText(getBaseContext(), getString(R.string.apply_done), Toast.LENGTH_SHORT).show();
                layoutApply.setEnabled(false);
                layoutApply.setBackground(getResources().getDrawable(R.drawable.bg_gray_apply));
                txtApply.setText(getString(R.string.applied));
                txtApply.setTextColor(Color.BLACK);
            }
        }, 2000);

    }

    public void showAds() {
        if (isClickedApply
                && mInterstitialAd != null
                && mInterstitialAd.isLoaded()
                && allowAdsShow && allowPermission) {
            mInterstitialAd.show();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constant.PERMISSION_REQUEST_CODE_CALL_PHONE && grantResults.length > 0 && AppUtils.checkPermissionGrand(grantResults)) {
            if (AppUtils.canDrawOverlays(this)) {
                if (!AppUtils.checkNotificationAccessSettings(this)) {
                    AppUtils.showNotificationAccess(this);
                }
            } else {
                AppUtils.checkDrawOverlayApp(this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_OVERLAY) {
            if (AppUtils.canDrawOverlays(this)) {
                if (!AppUtils.checkNotificationAccessSettings(this)) {
                    AppUtils.showNotificationAccess(this);
                }
            }
        }
    }

    public void startAnimation() {
        Animation anim8 = AnimationUtils.loadAnimation(this, R.anim.anm_accept_call);
        btnAccept.startAnimation(anim8);
    }

    @Override
    protected void onStop() {
        allowAdsShow = false;
        super.onStop();
    }
}
