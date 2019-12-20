package com.htn.colorcall.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.htn.colorcall.constan.Constant;
import com.htn.colorcall.service.CallService;
import com.htn.colorcall.utils.AppUtils;
import com.htn.colorcall.utils.HawkHelper;
import com.orhanobut.hawk.Hawk;

import java.util.Date;

public class CallReceiver extends BroadcastReceiver {
    public String phoneNumber;
    public Date startTime;
    public final int TYPE_END_CALL = 0;
    public final int TYPE_IN_CALL = 2;
    public final int TYPE_RINGGING_CALL = 1;
    public static boolean isCommingCall;
    public static int lastStateCall = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
            String state = intent.getExtras().getString("state");
            String number = intent.getExtras().getString("incoming_number");
            Log.e("TAN", "onReceive: " + state + "--" + number);
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
        Log.e("TAN", "onCallStateChanged: " + state + "--" + lastStateCall + "--" + isCommingCall);
        if (lastStateCall != state) {
            switch (state) {
                case TYPE_END_CALL:
                    if (lastStateCall != TYPE_RINGGING_CALL) {
                        if (!isCommingCall) {
                            onOutgoingCallEnded();
                            break;
                        } else {
                            onIncomingCallEnded();
                            break;
                        }
                    }
                    onEndCall();
                    break;
                case TYPE_RINGGING_CALL:
                    isCommingCall = true;
                    startTime = new Date();
                    phoneNumber = number;
                    onIncommingCall(context, number);
                    break;
                case TYPE_IN_CALL:
                    if (lastStateCall != TYPE_RINGGING_CALL) {
                        isCommingCall = false;
                        startTime = new Date();
                        onOutgoingCallStart();
                        break;
                    }
                    startTime = new Date();
                    onInCalled();
                    break;
            }
            lastStateCall = state;
        }
    }

    private void onInCalled() {
        Log.e("TAN", "onInCalled: " + phoneNumber);
    }

    private void onOutgoingCallStart() {
        Log.e("TAN", "onOutgoingCallStart: " + phoneNumber);
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

    private void onEndCall() {
        Log.e("TAN", "onEndCall: " + phoneNumber);
    }

    private void onIncomingCallEnded() {
        Log.e("TAN", "onIncomingCallEnded: " + phoneNumber);
    }

    private void onOutgoingCallEnded() {
        Log.e("TAN", "onOutgoingCallEnded: " + phoneNumber);
    }
}
