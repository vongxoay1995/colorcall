package com.colorcall.callerscreen.apply;

import static com.colorcall.callerscreen.constan.Constant.PERMISSIONS_REQUEST_READ_CONTACTS;
import static com.colorcall.callerscreen.utils.ConstantAds.apply_banner_admob2;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.Event;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.application.ColorCallApplication;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.contact.SelectContactActivity;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.database.DatabaseViewModel;
import com.colorcall.callerscreen.databinding.ActivityApplyBinding;
import com.colorcall.callerscreen.listener.DialogDeleteListener;
import com.colorcall.callerscreen.model.SignApplyImage;
import com.colorcall.callerscreen.model.SignApplyMyTheme;
import com.colorcall.callerscreen.model.SignApplyVideo;
import com.colorcall.callerscreen.service.PhoneService;
import com.colorcall.callerscreen.utils.AppOpenManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.BannerAdsUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.InterstitialApply;
import com.colorcall.callerscreen.utils.PermissionContactListener;
import com.colorcall.callerscreen.utils.PermistionCallListener;
import com.colorcall.callerscreen.utils.PermistionUtils;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;

public class ApplyActivity extends AppCompatActivity implements com.colorcall.callerscreen.utils.AdListener,
        DialogDeleteListener, PermistionCallListener, DownloadTask.Listener, PermissionContactListener, AppOpenManager.AppOpenManagerObserver {
    TextView txtPercentDownloading;
    private String folderApp;
    private Background background;
    private Analystic analystic;
    private String newPathItem;
    private boolean isDownloaded = false;
    private int position;
    private int fromScreen;
    private Dialog dialog;
    private BannerAdsUtils bannerAdsUtils;
    private int posRandom;
    private AppOpenManager appOpenManager;
    private InterstitialApply interstitialApply;
    private boolean isRequestPermission = false;
    private DatabaseViewModel databaseViewModel;
    private ActivityApplyBinding binding;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTranslucent();
        binding = ActivityApplyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseViewModel = new ViewModelProvider(this).get(DatabaseViewModel.class);
        appOpenManager = ((ColorCallApplication) getApplication()).getAppOpenManager();
        interstitialApply = InterstitialApply.getInstance();
        AppUtils.changeStatusBarColor(this, R.color.blackAlpha30);
        posRandom = getIntent().getIntExtra(Constant.POS_RANDOM, 0);
        position = getIntent().getIntExtra(Constant.ITEM_POSITION, -1);
        bannerAdsUtils = new BannerAdsUtils(this, binding.layoutAds);
        analystic = Analystic.getInstance(this);
        folderApp = Constant.LINK_VIDEO_CACHE;
        checkInforTheme();
        fromScreen = getIntent().getIntExtra(Constant.FROM_SCREEN, -1);
        loadAdsBanner();
        analystic.trackEvent(ManagerEvent.applyOpen());
        listener();
    }



    private void loadAdsBanner() {
        String ID_ADS_GG = "ca-app-pub-3134368447261649/7123602157";
        bannerAdsUtils.setIdAds(apply_banner_admob2);
        bannerAdsUtils.setAdListener(this);
        bannerAdsUtils.loadAds();
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
       /* if (HawkHelper.isCanShowDiaLogRate() && !disableShowRate()) {
            return false;
        }*/
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

    private void initInfor() {
        String pathAvatar = Constant.avatarRandom[posRandom];
        String name = Constant.nameRandom[posRandom];
        String phone = Constant.phoneRandom[posRandom];
        Glide.with(getApplicationContext())
                .load("file:///android_asset/avatar/" + pathAvatar)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .thumbnail(0.1f)
                .into(binding.profileImage);
        binding.txtName.setText(name);
        binding.txtPhone.setText(phone);
    }

    private void checkInforTheme() {
        initInfor();
        if (getIntent().getBooleanExtra(Constant.SHOW_IMG_DELETE, false)) {
            binding.imgDelete.setVisibility(View.VISIBLE);
        } else {
            binding.imgDelete.setVisibility(View.GONE);
        }
        Gson gson = new Gson();
        background = gson.fromJson(getIntent().getStringExtra(Constant.BACKGROUND), Background.class);
        Background backgroundCurrent = HawkHelper.getBackgroundSelect();
        if (backgroundCurrent != null) {
            if (background.getPathItem().equals(backgroundCurrent.getPathItem()) && HawkHelper.isEnableColorCall()) {
                binding.layoutApply.setEnabled(false);
                binding.layoutApply.setBackground(getResources().getDrawable(R.drawable.bg_gray_apply));
                binding.txtApply.setText(getString(R.string.applied));
                binding.txtApply.setTextColor(Color.BLACK);
            } else {
                binding.layoutApply.setEnabled(true);
                binding.layoutApply.setBackground(getResources().getDrawable(R.drawable.bg_green_radius_60));
                binding.txtApply.setTextColor(Color.WHITE);
            }
        }

        String sPathThumb;
        if (background.getType() == Constant.TYPE_VIDEO) {
            processVideo();
        } else {
            binding.layoutContact.setVisibility(View.VISIBLE);
            binding.imgBackgroundCall.setVisibility(View.VISIBLE);
            if (background.getPathItem().contains("default") && background.getPathItem().contains("thumbDefault")) {
                sPathThumb = "file:///android_asset/" + background.getPathItem();
            } else {
                sPathThumb = background.getPathItem();
            }
            Log.e("TAN", "checkInforTheme: "+sPathThumb);
            Glide.with(getApplicationContext())
                    .load(sPathThumb)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .thumbnail(0.1f)
                    .into(binding.imgBackgroundCall);
            binding.vdoBackgroundCall.setVisibility(View.GONE);
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
                .into(binding.imgBackgroundCall);
        if (background.getPathItem().contains("storage") || background.getPathItem().contains("/data/data") || background.getPathItem().contains("data/user/")) {
            sPath = background.getPathItem();
            Log.e("TAN", "processVideo: "+sPath);

            if (!sPath.startsWith("http")) {
                isDownloaded = false;
                binding.vdoBackgroundCall.setVideoURI(Uri.parse(sPath));
                playVideo();
                binding.layoutFooter.setPadding(getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._15sdp), binding.layoutFooter.getPaddingTop(), getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._15sdp), binding.layoutFooter.getPaddingBottom());
                binding.layoutContact.setVisibility(View.VISIBLE);
                binding.txtApply.setText(getString(R.string.applyContact));
            } else {
                isDownloaded = true;
                binding.layoutContact.setVisibility(View.GONE);
                binding.layoutFooter.setPadding(getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._45sdp), binding.layoutFooter.getPaddingTop(), getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._45sdp), binding.layoutFooter.getPaddingBottom());
                binding.txtApply.setText(getString(R.string.download));
            }
        } else {
            binding.layoutFooter.setPadding(getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._15sdp), binding.layoutFooter.getPaddingTop(), getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._15sdp), binding.layoutFooter.getPaddingBottom());
            binding.layoutContact.setVisibility(View.VISIBLE);
            binding.vdoBackgroundCall.setVideoURI(Uri.parse(uriPath));
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
        if (background.getType()==0) {
            deleteInternalFile(this, background.getPathThumb());
        }
        deleteFileByPath(this, background.getPathItem());
        Log.e("TAN", "deleteTheme: "+background.getPathItem()+"##"+background.getPathThumb());
        databaseViewModel.deleteBackground(background);

      //  DataManager.query().getBackgroundDao().delete(background);
    }
    public void deleteInternalFile(Context context, String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Log.e("TAN", "File deleted: " + filePath);
            } else {
                Log.e("TAN", "Failed to delete file: " + filePath);
            }
        } else {
            Log.e("TAN", "File not found: " + filePath);
        }
    }

    public void deleteFileByPath(Context context, String filePath) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri collection = MediaStore.Files.getContentUri("external");

        // Thiết lập điều kiện truy vấn với đường dẫn đầy đủ của tệp
        String selection = MediaStore.MediaColumns.DATA + "=?";
        String[] selectionArgs = new String[] { filePath };

        try (Cursor cursor = contentResolver.query(collection, null, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                    Uri fileUri = ContentUris.withAppendedId(collection, id);

                    // Xóa tệp theo đường dẫn
                    int rowsDeleted = contentResolver.delete(fileUri, null, null);
                    if (rowsDeleted > 0) {
                        Log.e("TAN", "File deleted: " + fileUri);
                    } else {
                        Log.e("TAN", "Failed to delete file: " + fileUri);
                    }
                } while (cursor.moveToNext());
            } else {
                Log.e("TAN", "No file found at path: " + filePath);
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void deleteFilesInColorCallImages(Context context,Uri collecttion,String path,String fileName) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri collection = collecttion;

        // Truy vấn tất cả các tệp trong thư mục "ColorCall/Images"
        String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=? AND " + MediaStore.MediaColumns.DISPLAY_NAME + "=?";
        String[] selectionArgs = new String[] { path, fileName };

        try (Cursor cursor = contentResolver.query(collection, null, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                    Uri fileUri = ContentUris.withAppendedId(collection, id);

                    // Xóa từng tệp
                    int rowsDeleted = contentResolver.delete(fileUri, null, null);
                    if (rowsDeleted > 0) {
                       Log.e("TAN", "deleteFilesInColorCallImages: "+fileUri);
                    } else {
                        Log.e("TAN", "Failed to delete file: " + fileUri);
                    }
                } while (cursor.moveToNext());
            } else {
                System.out.println("No files found in ColorCall/Images folder");
            }
        }
    }
    private void playVideo() {
        binding.imgBackgroundCall.setVisibility(View.GONE);
        binding.vdoBackgroundCall.setVisibility(View.VISIBLE);
        binding.vdoBackgroundCall.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0.0f, 0.0f);
        });
        binding.vdoBackgroundCall.setOnErrorListener((mp, what, extra) -> {
            analystic.trackEvent(ManagerEvent.applyVideoViewError(what, extra));
            return false;
        });
        binding.vdoBackgroundCall.start();
    }

    @Override
    protected void onResume() {
        binding.vdoBackgroundCall.start();
        startAnimation();
        super.onResume();
    }

    public void listener() {
        binding.btnBack.setOnClickListener(view1 -> {
            analystic.trackEvent(ManagerEvent.applyBackClick());
            finish();
        });
        binding.layoutApply.setOnClickListener(view1 -> {
            if (!AppUtils.allowViewClick())
                return;
            analystic.trackEvent(ManagerEvent.applyApplyClick());
            if (isDownloaded) {
                startDownloadBg(background.getPathItem(), background.getName());
            } else {
                PermistionUtils.checkPermissionCall(this, this);
            }
        });
        binding.imgDelete.setOnClickListener(view1 -> {
            analystic.trackEvent(ManagerEvent.applyBinClick());
            AppUtils.showDialogDelete(this, this);
        });
        binding.btnAds.setOnClickListener(view1 -> {
            analystic.trackEvent(ManagerEvent.applyAdsClick());
            AppUtils.showDialogDelete(this, this);
        });
        binding.layoutContact.setOnClickListener(view1 -> {
            analystic.trackEvent(ManagerEvent.applyContactClick());
            PermistionUtils.requestContactPermission(this, this);
        });
    }

    @Override
    public void onDelete() {
        deleteTheme(background);
        SignApplyMyTheme signApplyMyTheme = new SignApplyMyTheme(Constant.INTENT_DELETE_THEME);
        EventBus.getDefault().postSticky(signApplyMyTheme);
        finish();
    }

    private void applyBgCall() {
        int countRate = HawkHelper.getCoutShowRate();
        countRate++;
        HawkHelper.setCountRate(countRate);

       /* if (checkShowInter()) {
            InterstitialApply.getInstance().showInterstitialAds(this, this::applyTheme);
        } else {
            applyTheme();
        }*/
        InterstitialApply.getInstance().showInterstitialAds(this, this::applyTheme);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void applyTheme() {
        HawkHelper.setBackgroundSelect(background);
        PhoneService.startService(this);
        HawkHelper.setStateColorCall(true);
        Toast.makeText(getApplicationContext(), getString(R.string.apply_done), Toast.LENGTH_SHORT).show();
        binding.layoutApply.setEnabled(false);
        binding.layoutApply.setBackground(getResources().getDrawable(R.drawable.bg_gray_apply));
        binding.txtApply.setText(getString(R.string.applied));
        binding.txtApply.setTextColor(Color.BLACK);
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
        finish();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constant.PERMISSION_REQUEST_CODE_CALL_PHONE && grantResults.length > 0 && AppUtils.checkPermissionGrand(grantResults)) {
            // if (AppUtils.canDrawOverlays(this)) {
            if (AppUtils.checkDrawOverlayApp2(this)) {
                analystic.trackEvent(new Event("Permission_Dialog_DrawOver_Apply_Granted",new Bundle()));
                if (!AppUtils.checkNotificationAccessSettings(this)) {
                    isRequestPermission = true;
                    AppUtils.showNotificationAccess(this);
                }
            } else {
                Log.e("TAN", "onRequestPermissionsResult: 2222");
                AppUtils.showDrawOverlayApp(this);
            }
        } else if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onHasContactPermistion();
            } else {
                Toast.makeText(this, "You have disabled a contacts permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_OVERLAY) {
            Log.e("TAN", "onActivityResult: 1");
            if (AppUtils.checkDrawOverlayApp2(this)) {
                Log.e("TAN", "onActivityResult: 2");
                analystic.trackEvent(new Event("Per_Dlg_DrawOver_Apply_Result_Granted",new Bundle()));
                if (!AppUtils.checkNotificationAccessSettings(this)) {
                    Log.e("TAN", "onActivityResult: 3");
                    isRequestPermission = true;
                    AppUtils.showNotificationAccess(this);
                }
            }
        } else if (requestCode == Constant.REQUEST_NOTIFICATION_ACCESS) {
            Log.e("TAN", "onActivityResult: 5");

            if (AppUtils.checkNotificationAccessSettings(this)) {
                Log.e("TAN", "onActivityResult: 6");
                new Handler().postDelayed(() -> isRequestPermission = false, 500);
                analystic.trackEvent(new Event("Per_Dlg_Apply_Use_App_Granted",new Bundle()));
                applyBgCall();
            }
        } else if (requestCode == 95 && resultCode == RESULT_OK) {
            Log.e("TAN", "onActivityResult: 8");
            isRequestPermission = true;
            new Handler().postDelayed(() -> isRequestPermission = false, 500);
        }
    }

    public void startAnimation() {
        Animation anim8 = AnimationUtils.loadAnimation(this, R.anim.anm_accept_call);
        binding.btnAccept.startAnimation(anim8);
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
            binding.vdoBackgroundCall.setVideoURI(Uri.parse(newPathItem));
            binding.txtApply.setText(getString(R.string.applyContact));
            binding.layoutFooter.setPadding(getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._15sdp), binding.layoutFooter.getPaddingTop(), getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._15sdp), binding.layoutFooter.getPaddingBottom());
            binding.layoutContact.setVisibility(View.VISIBLE);
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
        binding.layoutHeader.setVisibility(View.GONE);
    }

    public void setTranslucent() {
        Window w = getWindow();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            AppUtils.showFullHeader(this, binding.layoutHead);
        }
    }

    @Override
    public void onHasContactPermistion() {
        Intent intent = new Intent(this, SelectContactActivity.class);
        Gson gson = new Gson();
        intent.putExtra(Constant.BACKGROUND, gson.toJson(background));
        startActivityForResult(intent, 95);
    }

    @Override
    protected void onStart() {
        super.onStart();
        appOpenManager.registerObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appOpenManager.unregisterObserver();
    }

    @Override
    public void lifecycleStart(@NonNull AppOpenAd appOpenAd, @NonNull AppOpenManager appOpenManager) {
      /*  if (hasActive() && !interstitialApply.isShowAdsInter() && !isRequestPermission && PermistionUtils.checkHasPermissionCall(this)) {
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
