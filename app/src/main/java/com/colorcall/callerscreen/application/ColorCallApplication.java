package com.colorcall.callerscreen.application;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.utils.AppOpenManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.facebook.FacebookSdk;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.orhanobut.hawk.Hawk;

import org.jetbrains.annotations.NotNull;

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
        Hawk.init(this).build();
        appOpenManager = new AppOpenManager(this);

        loadData();
        DataManager.getInstance().init(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false);
            List<String> testDeviceIds = Arrays.asList("C672C9D51F65E8B9B0345F9F8E4F7CC1");
            RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
            MobileAds.setRequestConfiguration(configuration);
            AdSettings.addTestDevice("90b91733-6634-4148-8e85-cdcf4b60902f");
        }
        AudienceNetworkAds.initialize(this);
        // configFirebaseRemote();
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

    private void configFirebaseRemote() {
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
    }

    private void fetchDataFromFirebase() {
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

    private void createAndPostFirebaseEvent() {
        long priority_ads = firebaseRemoteConfig.getLong("priority_ads");
        if (priority_ads != -1) {
            HawkHelper.setPriorityAds(priority_ads);
        }
    }
}
