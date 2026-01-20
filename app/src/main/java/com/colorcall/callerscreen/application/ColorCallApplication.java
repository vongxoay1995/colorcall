package com.colorcall.callerscreen.application;

import static com.colorcall.callerscreen.utils.SharePreferenceKeyKt.LOAD_DATA_FIRST_FIRST;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.utils.AppOpenManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.ContextExtKt;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.orhanobut.hawk.Hawk;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ColorCallApplication extends Application {
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private AppOpenManager appOpenManager;

    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(ColorCallApplication.this, initializationStatus -> {
        });
        if (BuildConfig.DEBUG){
            List<String> testDeviceIds = Arrays.asList("13C6FDCBDDECC41B0B5817912A40E9E6");
            RequestConfiguration configuration =
                    new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
            MobileAds.setRequestConfiguration(configuration);
        }
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
            Log.e("TAN", "loadData: AAAA" );
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                HawkHelper.setListBackground(AppUtils.loadDataDefault(getApplicationContext(), Constant.THUMB_DEFAULT));
                HawkHelper.setLoadDataFirst(true);
            });
        }
    }
}
