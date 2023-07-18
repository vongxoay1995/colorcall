package com.colorcall.callerscreen.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.main.MainActivity;

public class PhoneService extends Service {
    public TelephonyManager telephony;
    public PhoneStateListener phoneStateListener;
    public TelephonyCallback callStateListener;
    public static String number;

    public static void startService(Context context) {
        ContextCompat.startForegroundService(context, new Intent(context, PhoneService.class));
    }

    public static void stopService(Context context) {
        context.stopService(new Intent(context, PhoneService.class));
    }

    public static void setNumberPhone(String numberPhone) {
        number = numberPhone;
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(new NotificationChannel("channel_call", "notification", NotificationManager.IMPORTANCE_LOW));
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_call");
        builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), Build.VERSION.SDK_INT >= 31 ? PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT));
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(R.string.notify_msg_foreground));
        builder.setSmallIcon((int) R.drawable.icon);
        startForeground(1, builder.build());
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        this.telephony = telephonyManager;
        registerCallStateListener(telephonyManager);
    }

    private void registerCallStateListener(TelephonyManager telephonyManager) {
        if (!callStateListenerRegistered) {
            Log.e("TAN", "registerCallStateListener: ");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Log.e("TAN", "registerCallStateListener: 0");
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    Log.e("TAN", "registerCallStateListener: 1");
                    this.callStateListener = new CallState(this);
                    telephonyManager.registerTelephonyCallback(getMainExecutor(), callStateListener);
                    callStateListenerRegistered = true;
                }
            } else {
                Log.e("TAN", "registerCallStateListener: 2222");
                this.phoneStateListener = new PhoneState(this);
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                callStateListenerRegistered = true;
            }
        }
    }


    private boolean callStateListenerRegistered = false;

    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            this.telephony.unregisterTelephonyCallback(callStateListener);
        } else {
            this.telephony.listen(this.phoneStateListener, 0);
        }
        this.telephony = null;
        this.phoneStateListener = null;
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        return START_STICKY;
    }

}
