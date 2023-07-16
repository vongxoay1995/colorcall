package com.colorcall.callerscreen.custom;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.android.internal.telephony.ITelephony;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.database.Contact;
import com.colorcall.callerscreen.database.ContactDao;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.model.ContactRetrieve;
import com.colorcall.callerscreen.service.AcceptCallActivity;
import com.colorcall.callerscreen.service.PhoneState;
import com.colorcall.callerscreen.service.PhoneStateService;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.DynamicImageView;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class IncomingCallView extends RelativeLayout {
    // private LayoutCallColorBinding binding;
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
    private ITelephony telephonyService;
    @BindView(R.id.txtName)
    public TextView txtName;
    @BindView(R.id.txtPhone)
    public TextView txtPhone;
    @BindView(R.id.profile_image)
    public CircleImageView profile_image;
    @BindView(R.id.imgExit)
    public ImageView imgExit;
    @BindView(R.id.vdo_background_call)
    public TextureVideoView vdo_background_call;
    @BindView(R.id.img_background_call)
    public DynamicImageView img_background_call;
    @BindView(R.id.btnAccept)
    public ImageView btnAccept;
    @BindView(R.id.btnReject)
    public ImageView btnReject;
    public PhoneState phoneState;

    public IncomingCallView(@NonNull Context context) {
        super(context);
        this.context = context;
        analystic = Analystic.getInstance(context);
        Log.e("TAN", "IncomingCallView: ");
        //binding = LayoutCallColorBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public IncomingCallView(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        analystic = Analystic.getInstance(context);
        Log.e("TAN", "IncomingCallView: 2");
    }

    public IncomingCallView(@NonNull Context context, @Nullable AttributeSet attributeSet, int i2) {
        super(context, attributeSet, i2);
        this.context = context;
        analystic = Analystic.getInstance(context);
        Log.e("TAN", "IncomingCallView: 3");
    }

    public void setNumberPhone(String numberPhone) {
        this.numberPhone = numberPhone;
        setInforContact();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.e("TAN", "onFinishInflate: ");
        /*WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        this.windowParams = layoutParams;
        layoutParams.type = Build.VERSION.SDK_INT >= 26 ? TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        WindowManager.LayoutParams layoutParams2 = this.windowParams;
        layoutParams2.format = -2;
        layoutParams2.flags = 524584;
        layoutParams2.width = -1;
        layoutParams2.height = -1;
        layoutParams2.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        layoutParams2.windowAnimations = 16973826;
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        this.windowManager = windowManager;
        this.windowManager.addView(this, this.windowParams);*/

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        this.windowParams = layoutParams;
        layoutParams.type = Build.VERSION.SDK_INT >= 26 ? 2038 : 2010;
        WindowManager.LayoutParams layoutParams2 = this.windowParams;
        layoutParams2.format = -2;
        layoutParams2.flags = 524584;
        layoutParams2.width = -1;
        layoutParams2.height = -1;
        layoutParams2.screenOrientation = 1;
        layoutParams2.windowAnimations = 16973826;
        WindowManager windowManager = (WindowManager) getContext().getSystemService("window");
        this.windowManager = windowManager;
        windowManager.addView(this, this.windowParams);
        ButterKnife.bind(this, this);
        Log.e("TAN", "onFinishInflate: " + windowManager.getDefaultDisplay().getHeight());
    }

    @Override
    protected void onAttachedToWindow() {
        Log.e("TAN", "onAttachedToWindow: ");
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.e("TAN", "onDetachedFromWindow: ");
        super.onDetachedFromWindow();
    }

    public void setInforContact() {
        if (numberPhone != null && !numberPhone.equals("")) {
            try {
                Log.e("TAN", "initData: 1");
                ContactRetrieve contactRetrieve = AppUtils.getContactName(getApplicationContext(), String.valueOf(numberPhone));
                name = contactRetrieve.getName();
                contactId = contactRetrieve.getContact_id();
                txtName.setText(name);
                if (name.equals("")) {
                    txtName.setText(context.getString(R.string.unknowContact));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            txtPhone.setText(String.valueOf(numberPhone));
        }
    }

    public void initData() {
        backgroundSelect = HawkHelper.getBackgroundSelect();
        if (backgroundSelect != null) {
            typeBgCall = backgroundSelect.getType();
            bmpAvatar = AppUtils.getContactPhoto(getApplicationContext(), String.valueOf(numberPhone));
            profile_image.setImageBitmap(bmpAvatar);
            vdo_background_call.setVisibility(View.VISIBLE);
            Glide.with(this).load(R.drawable.ic_exit).into(imgExit);
            Log.e("TAN", "initData: 2");
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
            Log.e("TAN", "initData: 3");
            checkTypeCall(typeBgCall);
            new Handler().postDelayed(this::startAnimation, 400);
            handlingCallState();
            listener();
        }
    }

    private void listener() {
        btnAccept.setOnClickListener(v -> {
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
                Intent intent = new Intent(getApplicationContext(), AcceptCallActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                getApplicationContext().startActivity(intent);
            }
            release();
        });

        btnReject.setOnClickListener(v -> {
            analystic.trackEvent(ManagerEvent.callWinDowRejectCall());
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    TelecomManager tm = (TelecomManager) getApplicationContext().getSystemService(Context.TELECOM_SERVICE);
                    if (tm != null) {
                        tm.endCall();
                    }
                } else {
                    telephonyService.endCall();
                }
                release();
            } catch (Exception e) {
                release();
            }
        });
        imgExit.setOnClickListener(v -> {
            analystic.trackEvent(ManagerEvent.callWinDowExit());
            if (phoneState != null) {
                phoneState.release();
            }
            release();
        });
    }


    private void handlingCallState() {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Class clazz;
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                clazz = Class.forName(telephonyManager.getClass().getName());
                Method method = clazz.getDeclaredMethod("getITelephony");
                method.setAccessible(true);
                telephonyService = (ITelephony) method.invoke(telephonyManager);
            }
        } catch (Exception e) {
            release();
            e.printStackTrace();
        }
    }

    private void handlingBgCallVideo() {
        Log.e("TAN", "handlingBgCallVideo: 4");
        String sPath;
        img_background_call.setVisibility(View.GONE);
        vdo_background_call.setVisibility(View.VISIBLE);
        if (backgroundSelect.getPathItem().contains("storage") || backgroundSelect.getPathItem().contains("/data/data") || backgroundSelect.getPathItem().contains("data/user/")) {
            sPath = backgroundSelect.getPathItem();
        } else {
            String uriPath = "android.resource://" + context.getPackageName() + backgroundSelect.getPathItem();

            sPath = uriPath;
        }
        Log.e("TAN", "handlingBgCallVideo: 5");
        vdo_background_call.setVideoURI(Uri.parse(sPath));
        vdo_background_call.setOnErrorListener((mp, what, extra) -> {
            Log.e("TAN", "handlingBgCallVideoERR: " + extra);
            analystic.trackEvent(ManagerEvent.callVideoViewError(what, extra));
            release();
            return true;
        });
        vdo_background_call.setOnPreparedListener(mp -> {
            Log.e("TAN", "handlingBgCallVideo: start");
            mp.setLooping(true);
            mp.setVolume(0.0f, 0.0f);
            vdo_background_call.start();
        });
        Log.e("TAN", "handlingBgCallVideo: 6");
    }

    private void handlingBgCallImage() {
        img_background_call.setVisibility(View.VISIBLE);
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
                .into(img_background_call);
        vdo_background_call.setVisibility(View.GONE);
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
        btnAccept.startAnimation(anim8);
    }

    public void release() {
        if (this.windowManager != null) {
            /*gx gxVar = this.k;
            if (gxVar.d) {
                gxVar.d = false;
                gxVar.f.cancel();
                gxVar.a();
            }*/
            clearView();
            this.windowManager.removeViewImmediate(this);
            this.windowManager = null;
            PhoneStateService.stopService(context);
        }
    }

    public void clearView() {
        img_background_call.setImageDrawable(null);
        img_background_call.setVisibility(View.GONE);
        vdo_background_call.setAlpha(0.0f);
        vdo_background_call.stopPlayback();
        vdo_background_call.setVisibility(View.GONE);
        try {
            btnAccept.setVisibility(View.VISIBLE);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }


}
