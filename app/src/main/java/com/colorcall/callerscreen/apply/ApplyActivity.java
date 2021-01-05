package com.colorcall.callerscreen.apply;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.FirebaseAnalystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.custom.CustomVideoView;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.listener.DialogDeleteListener;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.PermistionCallListener;
import com.colorcall.callerscreen.utils.PermistionUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.google.gson.Gson;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ApplyActivity extends AppCompatActivity implements DialogDeleteListener, PermistionCallListener, DownloadTask.Listener {
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
    private String dataPath, folderApp;
    private Background background;
    private PublisherInterstitialAd mInterstitialAd;
    private FirebaseAnalystic firebaseAnalystic;
    private boolean allowAdsShow;
    LocalBroadcastManager localBroadcastManager;
    ProgressDialog mProgressDialogDownload;
    private String newPathItem;
    private boolean isDownloaded=false;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);
        ButterKnife.bind(this);
        localBroadcastManager = LocalBroadcastManager
                .getInstance(this);
        AppUtils.showFullHeader(this, layoutHead);
        firebaseAnalystic = FirebaseAnalystic.getInstance(this);
        folderApp = Constant.LINK_VIDEO_CACHE;
        checkInforTheme();
    }


    private void startDownloadBg(String url, String videoName) {
        AppUtils.createFolder(folderApp);
        mProgressDialogDownload = new ProgressDialog(this);
        mProgressDialogDownload.setMessage(getString(R.string.download));
        mProgressDialogDownload.setIndeterminate(true);
        mProgressDialogDownload.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialogDownload.setCancelable(true);
        DownloadTask downloadTask = new DownloadTask(this);
        downloadTask.setListener(this);
        newPathItem = folderApp + videoName;
        downloadTask.execute(url,newPathItem);
        mProgressDialogDownload.setOnCancelListener(dialog -> {
            downloadTask.cancel(true);
        });
    }

    private void loadAds() {
        mInterstitialAd = new PublisherInterstitialAd(this);
        String ID_ADS = "ca-app-pub-3222539657172474/5724276494";
        if (BuildConfig.DEBUG) {
            mInterstitialAd.setAdUnitId(Constant.ID_INTER_TEST);
        } else {
            mInterstitialAd.setAdUnitId(ID_ADS);
        }
        PublisherAdRequest.Builder adRequestBuilder = new PublisherAdRequest.Builder();
        String[] ggTestDevices = getResources().getStringArray(R.array.google_test_device);
        for (String testDevice : ggTestDevices) {
            adRequestBuilder.addTestDevice(testDevice);
        }
        mInterstitialAd.loadAd(adRequestBuilder.build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (allowAdsShow) {
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    }
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                applyTheme();
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
                applyTheme();
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
        Background backgroundCurrent = HawkHelper.getBackgroundSelect();
        if (backgroundCurrent != null) {
            if (background.getPathItem().equals(backgroundCurrent.getPathItem()) && HawkHelper.isEnableColorCall()) {
                layoutApply.setEnabled(false);
                layoutApply.setBackground(getResources().getDrawable(R.drawable.bg_gray_apply));
                txtApply.setText(getString(R.string.applied));
                txtApply.setTextColor(Color.BLACK);
            } else {
                layoutApply.setEnabled(true);
                layoutApply.setBackground(getResources().getDrawable(R.drawable.bg_green_radius_60));
                txtApply.setTextColor(Color.WHITE);
            }
        }

        String sPathThumb;
        if (background.getType() == Constant.TYPE_VIDEO) {
            processVideo();
        } else {
            imgBackgroundCall.setVisibility(View.VISIBLE);
            if (background.getPathItem().contains("default")) {
                sPathThumb = "file:///android_asset/" + background.getPathThumb();
            } else {
                sPathThumb = background.getPathThumb();
            }
            Glide.with(getApplicationContext())
                    .load(sPathThumb)
                    .apply(RequestOptions.placeholderOf(R.drawable.bg_gradient_green))
                    .into(imgBackgroundCall);
            vdoBackgroundCall.setVisibility(View.GONE);
        }
    }

    private void processVideo() {
        String sPath;
        String sPathThumb;
        String uriPath = "android.resource://" + getPackageName() + background.getPathItem();
        if (background.getPathItem().contains("default")) {
            sPathThumb = "file:///android_asset/" + background.getPathThumb();
        } else {
            sPathThumb = background.getPathThumb();
        }
        Glide.with(getApplicationContext())
                .load(sPathThumb)
                .thumbnail(0.001f)
                .apply(RequestOptions.placeholderOf(R.drawable.bg_gradient_green).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).skipMemoryCache(true))
                .into(imgBackgroundCall);
        if (background.getPathItem().contains("storage") ||background.getPathItem().contains("/data/data")|| background.getPathItem().contains("data/user/")) {
            sPath = background.getPathItem();
            if (!sPath.startsWith("http")) {
                isDownloaded = false;
                vdoBackgroundCall.setVideoURI(Uri.parse(sPath));
                playVideo();
                txtApply.setText(getString(R.string.applyContact));
            }else{
                isDownloaded = true;
                txtApply.setText(getString(R.string.download));
            }
        } else {
            vdoBackgroundCall.setVideoURI(Uri.parse(uriPath));
            playVideo();
        }
    }

    public void deleteTheme(Background background) {
        DataManager.query().getBackgroundDao().delete(background);
        ArrayList<Background> arr =  HawkHelper.getListBackground();
        arr.remove(background.getPosition());
        HawkHelper.setListBackground(arr);
        ArrayList<Background> bbb = HawkHelper.getListBackground();
        for (int i=0;i<bbb.size();i++){
            Log.e("TAN", "deleteTheme: index "+i+"--"+bbb.get(i));
        }
    }

    private void playVideo() {
        imgBackgroundCall.setVisibility(View.GONE);
        vdoBackgroundCall.setVisibility(View.VISIBLE);
        vdoBackgroundCall.setOnPreparedListener(mediaPlayer -> mediaPlayer.setLooping(true));
        vdoBackgroundCall.setOnErrorListener((mp, what, extra) -> {
            firebaseAnalystic.trackEvent(ManagerEvent.applyVideoViewError(what, extra));
            return false;
        });
        vdoBackgroundCall.start();
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
                if(isDownloaded){
                    startDownloadBg(background.getPathItem(),background.getName());
                }else{
                    PermistionUtils.checkPermissionCall(this, this);
                }
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
        localBroadcastManager.sendBroadcast(new Intent(Constant.INTENT_DELETE_THEME));
        finish();
    }

    ProgressDialog mProgressDialog;

    private void applyBgCall() {
        mProgressDialog = ProgressDialog.show(this, "",
                getString(R.string.applying), true);
        mProgressDialog.show();
        loadAds();
    }

    public void applyTheme() {
        HawkHelper.setBackgroundSelect(background);
        HawkHelper.setStateColorCall(true);
        Toast.makeText(getBaseContext(), getString(R.string.apply_done), Toast.LENGTH_SHORT).show();
        layoutApply.setEnabled(false);
        layoutApply.setBackground(getResources().getDrawable(R.drawable.bg_gray_apply));
        txtApply.setText(getString(R.string.applied));
        txtApply.setTextColor(Color.BLACK);
        Intent intent = new Intent();
        intent.putExtra(Constant.IS_UPDATE_LIST, true);
        setResult(RESULT_OK, intent);
        localBroadcastManager.sendBroadcast(new Intent(Constant.INTENT_APPLY_THEME));
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        finish();
    }

    public void showAds() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
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
          /*  if (!AppUtils.checkNotificationAccessSettings(this)) {
                AppUtils.showNotificationAccess(this);
            }*/
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
        } else if (requestCode == Constant.REQUEST_NOTIFICATION_ACCESS) {
            if (AppUtils.checkNotificationAccessSettings(this)) {
                applyBgCall();
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

    @Override
    protected void onStart() {
        super.onStart();
        allowAdsShow = true;
    }

    @Override
    public void onHasCallPermistion() {
        applyBgCall();
    }

    @Override
    public void onBackPressed() {
        if (mInterstitialAd == null || !mInterstitialAd.isLoading()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onPreExecute() {
        mProgressDialogDownload.show();
    }

    @Override
    public void onProgressUpdate(int value) {
        mProgressDialogDownload.setIndeterminate(false);
        mProgressDialogDownload.setMax(100);
        mProgressDialogDownload.setProgress(value);
    }

    @Override
    public void onPostExecute(String result) {
        mProgressDialogDownload.dismiss();
        if (result != null){
            Toast.makeText(this, "Download error: " + result, Toast.LENGTH_LONG).show();
        }else {
            ArrayList<Background> arr = HawkHelper.getListBackground();
            arr.get(background.getPosition()).setPathItem(newPathItem);
            HawkHelper.setListBackground(arr);
            vdoBackgroundCall.setVideoURI(Uri.parse(newPathItem));
            txtApply.setText(getString(R.string.applyContact));
            isDownloaded = false;
            playVideo();
            localBroadcastManager.sendBroadcast(new Intent(Constant.INTENT_DOWNLOAD_COMPLETE_THEME));
            Toast.makeText(this, getString(R.string.downloadSuccess), Toast.LENGTH_SHORT).show();
        }
    }
}
