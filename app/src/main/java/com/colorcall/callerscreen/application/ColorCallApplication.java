package com.colorcall.callerscreen.application;

import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.google.android.gms.ads.MobileAds;
import com.orhanobut.hawk.Hawk;
import com.unity3d.ads.metadata.MetaData;


public class ColorCallApplication extends MultiDexApplication {
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        MobileAds.initialize(this, initializationStatus -> {});
        Hawk.init(this).build();
        DataManager.getInstance().init(this);
        AppLovinPrivacySettings.setHasUserConsent(true, this);
        AppLovinSdk.getInstance(this).initializeSdk();
    }
}
