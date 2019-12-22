package com.colorcall.callerscreen.service;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationService extends NotificationListenerService {
    public StatusBarNotification[] getActiveNotifications() {
        StatusBarNotification[] statusBarNotificationArr = null;
        try {
            statusBarNotificationArr = super.getActiveNotifications();
        } catch (SecurityException e) {
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return statusBarNotificationArr == null ? new StatusBarNotification[0] : statusBarNotificationArr;
    }

    public void onCreate() {
        super.onCreate();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onListenerConnected() {
    }

    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
    }

    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
    }
}
