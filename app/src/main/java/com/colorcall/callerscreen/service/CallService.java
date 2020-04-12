package com.colorcall.callerscreen.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.internal.telephony.ITelephony;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.custom.CustomVideoView;
import com.colorcall.callerscreen.model.Background;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;

import java.lang.reflect.Method;

public class CallService extends Service {
    private String phoneNumber = "";
    private View viewCall;
    private CustomVideoView vdoBgCall;
    private ImageView imgBgCall, imgAccept, imgReject;
    private TextView txtName, txtPhoneNumber;
    private int typeBgCall;
    private Background backgroundSelect;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private ITelephony telephonyService;
    private static final int ID_NOTIFICATION = 1;
    public static String CHANNEL = "Color_Call_channel";
    private static final String CHANNEL_ID = "ColorCall";
    public boolean isDisable;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null&&intent.getExtras()!=null){
            phoneNumber = intent.getStringExtra(Constant.PHONE_NUMBER);
            if(phoneNumber==null){
                phoneNumber = "0000-0000-0000";
            }
            showViewCallColor();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void showViewCallColor() {
        backgroundSelect = HawkHelper.getBackgroundSelect();
        if (backgroundSelect != null) {
            typeBgCall = backgroundSelect.getType();
            int LAYOUT_TYPE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            }
            final WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, LAYOUT_TYPE, WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    PixelFormat.TRANSLUCENT);
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            try {
                if (inflater != null) {
                    viewCall = inflater.inflate(R.layout.layout_call_color, null);
                }
                txtPhoneNumber = viewCall.findViewById(R.id.txtPhone);
                txtName = viewCall.findViewById(R.id.txtName);
                txtName.setText(AppUtils.getContactName(getApplicationContext(),String.valueOf(phoneNumber)));
                txtPhoneNumber.setText(String.valueOf(phoneNumber));
                imgAccept = viewCall.findViewById(R.id.btnAccept);
                imgReject = viewCall.findViewById(R.id.btnReject);
                imgBgCall = viewCall.findViewById(R.id.img_background_call);
                vdoBgCall = viewCall.findViewById(R.id.vdo_background_call);
                vdoBgCall.setVisibility(View.VISIBLE);
                checkTypeCall(typeBgCall);
                viewCall.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_VISIBLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
                new Handler().postDelayed(() -> startAnimation(), 400);
                ((WindowManager) getSystemService(WINDOW_SERVICE)).addView(viewCall, mLayoutParams);
                handlingCallState();
                listener();
            } catch (Exception e) {
                finishService();
            }
        }
    }

    private void finishService() {
        viewCall.setVisibility(View.GONE);
        WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (mWindowManager != null && viewCall.getWindowToken() != null) {
            mWindowManager.removeViewImmediate(viewCall);
        }
        if (telephonyManager != null)
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        stopSelf();
    }

    private void listener() {
        imgAccept.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TelecomManager tm = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                tm.acceptRingingCall();
                isDisable = true;
            } else {
                Intent intent = new Intent(getApplicationContext(), AcceptCallActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                getApplicationContext().startActivity(intent);
                isDisable = true;
            }
        });

        imgReject.setOnClickListener(v -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    TelecomManager tm = (TelecomManager) getApplicationContext().getSystemService(Context.TELECOM_SERVICE);

                    if (tm != null) {
                        boolean success = tm.endCall();
                    }
                }else {
                    telephonyService.endCall();
                }
                isDisable = true;
            } catch (RemoteException e) {
                finishService();
                e.printStackTrace();
            }
            catch (Exception e){
                finishService();
            }
        });
    }

    private void handlingCallState() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Class clazz = null;
        try {
            clazz = Class.forName(telephonyManager.getClass().getName());
            Method method = clazz.getDeclaredMethod("getITelephony");
            method.setAccessible(true);
            telephonyService = (ITelephony) method.invoke(telephonyManager);

            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    super.onCallStateChanged(state, incomingNumber);
                    if (state == TelephonyManager.CALL_STATE_OFFHOOK || state == TelephonyManager.CALL_STATE_IDLE) {
                        viewCall.setVisibility(View.GONE);
                    }
                }
            };
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        } catch (Exception e) {
            finishService();
            e.printStackTrace();
        }
    }

    private void checkTypeCall(int typeBgCall) {
        switch (typeBgCall) {
            case Constant.TYPE_VIDEO:
                handlingBgCallVideo();
                break;
            case Constant.TYPE_IMAGE:
                handlingBgCallImage();
                break;
        }
    }

    private void handlingBgCallImage() {
        imgBgCall.setVisibility(View.VISIBLE);
        if (backgroundSelect.getPathItem().contains("storage")) {
            Glide.with(getApplicationContext())
                    .load(backgroundSelect.getPathItem())
                    .apply(RequestOptions.placeholderOf(R.drawable.bg_gradient_green))
                    .into(imgBgCall);
        }
        vdoBgCall.setVisibility(View.GONE);
    }

    private void handlingBgCallVideo() {
        imgBgCall.setVisibility(View.GONE);
        vdoBgCall.setVisibility(View.VISIBLE);
        if (backgroundSelect.getPathItem().contains("storage")) {
            vdoBgCall.setVideoPath(backgroundSelect.getPathItem());
        } else {
            String uriPath = "android.resource://" + getPackageName() + backgroundSelect.getPathItem();
            vdoBgCall.setVideoURI(Uri.parse(uriPath));
        }
        vdoBgCall.setOnErrorListener((mp, what, extra) -> {
            finishService();
            return true;
        });
        vdoBgCall.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            vdoBgCall.start();
        });

    }

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(ID_NOTIFICATION, initNotificationAndroidO());
        } else {
            startForeground(ID_NOTIFICATION, new Notification());
        }
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification initNotificationAndroidO() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .build();
        return notification;
    }

    public void startAnimation() {
        Animation anim8 = AnimationUtils.loadAnimation(this, R.anim.anm_accept_call);
        imgAccept.startAnimation(anim8);
    }
}
