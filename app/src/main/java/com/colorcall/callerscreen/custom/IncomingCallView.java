package com.colorcall.callerscreen.custom;


import static android.content.Context.TELEPHONY_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.database.Contact;
import com.colorcall.callerscreen.database.DatabaseViewModel;
import com.colorcall.callerscreen.databinding.LayoutCallColorBinding;
import com.colorcall.callerscreen.model.ContactRetrieve;
import com.colorcall.callerscreen.service.AcceptCallActivity;
import com.colorcall.callerscreen.service.CallState;
import com.colorcall.callerscreen.service.PhoneState;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.List;

public class IncomingCallView extends RelativeLayout {
    private LayoutCallColorBinding binding;
    private Context context;
    public WindowManager windowManager;
    public String numberPhone = "";
    public WindowManager.LayoutParams windowParams;
    Background backgroundSelect, back_ground_contact;
    private int typeBgCall;
    private String name;
    private String contactId = "";
    private Contact mContact;
    private Bitmap bmpAvatar;
    private Analystic analystic;
    private TelephonyManager telephonyManager;
    private Object telephonyService;
    public PhoneState phoneState;
    public CallState callState;
    private DatabaseViewModel databaseViewModel;

    public IncomingCallView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public IncomingCallView(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public IncomingCallView(@NonNull Context context, @Nullable AttributeSet attributeSet, int i2) {
        super(context, attributeSet, i2);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        analystic = Analystic.getInstance(context);
        binding = LayoutCallColorBinding.inflate(LayoutInflater.from(context), this, true);
        if (context instanceof ViewModelStoreOwner) {
            databaseViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(DatabaseViewModel.class);
        } else {
            throw new IllegalStateException("Context must implement ViewModelStoreOwner");
        }
        if (AppUtils.checkDrawOverlayApp2(context)) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            this.windowParams = layoutParams;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
            }
            layoutParams.format = android.graphics.PixelFormat.TRANSLUCENT;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.screenOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            layoutParams.windowAnimations = android.R.style.Animation_Toast;
            WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            this.windowManager = windowManager;
            windowManager.addView(this, this.windowParams);
        }
    }

    public void setNumberPhone(String numberPhone) {
        this.numberPhone = numberPhone;
        setInforContact();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.e("TAN", "onFinishInflate: ");
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setInforContact() {
        if (numberPhone != null && !numberPhone.equals("")) {
            binding.txtPhone.setText(String.valueOf(numberPhone));
            binding.txtPhone.setVisibility(VISIBLE);
            // Move contact lookup off main thread to avoid ANR
            new Thread(() -> {
                try {
                    ContactRetrieve contactRetrieve = AppUtils.getContactName(context, String.valueOf(numberPhone));
                    Bitmap avatar = AppUtils.getContactPhoto(context, String.valueOf(numberPhone));
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (contactRetrieve != null) {
                            name = contactRetrieve.getName();
                            contactId = contactRetrieve.getContact_id();
                            if (name != null && !name.isEmpty()) {
                                binding.txtName.setText(name);
                            } else {
                                binding.txtName.setText(context.getString(R.string.unknowContact));
                            }
                        } else {
                            binding.txtName.setText(context.getString(R.string.unknowContact));
                        }
                        if (avatar != null) {
                            bmpAvatar = avatar;
                            binding.profileImage.setImageBitmap(avatar);
                        }
                    });
                } catch (Exception ignored) {}
            }).start();
        } else {
            binding.txtName.setText(context.getString(R.string.unknowContact));
            binding.txtPhone.setVisibility(INVISIBLE);
        }
    }

    public void initData() {
        backgroundSelect = HawkHelper.getBackgroundSelect();
        if (backgroundSelect != null) {
            typeBgCall = backgroundSelect.getType();
            // Avatar is now loaded asynchronously in setInforContact()
            binding.vdoBackgroundCall.setVisibility(View.VISIBLE);
            if (databaseViewModel != null) {
                databaseViewModel.getContactsByContactId(contactId).observe((LifecycleOwner) context, new Observer<List<Contact>>() {
                    @Override
                    public void onChanged(List<Contact> contacts) {
                        if (contacts != null && !contacts.isEmpty()) {
                            Glide.with(IncomingCallView.this).load(R.drawable.ic_exit).into(binding.imgExit);
                            Contact mContact = contacts.get(0);
                            back_ground_contact = new Gson().fromJson(mContact.getBackground(), Background.class);

                            if (back_ground_contact != null) {
                                backgroundSelect = back_ground_contact;
                                int newTypeBgCall = back_ground_contact.getType();
                                checkTypeCall(newTypeBgCall);
                            }
                        }
                    }
                });
            }
            new Handler(Looper.getMainLooper()).postDelayed(this::startAnimation, 400);
            handlingCallState();
            listener();
        }
        analystic.trackEvent(ManagerEvent.callshow());
    }

    private void listener() {
        binding.btnAccept.setOnClickListener(v -> {
            analystic.trackEvent(ManagerEvent.callWinDowAcceptCall());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (tm != null) {
                    tm.acceptRingingCall();
                }
            } else {
                Intent intent = new Intent(context, AcceptCallActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                context.startActivity(intent);
            }
            release();
        });

        binding.btnReject.setOnClickListener(v -> {
            analystic.trackEvent(ManagerEvent.callWinDowRejectCall());
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                    if (tm != null) {
                        tm.endCall();
                    }
                } else {
                    Method m3 = telephonyService.getClass().getDeclaredMethod("endCall");
                    m3.invoke(telephonyService);
                    //telephonyService.endCall();
                }
                release();
            } catch (Exception e) {
                release();
            }
        });
        binding.imgExit.setOnClickListener(v -> {
            analystic.trackEvent(ManagerEvent.callWinDowExit());
            if (phoneState != null) {
                phoneState.release();
            }
            if (callState != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    callState.release();
                }
            }
            release();
        });
    }
    Method m1;
    private void handlingCallState() {
        telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        Class clazz;
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                clazz = Class.forName(telephonyManager.getClass().getName());
                m1 = clazz.getDeclaredMethod("getITelephony");
                m1.setAccessible(true);
                telephonyService = m1.invoke(telephonyManager);

                //telephonyService = (ITelephony) method.invoke(telephonyManager);
            }
        } catch (Exception e) {
            release();
            e.printStackTrace();
        }
    }

    private void handlingBgCallVideo() {
        String sPath;
        binding.imgBackgroundCall.setVisibility(View.GONE);
        binding.vdoBackgroundCall.setVisibility(View.VISIBLE);
        if (backgroundSelect.getPathItem().contains("storage") || backgroundSelect.getPathItem().contains("/data/data") || backgroundSelect.getPathItem().contains("data/user/")) {
            sPath = backgroundSelect.getPathItem();
        } else {
            String uriPath = "android.resource://" + context.getPackageName() + backgroundSelect.getPathItem();
            sPath = uriPath;
        }
        binding.vdoBackgroundCall.setVideoURI(Uri.parse(sPath));
        binding.vdoBackgroundCall.setOnErrorListener((mp, what, extra) -> {
            analystic.trackEvent(ManagerEvent.callVideoViewError(what, extra));
            release();
            return true;
        });
        binding.vdoBackgroundCall.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0.0f, 0.0f);
            binding.vdoBackgroundCall.start();
        });
    }

    private void handlingBgCallImage() {
        binding.imgBackgroundCall.setVisibility(View.VISIBLE);
        String sPathThumb;
        if (backgroundSelect.getPathItem().contains("default") && backgroundSelect.getPathItem().contains("thumbDefault")) {
            sPathThumb = "file:///android_asset/" + backgroundSelect.getPathItem();
        } else {
            sPathThumb = AppUtils.upgradeToHttps(backgroundSelect.getPathItem());
        }
        Glide.with(context.getApplicationContext())
                .load(sPathThumb)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .thumbnail(0.1f)
                .into(binding.imgBackgroundCall);
        binding.vdoBackgroundCall.setVisibility(View.GONE);
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
        Animation anim8 = AnimationUtils.loadAnimation(context, R.anim.anm_accept_call);
        binding.btnAccept.startAnimation(anim8);
    }

    public void release() {
        if (this.windowManager != null) {
            clearView();
            this.windowManager.removeViewImmediate(this);
            this.windowManager = null;
        }
    }

    public void clearView() {
        binding.imgBackgroundCall.setImageDrawable(null);
        binding.imgBackgroundCall.setVisibility(View.GONE);
        binding.vdoBackgroundCall.setAlpha(0.0f);
        binding.vdoBackgroundCall.stopPlayback();
        binding.vdoBackgroundCall.setVisibility(View.GONE);
        try {
            binding.btnAccept.setVisibility(View.VISIBLE);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }
}
