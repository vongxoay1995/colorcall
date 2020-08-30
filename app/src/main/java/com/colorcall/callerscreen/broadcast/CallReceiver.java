package com.colorcall.callerscreen.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.service.CallService;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.FlashUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.PhoneUtils;


public class CallReceiver extends BroadcastReceiver implements PhoneUtils.PhoneListener {
    public String phoneNumber;
    public final int TYPE_END_CALL = 0;
    public final int TYPE_IN_CALL = 2;
    public final int TYPE_RINGGING_CALL = 1;
    public static int lastStateCall = 0;
    public static FlashUtils flashUtils;
    public static Intent intentCallService;
    public boolean isBiggerAndroidP;
    public int stateType = 0;
    public Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if (intent.getExtras() != null) {
            String state = intent.getExtras().getString("state");
            phoneNumber = intent.getExtras().getString("incoming_number");
            if (phoneNumber == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                isBiggerAndroidP = true;
                PhoneUtils.get().getNumberPhoneWhenNull(CallReceiver.this);
            }

            if (state != null && state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                stateType = TYPE_END_CALL;
            } else if (state != null && state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                stateType = TYPE_IN_CALL;
            } else if (state != null && state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                stateType = TYPE_RINGGING_CALL;
            }
            if (!isBiggerAndroidP) {
                onCallStateChanged(context, stateType);
            }
        }
    }

    private void onCallStateChanged(Context context, int state) {
        if (lastStateCall != state) {
            switch (state) {
                case TYPE_RINGGING_CALL:
                    onIncommingCall(context, phoneNumber);
                    if (HawkHelper.isEnableFlash()) {
                        flashUtils = FlashUtils.getInstance(true, context);
                        new Thread(flashUtils).start();
                    }
                    break;
                case TYPE_END_CALL:
                    onIdle(context);
                    break;
                case TYPE_IN_CALL:
                    onOffhook(context);
                    break;
            }
            if (flashUtils != null && flashUtils.isRunning()) {
                flashUtils.stop();
            }
            lastStateCall = state;
        }
    }

    private void onIncommingCall(Context context, String number) {
        if (AppUtils.checkDrawOverlay(context) && HawkHelper.isEnableColorCall()) {
            intentCallService = new Intent(context, CallService.class);
            intentCallService.putExtra(Constant.PHONE_NUMBER, number);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intentCallService);
            } else {
                context.startService(intentCallService);
            }
        }
    }

    public void onIdle(Context context) {
        if (intentCallService != null) {
            context.stopService(intentCallService);
        }
    }

    public void onOffhook(Context context) {
        if (intentCallService != null) {
            context.stopService(intentCallService);
        }
    }

    @Override
    public void getNumPhone(String phoneNumb) {
        phoneNumber = phoneNumb;
        isBiggerAndroidP = false;
        onCallStateChanged(context, stateType);
    }
}
