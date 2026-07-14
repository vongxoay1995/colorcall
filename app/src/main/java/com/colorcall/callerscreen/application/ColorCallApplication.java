package com.colorcall.callerscreen.application;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.utils.AppOpenManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.SharedPreferencesUtil;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.orhanobut.hawk.Hawk;

import java.util.Arrays;
import java.util.List;


public class ColorCallApplication extends Application {
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private AppOpenManager appOpenManager;

    public void onCreate() {
        super.onCreate();
        // Initialize Mobile Ads on background thread to avoid blocking cold start (~200-400ms)
        new Thread(() -> {
            MobileAds.initialize(ColorCallApplication.this, initializationStatus -> {});
            if (BuildConfig.DEBUG) {
                List<String> testDeviceIds = Arrays.asList("13C6FDCBDDECC41B0B5817912A40E9E6");
                RequestConfiguration configuration =
                        new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
                MobileAds.setRequestConfiguration(configuration);
            }
        }).start();
        SharedPreferencesUtil.INSTANCE.init(this);
        Hawk.init(this).build();
        appOpenManager = new AppOpenManager(this);
        loadData();
    }


    public AppOpenManager getAppOpenManager() {
        return appOpenManager;
    }


    private void loadData() {
        if (!HawkHelper.isLoadDataFirst()) {
            Log.e("TAN", "loadData: sync load default data");
            // Chạy đồng bộ trên main thread — loadDataDefault() chỉ đọc assets (~1ms),
            // không gây ANR. Đảm bảo data có sẵn trước khi Fragment mount.
            HawkHelper.setListBackground(AppUtils.loadDataDefault(getApplicationContext(), Constant.THUMB_DEFAULT));
            HawkHelper.setLoadDataFirst(true);
        }
    }
}
