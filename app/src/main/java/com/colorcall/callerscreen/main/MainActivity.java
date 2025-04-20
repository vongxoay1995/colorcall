package com.colorcall.callerscreen.main;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static com.colorcall.callerscreen.constan.Constant.REQUEST_CODE_SET_DEFAULT_DIALER;
import static com.colorcall.callerscreen.constan.Constant.REQUEST_CODE_SET_DEFAULT_DIALER_DIALOG;
import static com.colorcall.callerscreen.utils.AppUtils.isDefaultDialer;
import static com.colorcall.callerscreen.utils.ConstantAds.banner_main_admob_tk_cu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.application.ColorCallApplication;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.databinding.ActivityMainBinding;
import com.colorcall.callerscreen.dialer.activity.CallOwnerActivity;
import com.colorcall.callerscreen.image.ImagesFragment;
import com.colorcall.callerscreen.model.SignApplyMain;
import com.colorcall.callerscreen.model.SignMainImage;
import com.colorcall.callerscreen.model.SignMainVideo;
import com.colorcall.callerscreen.mytheme.MyThemeFragment;
import com.colorcall.callerscreen.paywall.PayWallActivity;
import com.colorcall.callerscreen.rate.DialogRate;
import com.colorcall.callerscreen.response.AppClient;
import com.colorcall.callerscreen.response.AppData;
import com.colorcall.callerscreen.response.AppService;
import com.colorcall.callerscreen.setting.SettingActivity;
import com.colorcall.callerscreen.utils.AdListener;
import com.colorcall.callerscreen.utils.AppOpenManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.BannerAdsUtils;
import com.colorcall.callerscreen.utils.ConstantAds;
import com.colorcall.callerscreen.utils.DialogPermissionXiaomi;
import com.colorcall.callerscreen.utils.GoogleMobileAdsConsentManager;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.InterstitialApply;
import com.colorcall.callerscreen.utils.InterstitialUtil;
import com.colorcall.callerscreen.utils.PermistionUtils;
import com.colorcall.callerscreen.utils.XiaomiUtilities;
import com.colorcall.callerscreen.video.VideoFragment;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.ump.FormError;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements AdListener, DialogRate.DialogRateListener, KeyboardVisibilityEventListener, AppOpenManager.AppOpenManagerObserver {
    private Analystic analystic;
    private BannerAdsUtils bannerAdsUtils;
    private boolean showLayoutAds;
    private Fragment imageFrag, videoFrag, mythemeFrag;
    ViewPagerMainAdapter mAdapter;
    private AppOpenManager appOpenManager;
    private InterstitialUtil interstitialUtil;
    public GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    public boolean isShowConsent = false;
    private ActivityMainBinding binding;
    DialogPermissionXiaomi dialogPermissionXiaomi;
    DialogPermissionCall dialogPermissionCall;
    boolean isPressGotoSetting;
    boolean isPressLaunchDialer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppUtils.changeStatusBarColor(this, R.color.colorHeaderMain);
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(getApplicationContext());
        showForm();
        appOpenManager = ((ColorCallApplication) getApplication()).getAppOpenManager();
        loadDataApi(true);
        analystic = Analystic.getInstance(this);
        bannerAdsUtils = new BannerAdsUtils(this, binding.layoutAds);
        initDataPage();
        if (AppUtils.isNetworkConnected(this) && !HawkHelper.isPayed()) {
            createWindowManagerField();
            preLoadInter();
            loadAds();
        } else {
            binding.layoutAds.setVisibility(GONE);
            binding.btnRemoveAds.setVisibility(INVISIBLE);
            updateButtonPosition();
        }
        disableToolTipTextTab();
        analystic.trackEvent(ManagerEvent.mainOpen());
        analystic.trackEvent(ManagerEvent.grantedPermission(PermistionUtils.checkHasPermissionCall(this)));
        KeyboardVisibilityEvent.setEventListener(this, this);
        binding.btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                analystic.trackEvent(ManagerEvent.mainSlideClick());
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            }
        });
        binding.btnRemoveAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveIapScreen();
            }
        });
        initDialogPermissionXiaomi();
        requestNotificationPermission();
        AppUtils.setFullNav(this);
    }

    private final ActivityResultLauncher<Intent> payWallLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    recreate();
                }
            }
    );

    public void moveIapScreen() {
        Intent intent = new Intent(this, PayWallActivity.class);
        payWallLauncher.launch(intent);
    }

    private void updateButtonPosition() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.mainDialpadButton.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        binding.mainDialpadButton.setLayoutParams(params);
    }

    private void initDialogPermissionXiaomi() {
        dialogPermissionXiaomi = new DialogPermissionXiaomi(this);
        dialogPermissionXiaomi.setListener(new DialogPermissionXiaomi.DialogPermissionXiaomiListener() {
            @Override
            public void onOkClicked() {
                isPressGotoSetting = true;
                AppUtils.openDetailPermission(MainActivity.this);
            }

            @Override
            public void onDialogDismissed() {

            }
        });
        dialogPermissionCall = new DialogPermissionCall(this);
        dialogPermissionCall.setListenerDialer(new DialogPermissionCall.DialogPermissionDialerListener() {
            @Override
            public void onSwDialerClick() {
                AppUtils.launchSetDefaultDialerIntentDialog(MainActivity.this);
            }

            @Override
            public void onDialogDismissed() {
                Log.e("TAN", "onDialogDismissed: dismis dialer");
            }
        });
        if (!isDefaultDialer(MainActivity.this)) {
            dialogPermissionCall.show();
        }

    }

    private void showForm() {
        googleMobileAdsConsentManager.showForm(this, new GoogleMobileAdsConsentManager.OnConsentGatheringCompleteListener() {
            @Override
            public void consentGatheringComplete(FormError error) {
                Log.e("TAN", "consentGatheringCompletemain: " + error);
                if (AppUtils.isNetworkConnected(MainActivity.this)) {
                    loadAds();
                    preLoadInter();
                }
            }

            @Override
            public void conSentShow() {
                isShowConsent = true;
                Log.e("TAN", "conSentShow: ");
            }

            @Override
            public void conSentDismiss() {
                isShowConsent = false;
                Log.e("TAN", "conSentDismiss: ");
            }
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !dialogPermissionCall.isShowing()) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void moveStore() {
        try {
            HawkHelper.setDialogShowRate(false);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String linkRateApp = "https://play.google.com/store/apps/details?id=" + getPackageName();
            intent.setData(Uri.parse(linkRateApp));
            startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            anfe.printStackTrace();
        }
    }

    private void disableToolTipTextTab() {
        LinearLayout tabStrip = (LinearLayout) binding.tabLayout.getChildAt(0);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setOnLongClickListener(v -> true);
        }
    }

    private void initDataPage() {
        mAdapter = new ViewPagerMainAdapter(getSupportFragmentManager());
        videoFrag = new VideoFragment(this);
        imageFrag = new ImagesFragment(this);
        mythemeFrag = new MyThemeFragment();
        mAdapter.addFrag(videoFrag, getString(R.string.videos));
        mAdapter.addFrag(imageFrag, getString(R.string.images));
        mAdapter.addFrag(mythemeFrag, getString(R.string.mytheme));
        binding.pageBgColor.setAdapter(mAdapter);
        binding.tabLayout.setupWithViewPager(binding.pageBgColor);
        binding.pageBgColor.setCurrentItem(0);
        mAdapter.notifyDataSetChanged();
        binding.pageBgColor.setOffscreenPageLimit(2);
        binding.pageBgColor.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        binding.mainDialpadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                analystic.trackEvent("Main_Dial_Pad_Button_Clicked");
                if (!isDefaultDialer(MainActivity.this)) {
                    dialogPermissionCall.show();
                } else {
                    moveCallOwnerActivity();
                }
            }
        });
    }

    private void preLoadInter() {
        interstitialUtil = InterstitialUtil.getInstance();
        interstitialUtil.init(this, ConstantAds.id_ads_inter_item_admob_tk_cu);
        InterstitialApply.getInstance().init(this);
    }

    @Override
    protected void onResume() {
        if (isPressGotoSetting) {
            isPressGotoSetting = false;
            if (!AppUtils.checkPermissionXiaomi(MainActivity.this)) {
                Toast.makeText(MainActivity.this, getString(R.string.request_permission_for_feature), Toast.LENGTH_SHORT).show();
            } else if (isPressLaunchDialer) {
                isPressLaunchDialer = false;
                moveCallOwnerActivity();
            }
        }
        super.onResume();
    }

    public void moveCallOwnerActivity() {
        HawkHelper.setStateColorCall(true);
        Intent intent = new Intent(MainActivity.this, CallOwnerActivity.class);
        intent.putExtra("move_dialer_from_main", true);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
                if (XiaomiUtilities.isMIUI() && !AppUtils.checkPermissionXiaomi(MainActivity.this)) {
                    dialogPermissionXiaomi.show();
                } else {
                    moveCallOwnerActivity();
                }
            } else if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER_DIALOG) {
                if (XiaomiUtilities.isMIUI() && !AppUtils.checkPermissionXiaomi(MainActivity.this) && dialogPermissionCall.isShowing()) {
                    dialogPermissionCall.setStateSw(true);
                    dialogPermissionCall.dismiss();
                    dialogPermissionXiaomi.show();
                } else {
                    dialogPermissionCall.setStateSw(true);
                    dialogPermissionCall.dismiss();
                }
            }
        } else if (dialogPermissionCall.isShowing()) {
            dialogPermissionCall.setStateSw(false);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadAds() {
        String ID_ADS_GG = "ca-app-pub-3222539657172474/4654234996";

        bannerAdsUtils.setIdAds(banner_main_admob_tk_cu);
        bannerAdsUtils.setAdListener(this);
        bannerAdsUtils.loadCollapsibleBanner();
    }

    @Override
    public void onAdloaded() {
        showLayoutAds = true;
        binding.layoutAds.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAdFailed() {
        binding.layoutAds.setVisibility(GONE);
    }


    public void refreshCalApi() {
        loadDataApi(false);
    }

    private void loadDataApi(boolean isRefresh) {
        AppService appService = AppClient.getInstance();
        Call<AppData> app = appService.getTheme();
        app.enqueue(new Callback<AppData>() {
            @Override
            public void onResponse(@NonNull Call<AppData> call, @NonNull Response<AppData> response) {
                if (response.body() != null && response.body().getApp().size() > 0) {
                    checkHasNewData(response.body().getApp());
                }
                Intent intent = new Intent();
                intent.setAction(Constant.ACTION_LOAD_COMPLETE_THEME);
                intent.putExtra(Constant.REFRESH_All, isRefresh);
                SignMainVideo signMainVideo = new SignMainVideo(true, isRefresh);
                SignMainImage signMainImage = new SignMainImage(true, isRefresh);
                EventBus.getDefault().postSticky(signMainVideo);
                EventBus.getDefault().postSticky(signMainImage);
            }

            @Override
            public void onFailure(Call<AppData> call, Throwable t) {
                Intent intent = new Intent();
                intent.setAction(Constant.ACTION_LOAD_COMPLETE_THEME);
                intent.putExtra(Constant.REFRESH_All, isRefresh);
                SignMainVideo signMainVideo = new SignMainVideo(true, isRefresh);
                SignMainImage signMainImage = new SignMainImage(true, isRefresh);
                EventBus.getDefault().postSticky(signMainVideo);
                EventBus.getDefault().postSticky(signMainImage);
            }
        });
    }

    private void checkHasNewData(ArrayList<Background> listBg) {
        long lastTimeUpdate = HawkHelper.getTimeStamp();
        boolean isSelected = false;
        int initPosition = HawkHelper.getListBackground().size();
        ArrayList<Background> arr = HawkHelper.getListBackground();
        for (int i = 0; i < listBg.size(); i++) {
            if (Long.parseLong(listBg.get(i).getTimeUpdate()) > lastTimeUpdate) {
                listBg.get(i).setPosition(initPosition + i);
                arr.add(listBg.get(i));
                if (!isSelected) {
                    HawkHelper.setTimeStamp(Long.parseLong(listBg.get(i).getTimeUpdate()));
                    isSelected = true;
                }
            }

        /*    if(!contains(listBg.get(i))) {
                listBg.get(i).setPosition(initPosition+i);
                arr.add(listBg.get(i));
                if(!isSelected){
                    HawkHelper.setTimeStamp(Long.parseLong(listBg.get(i).getTime_update()));
                    isSelected = true;
                }
            }*/

        }
        HawkHelper.setListBackground(arr);
    }

    private boolean contains(Background item) {
        for (Background i : HawkHelper.getListBackground()) {
            if (i.getName().equals(item.getName())) {
                return true;
            }
        }
        return false;
    }

    public void showDialogRate() {
        DialogRate dialogRate = new DialogRate(this, this);
        dialogRate.show();
        analystic.trackEvent(ManagerEvent.rateShow());
    }

    @Override
    public void onRate(int rate) {
        ReviewManager reviewManager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = reviewManager.requestReviewFlow();
        request.addOnSuccessListener(result -> {
            Task<Void> flow = reviewManager.launchReviewFlow(this, result);
            flow.addOnSuccessListener(result1 -> {
                analystic.trackEvent(ManagerEvent.rateReview(rate));
                HawkHelper.setDialogShowRate(false);
            }).addOnFailureListener(e -> {
            });
        }).addOnFailureListener(e -> moveStore());
    }

    @Override
    public void onFeedBack(String content, int rate) {
        analystic.trackEvent(ManagerEvent.rateFeedBack());
        HawkHelper.setDialogShowRate(false);
        Intent intent = new Intent(Intent.ACTION_SENDTO)
                .setData(new Uri.Builder().scheme("mailto").build())
                .putExtra(Intent.EXTRA_EMAIL, new String[]{"Call color <phamthanhtan.dev@gmail.com>"})
                .putExtra(Intent.EXTRA_SUBJECT, "Feedback for the Call color app")
                .putExtra(Intent.EXTRA_TEXT, content + " [with rate " + rate + "]");

        ComponentName emailApp = intent.resolveActivity(getPackageManager());
        ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
        if (emailApp != null && !emailApp.equals(unsupportedAction))
            try {
                Intent chooser = Intent.createChooser(intent, "Send email with");
                startActivity(chooser);
                return;
            } catch (ActivityNotFoundException ignored) {
            }

        Toast.makeText(this, "Couldn't find an email app and account", Toast.LENGTH_LONG).show();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSignShowRate(SignApplyMain signApplyMain) {
        if (HawkHelper.isCanShowDiaLogRate() && !disableShowRate()) {
            showDialogRate();
        }
        EventBus.getDefault().removeStickyEvent(signApplyMain);
    }

    private boolean disableShowRate() {
        int count = HawkHelper.getCoutShowRate();
        //if count =2, thi return false
        if (count <= 30) {
            return count != 3 && count != 7 && count != 12;
        } else {
            return (count - 30) % 30 != 0;
        }
    }

    @Override
    protected void onStart() {
        Log.e("TAN", "onStart: main");
        EventBus.getDefault().register(this);
        super.onStart();
        appOpenManager.registerObserver(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onVisibilityChanged(boolean isOpen) {
        if (isOpen) {
            binding.layoutAds.setVisibility(GONE);
        } else {
            if (showLayoutAds) {
                binding.layoutAds.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!HawkHelper.isPayed()) {
            cleanPopupWindow();
        }
        appOpenManager.unregisterObserver();
    }

    private Field windowManagerField;
    private Field viewsField;

    @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
    private void createWindowManagerField() {
        try {
            Class<?> windowManagerImplClass = Class.forName("android.view.WindowManagerImpl");
            windowManagerField = windowManagerImplClass.getDeclaredField("mGlobal");
            windowManagerField.setAccessible(true);

            Class<?> windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal");
            viewsField = windowManagerGlobalClass.getDeclaredField("mViews");
            viewsField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanPopupWindow() {
        try {
            WindowManager windowManagerImpl = (WindowManager) getSystemService(WINDOW_SERVICE);
            Object windowManagerGlobal = windowManagerField.get(windowManagerImpl);
            @SuppressWarnings("unchecked")
            List<View> views = (List<View>) viewsField.get(windowManagerGlobal);

            for (View view : views) {
                if (view.getClass().getName().contains("PopupDecorView")) {
                    windowManagerImpl.removeViewImmediate(view);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void lifecycleStart(@NonNull AppOpenAd appOpenAd, @NonNull AppOpenManager appOpenManager) {
        Log.e("TAN", "lifecycleStart: main" + appOpenManager);
      /*  if (hasActive() && !interstitialUtil.isShowAdsInter()&&mythemeFrag!=null&&!((MyThemeFragment)mythemeFrag).isRequestImageVideo) {
            appOpenManager.setShowingAd(true);
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

    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                /*if (!isGranted) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Build.VERSION.SDK_INT >= 33) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                            } else {
                            }
                        }
                    }
                }*/
            });


}
