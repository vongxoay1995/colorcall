package com.colorcall.callerscreen.service;

import static com.colorcall.callerscreen.constan.Constant.TYPE_PROMPT;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.telephony.TelephonyCallback;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.custom.IncomingCallView;
import com.colorcall.callerscreen.promt.PermissionOverLayActivity;

@RequiresApi(api = Build.VERSION_CODES.S)
public class CallState extends TelephonyCallback implements TelephonyCallback.CallStateListener {
    public Context context;
    public Handler handler = new Handler();
    public AudioManager audio;
    public int ringerMode = -1;
    IncomingCallView incomingCallView;
    int state = 0;

    public CallState(Context context) {
        this.context = context;
        this.audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }
    @Override
    public void onCallStateChanged(int i) {
        state = i;
        /*Log.e("TAN", "onCallStateChanged: " + i + "##" + str);
        if (str==null||str.equals("")){
            Log.e("TAN", "onCallStateChanged: number k co ");

            str = PhoneStateService.number;
            Log.e("TAN", "onCallStateChanged: number co r "+str);

        }*/
        showViewCall();
    }
    public void grantPermissionActivity() {
        Intent intent = new Intent(this.context, PermissionOverLayActivity.class);
        intent.putExtra(TYPE_PROMPT, 1);
        this.context.startActivity(intent);
    }


    public final void setRinger(int mode) {
        try {
            this.audio.setRingerMode(mode);
        } catch (SecurityException unused) {
            NotificationManager notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 24 && notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
                this.context.startActivity(new Intent("android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS"));
                this.handler.postDelayed(() -> grantPermissionActivity(), 100);
            }
        }
    }

    public void release() {
        if (this.incomingCallView != null) {
            int i = this.ringerMode;
            if (i != -1) {
                setRinger(i);
                this.ringerMode = -1;
            }
            this.incomingCallView.release();
            this.incomingCallView = null;
        }
    }
    public void showViewCall() {
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
                this.incomingCallView.callState = this;
                this.incomingCallView.initData();
                this.incomingCallView.setNumberPhone("094");
                if (this.ringerMode == -1) {
                    this.ringerMode = this.audio.getRingerMode();
                    setRinger(1);
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
