package com.colorcall.callerscreen.analystic;

import android.content.Context;

import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;

public class Analystic {
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppEventsLogger loggerEvent;
    public static Analystic mAnalytics;

    private Analystic(Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        loggerEvent = AppEventsLogger.newLogger(context);
    }

    public static Analystic getInstance(Context context) {
        synchronized (Analystic.class) {
            if (mAnalytics == null) {
                mAnalytics = new Analystic(context);
            }
        }
        return mAnalytics;
    }
    public void trackEvent(Event event){
        mFirebaseAnalytics.logEvent(event.getKey(), event.getBundleValue());
        loggerEvent.logEvent(event.getKey(), event.getBundleValue());
    }
}
