package com.colorcall.callerscreen.analystic;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

public class Analystic {
    private FirebaseAnalytics mFirebaseAnalytics;
    public static Analystic mAnalytics;

    private Analystic(Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
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
        Log.e("TAN", "trackEvent: "+event);
        mFirebaseAnalytics.logEvent(event.getKey(), event.getBundleValue());
    }
    public void trackEvent(String event){
        Log.e("TAN", "trackEvent: "+event);
        mFirebaseAnalytics.logEvent(event, new Bundle());
    }
    public void trackEventParams(String event,Bundle bundle){
        Log.e("TAN", "trackEvent: "+event);
        mFirebaseAnalytics.logEvent(event, bundle);
    }
}
