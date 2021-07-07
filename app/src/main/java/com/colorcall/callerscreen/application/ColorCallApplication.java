package com.colorcall.callerscreen.application;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.orhanobut.hawk.Hawk;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ColorCallApplication extends MultiDexApplication {
    private static ColorCallApplication callApplication;
    public void onCreate() {
        super.onCreate();
        callApplication = this;
        MultiDex.install(this);
        MobileAds.initialize(this, initializationStatus -> {});
        Hawk.init(this).build();
        loadData();
        DataManager.getInstance().init(this);
        AppLovinPrivacySettings.setHasUserConsent(true, this);
        AppLovinSdk.getInstance(this).initializeSdk();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false);
        }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    @SuppressLint("StaticFieldLeak")
    private void loadData() {
        if (!HawkHelper.isLoadDataFirst()) {
            Log.e("TAN", "loadData: ");
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                HawkHelper.setListBackground(AppUtils.loadDataDefault(getApplicationContext(), Constant.THUMB_DEFAULT));
                HawkHelper.setLoadDataFirst(true);
            });
        }
    }
    public static ColorCallApplication get() {
        return callApplication;
    }
}
