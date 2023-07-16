package com.colorcall.callerscreen.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

public class OpenDrawSettings {
    public static final String manufacturer = Build.MANUFACTURER.toLowerCase();
  /*  public d01 a;

    public OpenDrawSettings(d01 d01) {
        this.a = d01;
    }*/

    public static Intent getIntentDefault(Context context) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        return intent;
    }

    public static boolean queryIntentActivity(Context context, Intent intent) {
        return context.getPackageManager().queryIntentActivities(intent, 65536).size() > 0;
    }

}
