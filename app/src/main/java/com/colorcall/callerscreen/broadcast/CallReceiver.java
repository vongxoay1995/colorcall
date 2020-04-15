package com.colorcall.callerscreen.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.service.CallService;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
public class CallReceiver extends BroadcastReceiver {
    public String phoneNumber;
    public final int TYPE_END_CALL = 0;
    public final int TYPE_IN_CALL = 2;
    public final int TYPE_RINGGING_CALL = 1;
    public static int lastStateCall = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
            String state = intent.getExtras().getString("state");
            String number = intent.getExtras().getString("incoming_number");
            Log.e("TAN", "onReceive: "+number);
//            if (!intent.getExtras().containsKey(TelephonyManager.EXTRA_INCOMING_NUMBER)) {
//               return;
//            }
            int stateType = 0;
            if (state != null && state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                stateType = TYPE_END_CALL;
            } else if (state != null && state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                stateType = TYPE_IN_CALL;
            } else if (state != null && state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                stateType = TYPE_RINGGING_CALL;
            }
            onCallStateChanged(context, stateType, number);
        }
    }

    private void onCallStateChanged(Context context, int state, String number) {
        if (lastStateCall != state) {
            switch (state) {
                case TYPE_RINGGING_CALL:
                    phoneNumber = number;
                    onIncommingCall(context, number);
                    break;
            }
            lastStateCall = state;
        }
    }
    private void onIncommingCall(Context context, String number) {
        if (AppUtils.checkDrawOverlay(context) && HawkHelper.isEnableColorCall()) {
            Intent intent=new Intent(context,CallService.class);
            intent.putExtra(Constant.PHONE_NUMBER,number);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }
}
