package com.colorcall.callerscreen.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

import androidx.lifecycle.ProcessLifecycleOwner
import com.colorcall.callerscreen.BuildConfig
import com.colorcall.callerscreen.application.ColorCallApplication
import com.colorcall.callerscreen.splash.SplashActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import java.util.*

class AppOpenManager(private val application: ColorCallApplication) : LifecycleObserver,
    Application.ActivityLifecycleCallbacks {
    private var appOpenAd: AppOpenAd? = null
    private var loadCallback: AppOpenAdLoadCallback? = null
    private var currentActivity: Activity? = null
    private var loadTime: Long = 0
    private var activityShowAdsOpen: Activity? = null
    private var appOpenManagerObserver: AppOpenManagerObserver? = null
    var isShowingAd = false

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        //if (!PreferencesUtils.getBoolean(AppConstant.IS_PURCHASED, false)) {
        if (currentActivity !is SplashActivity && currentActivity !is AdActivity) {
            showAdIfAvailable()
        }
        // }
    }

    fun showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable) {
            Log.e(
                "TAN",
                "showAdIfAvailable: " + appOpenManagerObserver + "---" + appOpenAd + "--" + this
            )
            if (appOpenManagerObserver != null) {
                appOpenManagerObserver!!.lifecycleStart(appOpenAd!!, this)
            }
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        // Set the reference to null so isAdAvailable() returns false.
                        appOpenAd = null
                        isShowingAd = false
                        appOpenManagerObserver?.lifecycleStop()
                        fetchAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e("TAN", "onAdFailedToShowFullScreenContent: " + adError.message)
                        // remove loading ads View
                        if (adError.code == 3) { //  The ad can not be shown when app is not in foreground.

                        }
                        appOpenManagerObserver?.lifecycleStop()
                        appOpenAd = null
                        isShowingAd = false
                    }

                    override fun onAdShowedFullScreenContent() {
                        activityShowAdsOpen = currentActivity
                        appOpenManagerObserver?.lifecycleShowAd()
                        isShowingAd = true
                    }
                }
            appOpenAd!!.fullScreenContentCallback = fullScreenContentCallback

        } else {
            fetchAd()
        }
    }

    fun fetchAd() {
        // Have unused ad, no need to fetch another.
        if (isAdAvailable) {
            return
        }
        loadCallback = object : AppOpenAdLoadCallback() {

            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                loadTime = Date().time
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            }
        }
        val request = adRequest
        val idAds = if (BuildConfig.DEBUG) {
            ConstantAds.id_ads_open_test
        } else {
            ConstantAds.id_back_app_ads_open
        }
        AppOpenAd.load(
            application,
            idAds,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            loadCallback!!
        )
    }

    private val adRequest: AdRequest
        private get() = AdRequest.Builder()
            .build()

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    val isAdAvailable: Boolean
        get() = appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }


    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(this);
    }


    fun registerObserver(obs: AppOpenManagerObserver?) {
        unregisterObserver()
        appOpenManagerObserver = obs
    }

    fun getAppOpenManagerObserver(): AppOpenManagerObserver? {
        return appOpenManagerObserver
    }

    fun unregisterObserver() {
        appOpenManagerObserver = null
    }

    interface AppOpenManagerObserver {
        fun lifecycleStart(appOpenAd: AppOpenAd, appOpenManager: AppOpenManager)
        fun lifecycleShowAd()
        fun lifecycleStop()
    }
}
