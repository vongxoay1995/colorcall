package com.colorcall.callerscreen.main;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.image.ImagesFragment;
import com.colorcall.callerscreen.model.SignApplyMain;
import com.colorcall.callerscreen.model.SignMainImage;
import com.colorcall.callerscreen.model.SignMainVideo;
import com.colorcall.callerscreen.mytheme.MyThemeFragment;
import com.colorcall.callerscreen.rate.DialogRate;
import com.colorcall.callerscreen.response.AppClient;
import com.colorcall.callerscreen.response.AppData;
import com.colorcall.callerscreen.response.AppService;
import com.colorcall.callerscreen.setting.SettingActivity;
import com.colorcall.callerscreen.utils.AdListener;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.BannerAdsUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.InterstitialApply;
import com.colorcall.callerscreen.utils.InterstitialUtil;
import com.colorcall.callerscreen.video.VideoFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements AdListener, DialogRate.DialogRateListener, KeyboardVisibilityEventListener {
    @BindView(R.id.tab_layout)
    TabLayout tab_layout;
    @BindView(R.id.pageBgColor)
    ViewPager pageBgColor;
    @BindView(R.id.layout_ads)
    RelativeLayout layoutAds;
    @BindView(R.id.layout_head)
    RelativeLayout layout_head;
    private Analystic analystic;
    private BannerAdsUtils bannerAdsUtils;
    private boolean showLayoutAds;
    private Fragment imageFrag, videoFrag, mythemeFrag;
    ViewPagerMainAdapter mAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AppUtils.showFullHeader(this, layout_head);
        loadDataApi(true);
        analystic = Analystic.getInstance(this);
        bannerAdsUtils = new BannerAdsUtils(this, layoutAds);
        initDataPage();
        if (AppUtils.isNetworkConnected(this)) {
            loadAds();
        } else {
            layoutAds.setVisibility(View.GONE);
        }
        disableToolTipTextTab();
        KeyboardVisibilityEvent.setEventListener(this, this);
        //MediationTestSuite.launch(MainActivity.this);
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
        LinearLayout tabStrip = (LinearLayout) tab_layout.getChildAt(0);
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
        pageBgColor.setAdapter(mAdapter);
        tab_layout.setupWithViewPager(pageBgColor);
        pageBgColor.setCurrentItem(0);
        mAdapter.notifyDataSetChanged();
        pageBgColor.setOffscreenPageLimit(2);
        pageBgColor.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        InterstitialUtil.getInstance().init(this);
        InterstitialApply.getInstance().init(this);
    }

    @Override
    protected void onResume() {
        analystic.trackEvent(ManagerEvent.mainOpen());
        super.onResume();
    }

    @OnClick({R.id.btnSetting})
    public void onViewClicked() {
        analystic.trackEvent(ManagerEvent.mainSlideClick());
        startActivity(new Intent(this, SettingActivity.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadAds() {
        String ID_ADS_GG = "ca-app-pub-3222539657172474/8137142250";
        String ID_ADS_FB = "1205962693239181_1205972196571564";
        bannerAdsUtils.setIdAds(ID_ADS_GG,ID_ADS_FB);
        bannerAdsUtils.setAdListener(this);
        bannerAdsUtils.showMediationBannerAds();
    }

    @Override
    public void onAdloaded() {
        showLayoutAds = true;
        layoutAds.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAdFailed() {
        layoutAds.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bannerAdsUtils!=null){
            bannerAdsUtils.destroyFb();
        }
        InterstitialUtil.getInstance().onDestroy();
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
            if (Long.parseLong(listBg.get(i).getTime_update()) > lastTimeUpdate) {
                listBg.get(i).setPosition(initPosition + i);
                arr.add(listBg.get(i));
                if (!isSelected) {
                    HawkHelper.setTimeStamp(Long.parseLong(listBg.get(i).getTime_update()));
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
        } else {
            moveStore();
        }
    }

    @Override
    public void onFeedBack(String content, int rate) {
        analystic.trackEvent(ManagerEvent.rateFeedBack());
        HawkHelper.setDialogShowRate(false);
        Intent intent = new Intent(Intent.ACTION_SENDTO)
                .setData(new Uri.Builder().scheme("mailto").build())
                .putExtra(Intent.EXTRA_EMAIL, new String[]{"Call color <phamthanhtan.dev@gmail.com>"})
                .putExtra(Intent.EXTRA_SUBJECT, "Feedback for the Call color app")
                .putExtra(Intent.EXTRA_TEXT, content + " [with rate " + rate+"]");

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
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onVisibilityChanged(boolean isOpen) {
        if (isOpen) {
            layoutAds.setVisibility(View.GONE);
        } else {
            if (showLayoutAds) {
                layoutAds.setVisibility(View.VISIBLE);
            }
        }
    }
}
