package com.colorcall.callerscreen.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.FlashUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.PhoneUtils;


public class CallReceiver extends BroadcastReceiver implements PhoneUtils.PhoneListener {
    public String phoneNumber;
    public final int TYPE_END_CALL = 0;
    public final int TYPE_IN_CALL = 2;
    public final int TYPE_RINGGING_CALL = 1;
    public static FlashUtils flashUtils;
    public  Intent intentCallService;
    public boolean isBiggerAndroidP;
    public int stateType = 0;
    public Context context;
    public boolean isFirstRun;
    @Override
    public void onReceive(Context context, Intent intent) {
        Thread t = new Thread(){
            public void run(){
                Log.e("TAN", "CallReceiver: ");
                CallReceiver.this.context = context;
                if (intent.getExtras() != null) {
                    String state = intent.getExtras().getString("state");
                    phoneNumber = intent.getExtras().getString("incoming_number");
                    if (phoneNumber == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        isBiggerAndroidP = true;
                       // PhoneUtils.get(context).getNumberPhoneWhenNull(CallReceiver.this);
                    }

                    if (state != null && state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        stateType = TYPE_END_CALL;
                    } else if (state != null && state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        stateType = TYPE_IN_CALL;
                    } else if (state != null && state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        stateType = TYPE_RINGGING_CALL;
                    }
                    onCallStateChanged(context, stateType);
                }
            }
        };
        t.start();
    }

    private void onCallStateChanged(Context context, int state) {
        Log.e("TAN", "onCallStateChanged call receiver: "+state+"##"+phoneNumber);
            switch (state) {
                case TYPE_RINGGING_CALL:
                    if(phoneNumber!=null){
                        onIncommingCall(context, phoneNumber);
                    }
                    if (HawkHelper.isEnableFlash()) {
                        flashUtils = FlashUtils.getInstance(true, context);
                        new Thread(flashUtils).start();
                    }
                    break;
                case TYPE_END_CALL:
                case TYPE_IN_CALL:
                    finishCall();
                    break;
            }
            if (flashUtils != null && flashUtils.isRunning()) {
                flashUtils.stop();
            }
    }

    private void onIncommingCall(Context context, String number) {
        if (AppUtils.checkDrawOverlayApp2(context) && HawkHelper.isEnableColorCall()) {
            Log.e("TAN", "onIncommingCall: aaaa");
            /*intentCallService = new Intent(context, CallService.class);
            intentCallService.putExtra(Constant.PHONE_NUMBER, number);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intentCallService);
            } else {
                context.startService(intentCallService);
            }*/
            //PhoneStateService.startService(context);
           // PhoneService.setNumberPhone(number);
        }
    }

    public void finishCall() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(context);
        localBroadcastManager.sendBroadcast(new Intent("com.colorcall.endCall"));
    }

    @Override
    public void getNumPhone(String phoneNumb) {
        if(!isFirstRun){
            Log.e("TAN", "getNumPhone: "+phoneNumb);
            phoneNumber = phoneNumb;
            isFirstRun = true;
            isBiggerAndroidP = false;
            onCallStateChanged(context, stateType);
        }
    }

}
