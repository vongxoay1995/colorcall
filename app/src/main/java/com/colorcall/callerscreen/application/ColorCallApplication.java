package com.colorcall.callerscreen.application;

import android.annotation.SuppressLint;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.facebook.FacebookSdk;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.orhanobut.hawk.Hawk;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ColorCallApplication extends MultiDexApplication {
    //private FirebaseRemoteConfig firebaseRemoteConfig;
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        MobileAds.initialize(this, initializationStatus -> {
        });
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("A2287B6C4425EEEAD0688598D4825BAE")).build();
        MobileAds.setRequestConfiguration(configuration);
        Hawk.init(this).build();
        loadData();
        DataManager.getInstance().init(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false);
            AdSettings.addTestDevice("90b91733-6634-4148-8e85-cdcf4b60902f");
        }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        AudienceNetworkAds.initialize(this);

       /* if(!HawkHelper.isFirstAB()){
            configFirebaseRemote();
        }*/
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
    /*private void configFirebaseRemote() {
        long cacheExpiration;
        if (BuildConfig.DEBUG) {
            cacheExpiration = 0;
        } else cacheExpiration = 3600; // 10 s same as the default value
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(cacheExpiration)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        fetchDataFromFirebase();
    }*/
   /* private void fetchDataFromFirebase() {
        firebaseRemoteConfig.fetch().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    firebaseRemoteConfig.activate()
                            .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Boolean> task) {
                                    task.addOnCompleteListener(new OnCompleteListener<Boolean>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Boolean> task) {
                                            createAndPostFirebaseEvent();
                                        }
                                    }).addOnCanceledListener(new OnCanceledListener() {
                                        @Override
                                        public void onCanceled() {
                                            createAndPostFirebaseEvent();
                                        }
                                    });
                                }
                            }).
                            addOnCanceledListener(new OnCanceledListener() {
                                @Override
                                public void onCanceled() {
                                    createAndPostFirebaseEvent();
                                }
                            });
                } else {
                    createAndPostFirebaseEvent();
                }
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                createAndPostFirebaseEvent();
            }
        });
    }

    private void createAndPostFirebaseEvent(){
        long valueNewUx =  firebaseRemoteConfig.getLong("screencall");
        if(valueNewUx!=-1){
            HawkHelper.setScreenCall(valueNewUx);
            HawkHelper.setIsFirstAB(true);
        }
    }*/
}
