package com.colorcall.callerscreen.call;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.internal.telephony.ITelephony;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.custom.FullScreenVideoView;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.database.Contact;
import com.colorcall.callerscreen.database.ContactDao;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.model.ContactRetrieve;
import com.colorcall.callerscreen.service.AcceptCallActivity;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.DynamicImageView;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class CallActivity extends AppCompatActivity {
    @BindView(R.id.profile_image)
    CircleImageView imgAvatar;
    @BindView(R.id.txtName)
    TextView txtName;
    @BindView(R.id.txtPhone)
    TextView txtPhoneNumber;
    @BindView(R.id.img_background_call)
    DynamicImageView imgBgCall;
    @BindView(R.id.btnAccept)
    ImageView imgAccept;
    @BindView(R.id.btnReject)
    ImageView imgReject;
    @BindView(R.id.vdo_background_call)
    FullScreenVideoView vdoBgCall;
    public boolean isDisable;
    private String contactId="";
    private Background backgroundSelect,back_ground_contact;
    private ITelephony telephonyService;
    private Analystic analystic;
    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Thread t = new Thread(){
                public void run(){
                    if (intent.getAction().equals("com.colorcall.endCall")) {
                        finish();
                    }
                }
            };
            t.start();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(1024, 1024);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_call);
        ButterKnife.bind(this);
        analystic = Analystic.getInstance(this);
        analystic.trackEvent(ManagerEvent.callshow());
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.colorcall.endCall");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
        showViewCall();
    }

    protected void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
    // crash fire base if not permission contact
    private void showViewCall() {
        String phoneNumber = getIntent().getStringExtra(Constant.PHONE_NUMBER);
        backgroundSelect = HawkHelper.getBackgroundSelect();
        if (backgroundSelect != null) {
            int typeBgCall = backgroundSelect.getType();
            try {
                ContactRetrieve contactRetrieve = AppUtils.getContactName(getApplicationContext(), String.valueOf(phoneNumber));
                String name = contactRetrieve.getName();
                contactId = contactRetrieve.getContact_id();
                txtName.setText(name);
                if (name.equals("")) {
                    txtName.setText(getString(R.string.unknowContact));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Bitmap bmpAvatar = AppUtils.getContactPhoto(getApplicationContext(), String.valueOf(phoneNumber));
            imgAvatar.setImageBitmap(bmpAvatar);
            txtPhoneNumber.setText(String.valueOf(phoneNumber));
            vdoBgCall.setVisibility(View.VISIBLE);
            List<Contact> listQueryContactID = DataManager.query().getContactDao().queryBuilder()
                    .where(ContactDao.Properties.Contact_id.eq(contactId))
                    .list();
            if(listQueryContactID.size()>0){
                Contact mContact = listQueryContactID.get(0);
                back_ground_contact = new Gson().fromJson(mContact.getBackground(),Background.class);
            }
            if(back_ground_contact!=null){
                backgroundSelect = back_ground_contact;
                typeBgCall = back_ground_contact.getType();
            }
            checkTypeCall(typeBgCall);
            new Handler().postDelayed(this::startAnimation, 400);
            handlingCallState();
            listener();
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

    public void startAnimation() {
        Animation anim8 = AnimationUtils.loadAnimation(this, R.anim.anm_accept_call);
        imgAccept.startAnimation(anim8);
    }

    private void handlingCallState() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Class clazz;
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                clazz = Class.forName(telephonyManager.getClass().getName());
                Method method = clazz.getDeclaredMethod("getITelephony");
                method.setAccessible(true);
                telephonyService = (ITelephony) method.invoke(telephonyManager);
            }
        } catch (Exception e) {
            finish();
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
            finish();
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
                finish();
            } catch (Exception e) {
                finish();
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
            sPath = "android.resource://" + getPackageName() + backgroundSelect.getPathItem();
        }
        vdoBgCall.setVideoURI(Uri.parse(sPath));
        vdoBgCall.setOnErrorListener((mp, what, extra) -> {
            analystic.trackEvent(ManagerEvent.callVideoViewError(what, extra));
            finish();
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
}