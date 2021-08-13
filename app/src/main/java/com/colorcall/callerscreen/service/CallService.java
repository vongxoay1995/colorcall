package com.colorcall.callerscreen.service;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.internal.telephony.ITelephony;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.call.CallActivity;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.custom.TextureVideoView;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.database.Contact;
import com.colorcall.callerscreen.database.ContactDao;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.model.ContactRetrieve;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.DynamicImageView;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.NotificationUtil;
import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallService extends Service {
    private String phoneNumber = "";
    private View viewCall;
    private TextureVideoView vdoBgCall;
    private ImageView imgAccept, imgReject, imgExit;
    private CircleImageView imgAvatar;
    private DynamicImageView imgBgCall;
    private TextView txtName, txtPhoneNumber;
    private int typeBgCall;
    private Background backgroundSelect, back_ground_contact;
    private TelephonyManager telephonyManager;
    private ITelephony telephonyService;
    private static final int ID_NOTIFICATION = 1;
    public boolean isDisable;
    private Bitmap bmpAvatar;
    private WindowManager mWindowManager;
    private Analystic analystic;
    private LocalBroadcastManager mLocalBroadcastManager;
    private String name;
    public WindowManager.LayoutParams mLayoutParams;
    private String contactId = "";
    private Contact mContact;
    private LayoutInflater inflater;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(ID_NOTIFICATION, NotificationUtil.initNotificationAndroidQ(this));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(ID_NOTIFICATION, NotificationUtil.initNotificationAndroidO(this));
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
           /* if (HawkHelper.isScreenCall() == 1) {
                showViewCallColor();
            } else {
                Intent intent2 = new Intent(getApplicationContext(), CallActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.putExtra(Constant.PHONE_NUMBER, phoneNumber);
                startActivity(intent2);
            }*/
            checkDevice();
        }
        return super.onStartCommand(intent, flags, startId);
    }
    private void checkDevice() {
        if (Build.MANUFACTURER != null&&(Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")
                || Build.MANUFACTURER.equalsIgnoreCase("realme"))
                ||Build.MANUFACTURER.contains("INFINIX")) {
            showViewCallColor();
        }else{
            Intent intent2 = new Intent(getApplicationContext(), CallActivity.class);
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent2.putExtra(Constant.PHONE_NUMBER, phoneNumber);
            startActivity(intent2);
        }
    }

    @Override
    public void onDestroy() {
        removeUI();
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        NotificationUtil.hideNotification(this);
        super.onDestroy();
    }

    public void removeUI() {
        try {
            if (viewCall != null && mWindowManager != null) {
                mWindowManager.removeView(viewCall);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showViewCallColor() {
        backgroundSelect = HawkHelper.getBackgroundSelect();
        try {
            if (backgroundSelect != null) {
                initLayoutColor();
                if (inflater != null) {
                    viewCall = inflater.inflate(R.layout.layout_call_color, null);
                }
                txtPhoneNumber = viewCall.findViewById(R.id.txtPhone);
                imgAvatar = viewCall.findViewById(R.id.profile_image);
                txtName = viewCall.findViewById(R.id.txtName);
                imgAccept = viewCall.findViewById(R.id.btnAccept);
                imgReject = viewCall.findViewById(R.id.btnReject);
                imgExit = viewCall.findViewById(R.id.imgExit);
                Glide.with(this).load(R.drawable.ic_exit).into(imgExit);
                imgExit.setOnClickListener(v -> {
                    if (viewCall != null) {
                        viewCall.setVisibility(View.GONE);
                    }
                    removeUI();
                    stopCallService();
                });
                imgBgCall = viewCall.findViewById(R.id.img_background_call);
                vdoBgCall = viewCall.findViewById(R.id.vdo_background_call);
                typeBgCall = backgroundSelect.getType();
                try {
                    ContactRetrieve contactRetrieve = AppUtils.getContactName(getApplicationContext(), String.valueOf(phoneNumber));
                    name = contactRetrieve.getName();
                    contactId = contactRetrieve.getContact_id();
                    txtName.setText(name);
                    if (name.equals("")) {
                        txtName.setText(getString(R.string.unknowContact));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                bmpAvatar = AppUtils.getContactPhoto(getApplicationContext(), String.valueOf(phoneNumber));
                imgAvatar.setImageBitmap(bmpAvatar);
                txtPhoneNumber.setText(String.valueOf(phoneNumber));
                vdoBgCall.setVisibility(View.VISIBLE);
                viewCall.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                mWindowManager.addView(viewCall, mLayoutParams);
                analystic.trackEvent(ManagerEvent.callshow());
                List<Contact> listQueryContactID = DataManager.query().getContactDao().queryBuilder()
                        .where(ContactDao.Properties.Contact_id.eq(contactId))
                        .list();
                if (listQueryContactID.size() > 0) {
                    mContact = listQueryContactID.get(0);
                    back_ground_contact = new Gson().fromJson(mContact.getBackground(), Background.class);
                }
                if (back_ground_contact != null) {
                    backgroundSelect = back_ground_contact;
                    typeBgCall = back_ground_contact.getType();
                }
                checkTypeCall(typeBgCall);
                new Handler().postDelayed(this::startAnimation, 400);
                handlingCallState();
                listener();
            }
        } catch (Exception e) {
            stopCallService();
        }
    }

    private void initLayoutColor() {
        int LAYOUT_TYPE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        }
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
//            final WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT, LAYOUT_TYPE, 40371457,
//                    /*WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
//                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,*/
//                    PixelFormat.TRANSLUCENT);
        mLayoutParams.type = LAYOUT_TYPE;
        mLayoutParams.format = -2;
        mLayoutParams.flags = 524584;
        mLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mLayoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        mLayoutParams.windowAnimations = 16973826;
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void onCreate() {
        analystic = Analystic.getInstance(this);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.colorcall.endCall");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
        super.onCreate();
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.colorcall.endCall")) {
                stopCallService();
            }
        }
    };

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

    public void startAnimation() {
        Animation anim8 = AnimationUtils.loadAnimation(this, R.anim.anm_accept_call);
        imgAccept.startAnimation(anim8);
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
            }
        } catch (Exception e) {
            stopCallService();
            e.printStackTrace();
        }
    }

    private void listener() {
        imgAccept.setOnClickListener(v -> {
            analystic.trackEvent(ManagerEvent.callAcceptCall());
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
            stopCallService();
        });

        imgReject.setOnClickListener(v -> {
            analystic.trackEvent(ManagerEvent.callRejectCall());
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
                stopCallService();
            } catch (Exception e) {
                stopCallService();
            }
        });
    }

    private void handlingBgCallVideo() {
        String sPath;
        imgBgCall.setVisibility(View.GONE);
        vdoBgCall.setVisibility(View.VISIBLE);
        if (backgroundSelect.getPathItem().contains("storage") || backgroundSelect.getPathItem().contains("/data/data") || backgroundSelect.getPathItem().contains("data/user/")) {
            sPath = backgroundSelect.getPathItem();
        } else {
            String uriPath = "android.resource://" + getPackageName() + backgroundSelect.getPathItem();
            sPath = uriPath;
        }
        vdoBgCall.setVideoURI(Uri.parse(sPath));
        vdoBgCall.setOnErrorListener((mp, what, extra) -> {
            analystic.trackEvent(ManagerEvent.callVideoViewError(what, extra));
            stopCallService();
            return true;
        });
        vdoBgCall.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0.0f, 0.0f);
            vdoBgCall.start();
        });
    }

    private void handlingBgCallImage() {
        imgBgCall.setVisibility(View.VISIBLE);
        String sPathThumb;
        if (backgroundSelect.getPathItem().contains("default") && backgroundSelect.getPathItem().contains("thumbDefault")) {
            sPathThumb = "file:///android_asset/" + backgroundSelect.getPathItem();
        } else {
            sPathThumb = backgroundSelect.getPathItem();
        }
        Glide.with(getApplicationContext())
                .load(sPathThumb)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .thumbnail(0.1f)
                .into(imgBgCall);
        vdoBgCall.setVisibility(View.GONE);
    }

    public void stopCallService() {
        getApplicationContext().stopService(new Intent(getApplicationContext(), CallService.class));
    }
}
