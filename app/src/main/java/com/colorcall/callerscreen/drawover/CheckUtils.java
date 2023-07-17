package com.colorcall.callerscreen.drawover;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import java.lang.reflect.Method;

public abstract class CheckUtils {
    public int a;
    public String b;
    public AppOpsManager c;

    public abstract void a(Intent intent);

    public abstract void a(Intent intent, int i);

    public final boolean a() {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        Context b2 = b();
        if (this.a < 14) {
            this.a = b().getApplicationInfo().targetSdkVersion;
        }
        if (this.a >= 23) {
            return Settings.canDrawOverlays(b2);
        }
        int i = b().getApplicationInfo().uid;
        Class<AppOpsManager> cls = AppOpsManager.class;
        try {
            Method method = cls.getMethod("checkOpNoThrow", Integer.TYPE, Integer.TYPE, String.class);
            int intValue = ((Integer) cls.getDeclaredField("OP_SYSTEM_ALERT_WINDOW").get(Integer.class)).intValue();
            if (this.c == null) {
                this.c = (AppOpsManager) b().getSystemService("appops");
            }
            AppOpsManager appOpsManager = this.c;
            Object[] objArr = new Object[3];
            objArr[0] = Integer.valueOf(intValue);
            objArr[1] = Integer.valueOf(i);
            if (this.b == null) {
                this.b = b().getApplicationContext().getPackageName();
            }
            objArr[2] = this.b;
            int intValue2 = ((Integer) method.invoke(appOpsManager, objArr)).intValue();
            if (intValue2 == 0 || intValue2 == 4) {
                return true;
            }
            return false;
        } catch (Throwable unused) {
            return true;
        }
    }

    public abstract boolean a(String str);

    public abstract Context b();

}
