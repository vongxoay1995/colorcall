package com.colorcall.callerscreen.analystic;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseAnalystic {
    FirebaseAnalytics mFirebaseAnalytics;

    public static FirebaseAnalystic firebase;

    private FirebaseAnalystic(Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public static FirebaseAnalystic getInstance(Context context) {
        synchronized (FirebaseAnalystic.class) {
            if (firebase == null) {
                firebase = new FirebaseAnalystic(context);
            }
        }
        return firebase;
    }
    public void trackEvent(Event event){
        mFirebaseAnalytics.logEvent(event.getKey(), event.getBundleValue());
        Log.e("FirebaseAnalystic", ""+event);
    }
}
