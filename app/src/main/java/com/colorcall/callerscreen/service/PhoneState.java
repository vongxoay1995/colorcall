package com.colorcall.callerscreen.service;

import static com.colorcall.callerscreen.constan.Constant.TYPE_PROMPT;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.util.Log;
import android.view.View;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.custom.IncomingCallView;
import com.colorcall.callerscreen.database.Contact;
import com.colorcall.callerscreen.promt.PermissionOverLayActivity;
import com.colorcall.callerscreen.utils.PhoneUtils;

public class PhoneState extends PhoneStateListener implements PhoneUtils.PhoneListener {
    public Context context;
    public Handler handler = new Handler();
    //public RingIncomingView c;
    public AudioManager audio;
    public int e = -1;
    IncomingCallView incomingCallView;
    public boolean isFirstRun;

    public PhoneState(Context context) {
        this.context = context;
        this.audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void grantPermissionActivity() {
        Intent intent = new Intent(this.context, PermissionOverLayActivity.class);
        intent.putExtra(TYPE_PROMPT, 1);
        this.context.startActivity(intent);
    }


    public final void a(int i) {
        try {
            this.audio.setRingerMode(i);
        } catch (SecurityException unused) {
            NotificationManager notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 24 && notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
                this.context.startActivity(new Intent("android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS"));
                this.handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        grantPermissionActivity();
                    }
                }, 100);
            }
        }
    }

    public void release() {
        if (this.incomingCallView != null) {
            int i = this.e;
            if (i != -1) {
                a(i);
                this.e = -1;
            }
            this.incomingCallView.release();
            this.incomingCallView = null;
        }
    }

    public void onCallStateChanged(int i, String str) {
        Contact contactBean;
        super.onCallStateChanged(i, str);
        state = i;
        Log.e("TAN", "onCallStateChanged: " + i + "##" + str);
        showViewCall("094");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PhoneUtils.get(context).getNumberPhoneWhenNull(PhoneState.this);
        }
    }

    int state = 0;

    @Override
    public void getNumPhone(String str) {
        Log.e("TAN", "getNumPhone: "+isFirstRun);
        if(!isFirstRun){
            isFirstRun = true;
            if (incomingCallView!=null){
                incomingCallView.setNumberPhone(str);
            }
            Log.e("TAN", "getNumPhone ne: " + str);
       // EventBus.getDefault().postSticky(new PhoneNumber(str));
        }
    }

    public void showViewCall(String str) {
        //if (PermistionUtils.checkPermissionCall(this).a(this.context)) {
        Log.e("TAN", "showViewCall: "+state);
        if (state != 0) {
            if (state == 1) {
                   /* Context context = this.context;

                    RingIncomingView ringIncomingView = (RingIncomingView) View.inflate(this.context, R.layout.layout_ring_incoming, null);
                    this.c = ringIncomingView;
                    ringIncomingView.j = this;
                    ringIncomingView.a(contactBean, chosenTheme);*/

                IncomingCallView incomingCallView =  (IncomingCallView) View.inflate(this.context, R.layout.layout_call_color, null);
                this.incomingCallView = incomingCallView;
                incomingCallView.initData();
                incomingCallView.setNumberPhone(str);
                if (this.e == -1) {
                    this.e = this.audio.getRingerMode();
                    a(1);
                    return;
                }
                return;
            } else if (state != 2) {
                return;
            }
        }
        release();
        // }
    }
}
