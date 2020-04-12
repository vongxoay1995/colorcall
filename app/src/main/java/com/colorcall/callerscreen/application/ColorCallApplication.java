package com.colorcall.callerscreen.application;

import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.gms.ads.MobileAds;
import com.orhanobut.hawk.Hawk;

import io.fabric.sdk.android.Fabric;

public class ColorCallApplication extends MultiDexApplication {
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        MobileAds.initialize(this, initializationStatus -> {});
        Hawk.init(this).build();
        DataManager.getInstance().init(this);
        Fabric.with(this, crashlyticsKit);
    }
}
