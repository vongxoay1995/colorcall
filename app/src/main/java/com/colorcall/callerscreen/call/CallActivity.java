package com.colorcall.callerscreen.call;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.telephony.ITelephony;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.custom.CustomVideoView;
import com.colorcall.callerscreen.model.Background;
import com.colorcall.callerscreen.service.AcceptCallActivity;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;

import java.lang.reflect.Method;

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
    ImageView imgBgCall;
    @BindView(R.id.btnAccept)
    ImageView imgAccept;
    @BindView(R.id.btnReject)
    ImageView imgReject;
    @BindView(R.id.vdo_background_call)
    CustomVideoView vdoBgCall;
    private String phoneNumber = "";
    public boolean isDisable;
    public boolean isUnknow;
    private Bitmap bmpAvatar;
    private String name;
    private int typeBgCall;

    private Background backgroundSelect;
    private TelephonyManager telephonyManager;
    private ITelephony telephonyService;
    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.colorcall.endCall")){
                finish();
            }
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
        try {
            requestWindowFeature(1);
        } catch (AndroidRuntimeException unused) {
        }
        setContentView(R.layout.activity_call);
        ButterKnife.bind(this);
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
    private void showViewCall() {
        phoneNumber = getIntent().getStringExtra(Constant.PHONE_NUMBER);
        if(phoneNumber==null){
            isUnknow = true;
        }
        backgroundSelect = HawkHelper.getBackgroundSelect();
        if (backgroundSelect != null) {
            typeBgCall = backgroundSelect.getType();
            try {
                name = AppUtils.getContactName(getApplicationContext(), String.valueOf(phoneNumber));
                txtName.setText(name);
                if (name.equals("")) {
                    txtName.setVisibility(View.GONE);
                } else {
                    txtName.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            bmpAvatar = AppUtils.getContactPhoto(getApplicationContext(), String.valueOf(phoneNumber));
            imgAvatar.setImageBitmap(bmpAvatar);
            if (isUnknow) {
                txtName.setText("Unknow contact");
                txtName.setVisibility(View.VISIBLE);
                txtPhoneNumber.setVisibility(View.GONE);
            }
            txtPhoneNumber.setText(String.valueOf(phoneNumber));
            vdoBgCall.setVisibility(View.VISIBLE);
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
            finish();
            e.printStackTrace();
        }
    }
    private void listener() {
        imgAccept.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TelecomManager tm = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                assert tm != null;
                tm.acceptRingingCall();
            } else {
                Intent intent = new Intent(getApplicationContext(), AcceptCallActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                getApplicationContext().startActivity(intent);
            }
            finish();
            isDisable = true;
        });

        imgReject.setOnClickListener(v -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    TelecomManager tm = (TelecomManager) getApplicationContext().getSystemService(Context.TELECOM_SERVICE);
                    if (tm != null) {
                        tm.endCall();
                        finish();
                    }
                } else {
                    telephonyService.endCall();
                    finish();
                }
                isDisable = true;
            } catch (Exception e) {
               finish();
            }
        });
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
            finish();
            return true;
        });
        vdoBgCall.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            vdoBgCall.start();
        });
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

}