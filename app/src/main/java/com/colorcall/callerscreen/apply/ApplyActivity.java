package com.colorcall.callerscreen.apply;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.Event;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.contact.SelectContactActivity;
import com.colorcall.callerscreen.custom.TextureVideoView;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.listener.DialogDeleteListener;
import com.colorcall.callerscreen.model.SignApplyImage;
import com.colorcall.callerscreen.model.SignApplyMain;
import com.colorcall.callerscreen.model.SignApplyMyTheme;
import com.colorcall.callerscreen.model.SignApplyVideo;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.BannerAdsUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.PermissionContactListener;
import com.colorcall.callerscreen.utils.PermistionCallListener;
import com.colorcall.callerscreen.utils.PermistionUtils;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.colorcall.callerscreen.constan.Constant.PERMISSIONS_REQUEST_READ_CONTACTS;

public class ApplyActivity extends AppCompatActivity implements com.colorcall.callerscreen.utils.AdListener, DialogDeleteListener, PermistionCallListener, DownloadTask.Listener, PermissionContactListener {
    @BindView(R.id.img_background_call)
    ImageView imgBackgroundCall;
    @BindView(R.id.vdo_background_call)
    TextureVideoView vdoBackgroundCall;
    @BindView(R.id.imgDelete)
    ImageView imgDelete;
    @BindView(R.id.layoutApply)
    RelativeLayout layoutApply;
    @BindView(R.id.layoutFooter)
    RelativeLayout layoutFooter;
    @BindView(R.id.txtApply)
    TextView txtApply;
    @BindView(R.id.btnAccept)
    ImageView btnAccept;
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    @BindView(R.id.layoutHeader)
    LinearLayout layoutHeader;
    @BindView(R.id.layout_ads)
    RelativeLayout layoutAds;
    @BindView(R.id.layoutContact)
    RelativeLayout layoutContact;
    @BindView(R.id.profile_image)
    ImageView imgAvatar;
    @BindView(R.id.txtName)
    TextView txtName;
    @BindView(R.id.txtPhone)
    TextView txtPhone;
    TextView txtPercentDownloading;
    private String folderApp;
    private Background background;
    private InterstitialAd mInterstitialAd;
    private Analystic analystic;
    private boolean allowAdsShow;
    private String newPathItem;
    private boolean isDownloaded = false;
    private int position;
    private int fromScreen;
    private Dialog dialog;
    private BannerAdsUtils bannerAdsUtils;
    private int posRandom;
    private String idInter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);
        ButterKnife.bind(this);
        posRandom = getIntent().getIntExtra(Constant.POS_RANDOM, 0);
        position = getIntent().getIntExtra(Constant.ITEM_POSITION, -1);
        bannerAdsUtils = new BannerAdsUtils(this, layoutAds);
        analystic = Analystic.getInstance(this);
        folderApp = Constant.LINK_VIDEO_CACHE;
        checkInforTheme();
        fromScreen = getIntent().getIntExtra(Constant.FROM_SCREEN, -1);
        loadAdsBanner();
    }

    private void loadAdsBanner() {
        String ID_ADS_GG = "ca-app-pub-3222539657172474/9030792883";
        String idGG;
        if (BuildConfig.DEBUG) {
            idGG = Constant.ID_TEST_BANNER_ADMOD;
        } else {
            idGG = ID_ADS_GG;
        }
        bannerAdsUtils.setIdAds(idGG);
        bannerAdsUtils.setAdListener(this);
        bannerAdsUtils.showMediationBannerAds();
    }


    private void startDownloadBg(String url, String videoName) {
        AppUtils.createFolder(folderApp);
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_download);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        txtPercentDownloading = dialog.findViewById(R.id.tv_progress);
        DownloadTask downloadTask = new DownloadTask(this);
        downloadTask.setListener(this);
        newPathItem = folderApp + videoName;
        downloadTask.execute(url, newPathItem);
    }

    public boolean checkShowInter() {
        if (HawkHelper.isCanShowDiaLogRate() && !disableShowRate()) {
            return false;
        }
        return true;
    }

    private boolean disableShowRate() {
        int count = HawkHelper.getCoutShowRate();
        if (count <= 30) {
            return count != 2 && count != 7 && count != 12;
        } else {
            return (count - 30) % 30 != 0;
        }
    }

    private void loadAds() {
        String ID_ADS = "ca-app-pub-3222539657172474/5724276494";
        if (BuildConfig.DEBUG) {
            idInter = Constant.ID_INTER_TEST;
        } else {
            idInter = ID_ADS;
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, idInter, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                applyTheme();
                                Log.e("TAG", "The ad was dismissed.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
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
                        if (allowAdsShow) {
                            mInterstitialAd.show(ApplyActivity.this);
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAd = null;
                        applyTheme();
                    }
                });
    }

    private void initInfor() {
        String pathAvatar = Constant.avatarRandom[posRandom];
        String name = Constant.nameRandom[posRandom];
        String phone = Constant.phoneRandom[posRandom];
        Glide.with(getApplicationContext())
                .load("file:///android_asset/avatar/" + pathAvatar)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .thumbnail(0.1f)
                .into(imgAvatar);
        txtName.setText(name);
        txtPhone.setText(phone);
    }

    private void checkInforTheme() {
        initInfor();
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
            layoutContact.setVisibility(View.VISIBLE);
            imgBackgroundCall.setVisibility(View.VISIBLE);
            if (background.getPathItem().contains("default") && background.getPathItem().contains("thumbDefault")) {
                sPathThumb = "file:///android_asset/" + background.getPathItem();
            } else {
                sPathThumb = background.getPathItem();
            }
            Glide.with(getApplicationContext())
                    .load(sPathThumb)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .thumbnail(0.1f)
                    .into(imgBackgroundCall);
            vdoBackgroundCall.setVisibility(View.GONE);
        }
    }

    private void processVideo() {
        String sPath;
        String sPathThumb;
        String uriPath = "android.resource://" + getPackageName() + background.getPathItem();
        if (background.getPathItem().contains("default") && background.getPathItem().contains("thumbDefault")) {
            sPathThumb = "file:///android_asset/" + background.getPathThumb();
        } else {
            sPathThumb = background.getPathThumb();
        }
        Glide.with(getApplicationContext())
                .load(sPathThumb)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .thumbnail(0.1f)
                .into(imgBackgroundCall);
        if (background.getPathItem().contains("storage") || background.getPathItem().contains("/data/data") || background.getPathItem().contains("data/user/")) {
            sPath = background.getPathItem();
            if (!sPath.startsWith("http")) {
                isDownloaded = false;
                vdoBackgroundCall.setVideoURI(Uri.parse(sPath));
                playVideo();
                layoutFooter.setPadding(getResources().getDimensionPixelSize(R.dimen._15sdp), layoutFooter.getPaddingTop(), getResources().getDimensionPixelSize(R.dimen._15sdp), layoutFooter.getPaddingBottom());
                layoutContact.setVisibility(View.VISIBLE);
                txtApply.setText(getString(R.string.applyContact));
            } else {
                isDownloaded = true;
                layoutContact.setVisibility(View.GONE);
                layoutFooter.setPadding(getResources().getDimensionPixelSize(R.dimen._45sdp), layoutFooter.getPaddingTop(), getResources().getDimensionPixelSize(R.dimen._45sdp), layoutFooter.getPaddingBottom());
                txtApply.setText(getString(R.string.download));
            }
        } else {
            layoutFooter.setPadding(getResources().getDimensionPixelSize(R.dimen._15sdp), layoutFooter.getPaddingTop(), getResources().getDimensionPixelSize(R.dimen._15sdp), layoutFooter.getPaddingBottom());
            layoutContact.setVisibility(View.VISIBLE);
            vdoBackgroundCall.setVideoURI(Uri.parse(uriPath));
            playVideo();
        }
    }

    public void deleteTheme(Background background) {
        if (HawkHelper.getBackgroundSelect().getPathThumb().equals(background.getPathThumb())) {
            Background bg = new Background(null, 0, "thumbDefault/default1.webp", "/raw/default1", false, "default1");
            HawkHelper.setBackgroundSelect(bg);
            SignApplyVideo signApplyVideo = new SignApplyVideo(Constant.APPLY_ITEM_DEFAULT);
            EventBus.getDefault().postSticky(signApplyVideo);
        }
        DataManager.query().getBackgroundDao().delete(background);
    }

    private void playVideo() {
        imgBackgroundCall.setVisibility(View.GONE);
        vdoBackgroundCall.setVisibility(View.VISIBLE);
        vdoBackgroundCall.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0.0f, 0.0f);
        });
        vdoBackgroundCall.setOnErrorListener((mp, what, extra) -> {
            analystic.trackEvent(ManagerEvent.applyVideoViewError(what, extra));
            return false;
        });
        vdoBackgroundCall.start();
    }

    @Override
    protected void onResume() {
        analystic.trackEvent(ManagerEvent.applyOpen());
        vdoBackgroundCall.start();
        startAnimation();
        allowAdsShow = true;
        showAds();
        super.onResume();
    }

    @OnClick({R.id.btnBack, R.id.imgDelete, R.id.layoutApply, R.id.layoutContact, R.id.btnAds})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                analystic.trackEvent(ManagerEvent.applyBackClick());
                finish();
                break;
            case R.id.layoutApply:
                analystic.trackEvent(ManagerEvent.applyApplyClick());
                if (isDownloaded) {
                    startDownloadBg(background.getPathItem(), background.getName());
                } else {
                    PermistionUtils.checkPermissionCall(this, this);
                }
                break;
            case R.id.imgDelete:
                analystic.trackEvent(ManagerEvent.applyBinClick());
                AppUtils.showDialogDelete(this, this);
                break;
            case R.id.btnAds:
                analystic.trackEvent(ManagerEvent.applyAdsClick());
                AppUtils.showDialogDelete(this, this);
                break;
            case R.id.layoutContact:
                analystic.trackEvent(ManagerEvent.applyContactClick());
                PermistionUtils.requestContactPermission(this, this);
                break;
        }
    }

    @Override
    public void onDelete() {
        deleteTheme(background);
        SignApplyMyTheme signApplyMyTheme = new SignApplyMyTheme(Constant.INTENT_DELETE_THEME);
        EventBus.getDefault().postSticky(signApplyMyTheme);
        finish();
    }

    ProgressDialog mProgressDialog;

    private void applyBgCall() {
        int countRate = HawkHelper.getCoutShowRate();
        countRate++;
        HawkHelper.setCountRate(countRate);
        if (checkShowInter()) {
            mProgressDialog = ProgressDialog.show(this, "",
                    getString(R.string.applying), true);
            mProgressDialog.show();
            loadAds();
        } else {
            applyTheme();
        }
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
        intent.setAction(Constant.INTENT_APPLY_THEME);
        intent.putExtra(Constant.ITEM_POSITION, position);
        setResult(RESULT_OK, intent);
        SignApplyVideo signApplyVideo = new SignApplyVideo(Constant.INTENT_APPLY_THEME);
        SignApplyMyTheme signApplyMyTheme = new SignApplyMyTheme(Constant.INTENT_APPLY_THEME);
        SignApplyImage signApplyImage = new SignApplyImage(Constant.INTENT_APPLY_THEME);
        Bundle bundle = new Bundle();
        bundle.putString("name", background.getName());
        bundle.putInt("position", position);
        EventBus.getDefault().postSticky(new SignApplyMain());
        analystic.trackEvent(new Event("APPLY_ITEM_INFOR", bundle));
        switch (fromScreen) {
            case Constant.VIDEO_FRAG_MENT:
                analystic.trackEvent(ManagerEvent.applyVideoThemeSelected(background.getName()));
                EventBus.getDefault().postSticky(signApplyVideo);
                break;
            case Constant.IMAGES_FRAG_MENT:
                analystic.trackEvent(ManagerEvent.applyImageThemeSelected(background.getName()));
                EventBus.getDefault().postSticky(signApplyImage);
                break;
            case Constant.MYTHEME_FRAG_MENT:
                EventBus.getDefault().postSticky(signApplyMyTheme);
                break;
        }
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        finish();
    }

    public void showAds() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(ApplyActivity.this);
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
        } else if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onHasContactPermistion();
            } else {
                Toast.makeText(this, "You have disabled a contacts permission", Toast.LENGTH_LONG).show();
            }
            return;
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
        super.onBackPressed();
    }

    @Override
    public void onPreExecute() {
        dialog.show();
    }

    @Override
    public void onProgressUpdate(int value) {
        txtPercentDownloading.setText(value + "%");
    }

    @Override
    public void onPostExecute(String result) {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {

        }
        if (result != null) {
            Toast.makeText(this, getString(R.string.down_err), Toast.LENGTH_LONG).show();
        } else {
            ArrayList<Background> arr = HawkHelper.getListBackground();
            background.setPathItem(newPathItem);
            arr.get(background.getPosition()).setPathItem(newPathItem);
            HawkHelper.setListBackground(arr);
            vdoBackgroundCall.setVideoURI(Uri.parse(newPathItem));
            txtApply.setText(getString(R.string.applyContact));
            layoutFooter.setPadding(getResources().getDimensionPixelSize(R.dimen._15sdp), layoutFooter.getPaddingTop(), getResources().getDimensionPixelSize(R.dimen._15sdp), layoutFooter.getPaddingBottom());
            layoutContact.setVisibility(View.VISIBLE);
            isDownloaded = false;
            playVideo();
            SignApplyVideo signApplyVideo = new SignApplyVideo(Constant.INTENT_DOWNLOAD_COMPLETE_THEME);
            EventBus.getDefault().postSticky(signApplyVideo);
            Toast.makeText(this, getString(R.string.downloadSuccess), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAdloaded() {

    }

    @Override
    public void onAdFailed() {
        layoutHeader.setVisibility(View.GONE);
        setTranslucent();
    }

    public void setTranslucent() {
        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            AppUtils.showFullHeader(this, layoutHead);
        }
    }

    @Override
    public void onHasContactPermistion() {
        Intent intent = new Intent(this, SelectContactActivity.class);
        Gson gson = new Gson();
        intent.putExtra(Constant.BACKGROUND, gson.toJson(background));
        startActivity(intent);
    }
}
