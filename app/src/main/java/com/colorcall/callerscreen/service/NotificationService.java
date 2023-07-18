package com.colorcall.callerscreen.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.telecom.TelecomManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.colorcall.callerscreen.utils.PhoneUtils;

import java.lang.ref.WeakReference;

@SuppressLint("OverrideAbstract")
public class NotificationService extends NotificationListenerService {
    @RequiresApi(api = 19)
    private static WeakReference<NotificationService> serviceWeakReference;
    private StatusBarNotification inCallNotification;
    private boolean isListen = false;

    public StatusBarNotification getInCallNotification() {
        return this.inCallNotification;
    }

    public static NotificationService get() {
        WeakReference<NotificationService> weakReference = serviceWeakReference;
        if (weakReference == null || weakReference.get() == null) {
            return null;
        }
        return serviceWeakReference.get();
    }

    public StatusBarNotification[] getActiveNotifications() {
        return super.getActiveNotifications();
    }

    public void onCreate() {
        super.onCreate();
        serviceWeakReference = new WeakReference<>(this);
    }

    @Override
    public void onNotificationPosted(final StatusBarNotification statusBarNotification) {
        super.onNotificationPosted(statusBarNotification);
        Log.e("TAN", "onNotificationPosted: "+statusBarNotification);
        new Thread() {
            public void run() {
                super.run();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (!statusBarNotification.getPackageName().contains("incallui") && !statusBarNotification.getPackageName().contains("dialer"))
                        return;
                    String str = "";
                    TelecomManager telecomManager = getApplicationContext().getSystemService(TelecomManager.class);
                    if (telecomManager != null) {
                        str = telecomManager.getDefaultDialerPackage() + "";
                    }
                    if (!statusBarNotification.getPackageName().contains("incallui") && !statusBarNotification.getPackageName().equals(str)) {
                        return;
                    }
                    if (NotificationService.this.isListen) {
                        new Thread() {
                            public void run() {
                                super.run();
                                PhoneUtils.get(getApplicationContext()).getPhoneFromNotificationListen(statusBarNotification);
                            }
                        }.start();
                        return;
                    }
                    NotificationService.this.inCallNotification = statusBarNotification;
                }
            }
        }.start();
    }

    public void stopListenColorCall() {
        getApplicationContext().stopService(new Intent(getApplicationContext(), NotificationService.class));
        this.isListen = false;
    }

    @RequiresApi(api = 28)
    public void startListenColorCall() {
        this.isListen = true;
    }
}
