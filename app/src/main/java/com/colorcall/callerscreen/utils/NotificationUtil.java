package com.colorcall.callerscreen.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.colorcall.callerscreen.R;

public class NotificationUtil {
    private static final int ID_NOTIFICATION = 1;
    public static String CHANNEL = "Color_Call_channel";
    private static final String CHANNEL_ID = "ColorCall";

    public NotificationUtil() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Notification initNotificationAndroidO(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .build();
        return notification;
    }
    public static void hideNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(ID_NOTIFICATION);
        }
    }
}
