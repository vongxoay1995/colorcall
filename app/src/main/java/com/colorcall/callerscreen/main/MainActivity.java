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
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.application.ColorCallApplication;
import com.colorcall.callerscreen.base.BaseActivity;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.databinding.ActivityMainBinding;
import com.colorcall.callerscreen.dialer.activity.CallOwnerActivity;
import com.colorcall.callerscreen.image.ImagesFragment;
import com.colorcall.callerscreen.model.SignApplyMain;
import com.colorcall.callerscreen.model.SignMainImage;
import com.colorcall.callerscreen.model.SignMainVideo;
import com.colorcall.callerscreen.mytheme.MyThemeFragment;
import com.colorcall.callerscreen.paywall.PayWallV3Activity;
import com.colorcall.callerscreen.rate.DialogRate;
import com.colorcall.callerscreen.response.AppClient;
import com.colorcall.callerscreen.response.AppData;
import com.colorcall.callerscreen.response.AppService;
import com.colorcall.callerscreen.setting.SettingActivity;
import com.colorcall.callerscreen.utils.AdListener;
import com.colorcall.callerscreen.utils.AppOpenManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.BannerAdsUtils;
import com.colorcall.callerscreen.utils.DialogPermissionXiaomi;
import com.colorcall.callerscreen.utils.GoogleMobileAdsConsentManager;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.InterstitialApply;
import com.colorcall.callerscreen.utils.PermistionUtils;
import com.colorcall.callerscreen.utils.XiaomiUtilities;
import com.colorcall.callerscreen.video.VideoFragment;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.ump.FormError;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity<ActivityMainBinding> implements AdListener, DialogRate.DialogRateListener, AppOpenManager.AppOpenManagerObserver {
    private Analystic analystic;
    private BannerAdsUtils bannerAdsUtils;
    private boolean showLayoutAds;
    private Fragment imageFrag, videoFrag, mythemeFrag;
    ViewPagerMainAdapter mAdapter;
    private AppOpenManager appOpenManager;
    public GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    public boolean isShowConsent = false;
    DialogPermissionXiaomi dialogPermissionXiaomi;
    DialogPermissionCall dialogPermissionCall;
    boolean isPressGotoSetting;
    boolean isPressLaunchDialer;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener;
    public MainActivity() {
        super(ActivityMainBinding::inflate);
    }

    private final ActivityResultLauncher<Intent> payWallLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    recreate();
                }
            }
    );

    public void moveIapScreen(String fromScr) {
        Intent intent = new Intent(this, PayWallV3Activity.class);
        intent.putExtra("from_scr", fromScr);
        payWallLauncher.launch(intent);
    }

    private void updateButtonPosition() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getBinding().mainDialpadButton.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        getBinding().mainDialpadButton.setLayoutParams(params);
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
        LinearLayout tabStrip = (LinearLayout) getBinding().tabLayout.getChildAt(0);
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
        getBinding().pageBgColor.setAdapter(mAdapter);
        getBinding().tabLayout.setupWithViewPager(getBinding().pageBgColor);
        getBinding().pageBgColor.setCurrentItem(0);
        mAdapter.notifyDataSetChanged();
        getBinding().pageBgColor.setOffscreenPageLimit(2);
        getBinding().pageBgColor.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

        getBinding().mainDialpadButton.setOnClickListener(new View.OnClickListener() {
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
       // InterstitialUtil.getInstance().init(this);
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
            }else{
                HawkHelper.setStateColorCall(true);
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
                    Log.e("TAN", "onActivityResult:1 " );
                    moveCallOwnerActivity();
                }
            } else if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER_DIALOG) {
                if (XiaomiUtilities.isMIUI() && !AppUtils.checkPermissionXiaomi(MainActivity.this) && dialogPermissionCall.isShowing()) {
                    dialogPermissionCall.setStateSw(true);
                    dialogPermissionCall.dismiss();
                    dialogPermissionXiaomi.show();
                } else {
                    Log.e("TAN", "onActivityResult:2 " );

                    HawkHelper.setStateColorCall(true);
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
        getBinding().layoutAds.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAdFailed() {
        getBinding().layoutAds.setVisibility(GONE);
        updateButtonPosition();
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
                AppData body = response.body();
                Log.e("TAN", "onResponse: res = " + (body != null ? body.getApp() : "null body"));
                if (body != null && body.getApp() != null && body.getApp().size() > 0) {
                    checkHasNewData(body.getApp());
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
        Log.e("TAN", "checkHasNewData: list = " + listBg);
        boolean isSelected = false;
        ArrayList<Background> arr = HawkHelper.getListBackground();
        Log.e("TAN", "checkHasNewData2: existing = " + arr);

        for (int i = 0; i < listBg.size(); i++) {
            Background newItem = listBg.get(i);
            Log.e("TAN", "checkHasNewData3: timeUpdate=" + newItem.getTimeUpdate());
            // Chỉ add nếu chưa tồn tại trong list → tránh duplicate + data phình to
            if (!contains(newItem)) {
                newItem.setPosition(arr.size());
                arr.add(newItem);
                Log.e("TAN", "checkHasNewData: added item = " + newItem);
                if (!isSelected) {
                    HawkHelper.setTimeStamp(Long.parseLong(newItem.getTimeUpdate()));
                    isSelected = true;
                }
            } else {
                Log.e("TAN", "checkHasNewData: skip duplicate = " + newItem.getName());
            }
        }
        Log.e("TAN", "checkHasNewData: final list size = " + arr.size());
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

    private void setupKeyboardListener() {
        final View rootView = getBinding().getRoot();
        keyboardLayoutListener = () -> {
            Rect rect = new Rect();
            rootView.getWindowVisibleDisplayFrame(rect);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - rect.bottom;
            boolean isKeyboardOpen = keypadHeight > screenHeight * 0.15;
            if (isKeyboardOpen) {
                getBinding().layoutAds.setVisibility(GONE);
            } else {
                if (showLayoutAds) {
                    getBinding().layoutAds.setVisibility(View.VISIBLE);
                }
            }
        };
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!HawkHelper.isPayed()) {
            cleanPopupWindow();
        }
        appOpenManager.unregisterObserver();
        // Remove keyboard listener to avoid memory leak
        if (keyboardLayoutListener != null) {
            getBinding().getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
            keyboardLayoutListener = null;
        }
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


    @Override
    protected void onCreate() {
        consumeSystemBars(false, (statusBarHeight, bottomBarHeight) -> {
            getBinding().topView.getLayoutParams().height = statusBarHeight;
            getBinding().bottomView.getLayoutParams().height = bottomBarHeight;

            if (getBinding().topView.getLayoutParams().height == 0) {
                getBinding().topView.getLayoutParams().height = statusBarHeight;
            }
            getBinding().bottomView.requestLayout();
            getBinding().topView.requestLayout();
            return null;
        });

        setSystemBarStyle(false); // false = white icons on dark/colored background
        //AppUtils.changeStatusBarColor(this, R.color.colorHeaderMain);
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(getApplicationContext());
        showForm();
        appOpenManager = ((ColorCallApplication) getApplication()).getAppOpenManager();
        loadDataApi(true);
        analystic = Analystic.getInstance(this);
        bannerAdsUtils = new BannerAdsUtils(this, getBinding().layoutAds);

        if (AppUtils.isNetworkConnected(this) && !HawkHelper.isPayed()) {
            createWindowManagerField();
            preLoadInter();
            loadAds();
        } else {
            getBinding().layoutAds.setVisibility(GONE);
            getBinding().btnRemoveAds.setVisibility(INVISIBLE);
            updateButtonPosition();
        }

        analystic.trackEvent(ManagerEvent.mainOpen());
        analystic.trackEvent(ManagerEvent.grantedPermission(PermistionUtils.checkHasPermissionCall(this)));
        setupKeyboardListener();

        initDialogPermissionXiaomi();
        requestNotificationPermission();
        //AppUtils.setFullNav(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideBottomNavigationBar(getBinding().getRoot());
    }

    @Override
    protected void onView() {
        // Safety fallback: if topView is still 0dp after view is attached, apply insets now
        if (getBinding().topView.getHeight() == 0 || getBinding().topView.getLayoutParams().height == 0) {
            androidx.core.view.WindowInsetsCompat rootInsets =
                    androidx.core.view.ViewCompat.getRootWindowInsets(getBinding().getRoot());
            if (rootInsets != null) {
                int sbHeight = rootInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars()).top;
                int nbHeight = rootInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.navigationBars()).bottom;
                if (sbHeight > 0) {
                    getBinding().topView.getLayoutParams().height = sbHeight;
                    getBinding().topView.requestLayout();
                }
                if (nbHeight > 0) {
                    getBinding().bottomView.getLayoutParams().height = nbHeight;
                    getBinding().bottomView.requestLayout();
                }
            }
        }
        initDataPage();
        disableToolTipTextTab();

        getBinding().btnSetting.setOnClickListener(view -> {
            analystic.trackEvent(ManagerEvent.mainSlideClick());
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
        });

        getBinding().btnRemoveAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveIapScreen("Settings");
            }
        });
    }

 /*   @Override
    public void onCloseScope() {

    }*/
}
