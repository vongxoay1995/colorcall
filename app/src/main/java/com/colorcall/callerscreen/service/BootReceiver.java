package com.colorcall.callerscreen.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.colorcall.callerscreen.utils.HawkHelper;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) && HawkHelper.isEnableColorCall()) {
            Log.e("TAN", "onReceive: boot");
            PhoneService.startService(context);
        }
    }
}
