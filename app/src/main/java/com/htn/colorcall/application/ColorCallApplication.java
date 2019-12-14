package com.htn.colorcall.application;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.htn.colorcall.BuildConfig;
import com.htn.colorcall.R;
import com.htn.colorcall.constan.Constant;
import com.htn.colorcall.database.DataManager;
import com.htn.colorcall.main.ImageLoading;
import com.htn.colorcall.model.Category;
import com.htn.colorcall.utils.AppUtils;
import com.htn.colorcall.utils.CategoryUtils;
import com.htn.colorcall.utils.HawkHelper;
import com.orhanobut.hawk.Hawk;

import java.lang.reflect.Field;
import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import ss.com.bannerslider.Slider;

public class ColorCallApplication extends MultiDexApplication {
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        Slider.init(new ImageLoading(this));
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        Hawk.init(this).build();
        DataManager.getInstance().init(this);
        // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit);
    }
}
