package com.colorcall.callerscreen.application;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseCommonRegistrar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.orhanobut.hawk.Hawk;
import com.unity3d.ads.metadata.MetaData;

import java.util.ArrayList;


public class ColorCallApplication extends MultiDexApplication {
    private static ColorCallApplication callApplication;

    public void onCreate() {
        super.onCreate();
        callApplication = this;
        MultiDex.install(this);
        MobileAds.initialize(this, initializationStatus -> {});
        Hawk.init(this).build();
        loadData(Constant.THUMB_DEFAULT);
        DataManager.getInstance().init(this);
        AppLovinPrivacySettings.setHasUserConsent(true, this);
        AppLovinSdk.getInstance(this).initializeSdk();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false);
        }
    }
    @SuppressLint("StaticFieldLeak")
    private void loadData(final String assetsDir) {
        if (!HawkHelper.isLoadDataFirst()) {
            new AsyncTask<Void,Void, ArrayList<Background>>(){
                @Override
                protected ArrayList<Background> doInBackground(Void... voids) {
                    return AppUtils.loadDataDefault(getApplicationContext(), assetsDir);
                }

                @Override
                protected void onPostExecute(ArrayList<Background> backgrounds) {
                    HawkHelper.setListBackground(backgrounds);
                    HawkHelper.setLoadDataFirst(true);
                    super.onPostExecute(backgrounds);
                }
            }.execute();
        }
    }
    public static ColorCallApplication get() {
        return callApplication;
    }
}
