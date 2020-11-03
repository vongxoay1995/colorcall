package com.colorcall.callerscreen.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.internal.telephony.ITelephony;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.FirebaseAnalystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.broadcast.CallReceiver;
import com.colorcall.callerscreen.call.CallActivity;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.custom.CustomVideoView;
import com.colorcall.callerscreen.model.Background;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.NotificationUtil;

import java.lang.reflect.Method;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallService extends Service {
    private String phoneNumber = "";
    private View viewCall;
    private CustomVideoView vdoBgCall;
    private ImageView imgBgCall, imgAccept, imgReject;
    private CircleImageView imgAvatar;
    private TextView txtName, txtPhoneNumber;
    private int typeBgCall;
    private Background backgroundSelect;
    private TelephonyManager telephonyManager;
    private ITelephony telephonyService;
    private static final int ID_NOTIFICATION = 1;
    public boolean isDisable;
    private Bitmap bmpAvatar;
    private WindowManager mWindowManager;
    private FirebaseAnalystic firebaseAnalystic;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(ID_NOTIFICATION, NotificationUtil.initNotificationAndroidQ(this));
        }else
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(ID_NOTIFICATION, NotificationUtil.initNotificationAndroidO(this));
        }
        if (intent != null && intent.getExtras() != null) {
            try {
                phoneNumber = intent.getStringExtra(Constant.PHONE_NUMBER);
            } catch (Exception e) {
                e.getMessage();
            }
            if (phoneNumber == null) {
                phoneNumber = " ";
            }
            phoneNumber = phoneNumber.replaceAll(" ", "").replaceAll("-", "");
            //showViewCallColor();
            Intent intent2 = new Intent(getApplicationContext(), CallActivity.class);
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent2.putExtra(Constant.PHONE_NUMBER, phoneNumber);
            startActivity(intent2);

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
      /*
        removeUI();*/
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        NotificationUtil.hideNotification(this);
       // firebaseAnalystic.trackEvent(ManagerEvent.callDismiss());
        super.onDestroy();
    }

    public void removeUI() {
        try {
            if (viewCall != null && mWindowManager != null) {
                mWindowManager.removeView(viewCall);
            }
        } catch (Exception e) {
        }
    }

    public void showViewCallColor() {
        backgroundSelect = HawkHelper.getBackgroundSelect();
        if (backgroundSelect != null) {
            typeBgCall = backgroundSelect.getType();
            int LAYOUT_TYPE;
            String name;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            }
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            final WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, LAYOUT_TYPE, 40371457,/*, WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,*/
                    PixelFormat.TRANSLUCENT);
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            try {
                if (inflater != null) {
                    viewCall = inflater.inflate(R.layout.layout_call_color, null);
                }
                txtPhoneNumber = viewCall.findViewById(R.id.txtPhone);
                imgAvatar = viewCall.findViewById(R.id.profile_image);
                txtName = viewCall.findViewById(R.id.txtName);
                try {
                    name = AppUtils.getContactName(getApplicationContext(), String.valueOf(phoneNumber));
                    bmpAvatar = AppUtils.getContactPhoto(getApplicationContext(), String.valueOf(phoneNumber));
                    txtName.setText(name);
                    imgAvatar.setImageBitmap(bmpAvatar);
                    if (name.equals("")) {
                        txtName.setText("Unknow contact");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                txtPhoneNumber.setText(String.valueOf(phoneNumber));
                imgAccept = viewCall.findViewById(R.id.btnAccept);
                imgReject = viewCall.findViewById(R.id.btnReject);
                imgBgCall = viewCall.findViewById(R.id.img_background_call);
                vdoBgCall = viewCall.findViewById(R.id.vdo_background_call);
                vdoBgCall.setVisibility(View.VISIBLE);
                checkTypeCall(typeBgCall);
              /*  viewCall.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_VISIBLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );*/
                new Handler().postDelayed(this::startAnimation, 400);
                mWindowManager.addView(viewCall, mLayoutParams);
                firebaseAnalystic.trackEvent(ManagerEvent.callshow());
                handlingCallState();
                listener();
            } catch (Exception e) {
                stopSelf();
            }
        }
    }


    private void listener() {
        imgAccept.setOnClickListener(v -> {
            firebaseAnalystic.trackEvent(ManagerEvent.callAcceptCall());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TelecomManager tm = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (tm != null) {
                    tm.acceptRingingCall();
                }
            } else {
                Intent intent = new Intent(getApplicationContext(), AcceptCallActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                getApplicationContext().startActivity(intent);
            }
            isDisable = true;
            stopSelf();
        });

        imgReject.setOnClickListener(v -> {
            firebaseAnalystic.trackEvent(ManagerEvent.callRejectCall());
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    TelecomManager tm = (TelecomManager) getApplicationContext().getSystemService(Context.TELECOM_SERVICE);
                    if (tm != null) {
                        tm.endCall();
                    }
                } else {
                    telephonyService.endCall();
                }
                isDisable = true;
                stopSelf();
            } catch (Exception e) {
                stopSelf();
            }
        });
    }

    private void handlingCallState() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Class clazz;
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                clazz = Class.forName(telephonyManager.getClass().getName());
                Method method = clazz.getDeclaredMethod("getITelephony");
                method.setAccessible(true);
                telephonyService = (ITelephony) method.invoke(telephonyManager);
                firebaseAnalystic.trackEvent(ManagerEvent.callSmallAndroidP());
            }else{
                firebaseAnalystic.trackEvent(ManagerEvent.callBiggerAndroidP());
            }
        } catch (Exception e) {
            stopSelf();
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
            stopSelf();
            return true;
        });
        vdoBgCall.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            vdoBgCall.start();
        });

    }

    @Override
    public void onCreate() {
        firebaseAnalystic = FirebaseAnalystic.getInstance(this);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.colorcall.endCall");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
        super.onCreate();
    }

    public void startAnimation() {
        Animation anim8 = AnimationUtils.loadAnimation(this, R.anim.anm_accept_call);
        imgAccept.startAnimation(anim8);
    }

    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.colorcall.endCall")) {
                stopSelf();
            }
        }
    };
}
