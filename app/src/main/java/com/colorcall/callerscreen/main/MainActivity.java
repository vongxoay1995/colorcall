package com.colorcall.callerscreen.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.image.ImagesFragment;
import com.colorcall.callerscreen.model.SignMainImage;
import com.colorcall.callerscreen.mytheme.MyThemeFragment;
import com.colorcall.callerscreen.response.AppClient;
import com.colorcall.callerscreen.response.AppData;
import com.colorcall.callerscreen.response.AppService;
import com.colorcall.callerscreen.setting.SettingActivity;
import com.colorcall.callerscreen.utils.AdListener;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.BannerAdsUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.model.SignMainVideo;
import com.colorcall.callerscreen.utils.InterstitialUtil;
import com.colorcall.callerscreen.video.VideoFragment;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements AdListener {
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
    }

    @Override
    protected void onResume() {
        analystic.trackEvent(ManagerEvent.mainOpen());
        super.onResume();
    }

    @OnClick({R.id.btnMenu})
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

    @Override
    public void onAdloaded() {
        layoutAds.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAdFailed() {
        layoutAds.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
}
