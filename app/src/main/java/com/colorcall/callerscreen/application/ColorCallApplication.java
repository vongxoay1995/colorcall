package com.colorcall.callerscreen.application;

import android.annotation.SuppressLint;
import android.app.Application;

import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.utils.AppOpenManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.orhanobut.hawk.Hawk;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ColorCallApplication extends Application {
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private AppOpenManager appOpenManager;

    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(ColorCallApplication.this, initializationStatus -> {
        });
        Hawk.init(this).build();
        appOpenManager = new AppOpenManager(this);
        loadData();
    }


    public AppOpenManager getAppOpenManager() {
        return appOpenManager;
    }



    @SuppressLint("StaticFieldLeak")
    private void loadData() {
        if (!HawkHelper.isLoadDataFirst()) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                HawkHelper.setListBackground(AppUtils.loadDataDefault(getApplicationContext(), Constant.THUMB_DEFAULT));
                HawkHelper.setLoadDataFirst(true);
            });
        }
    }
}
