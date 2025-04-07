package com.colorcall.callerscreen.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.database.Contact;
import com.colorcall.callerscreen.database.DatabaseViewModel;
import com.colorcall.callerscreen.databinding.ActivityCallBinding;
import com.colorcall.callerscreen.dialer.CallContactAvatarHelper;
import com.colorcall.callerscreen.dialer.CallManager;
import com.colorcall.callerscreen.dialer.activity.CallDialerActivity;
import com.colorcall.callerscreen.dialer.models.CallContact;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.List;


public class CallActivity extends AppCompatActivity {
    public boolean isDisable;
    private String contactId="";
    private Background backgroundSelect,back_ground_contact;
    private Object telephonyService;
    private Analystic analystic;
    private CallContact callContact;
    private DatabaseViewModel databaseViewModel;
    private ActivityCallBinding binding;
    private TelephonyManager telephonyManager;
    Method m1;
    CallContactAvatarHelper callContactAvatarHelper;
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
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        callContactAvatarHelper = new CallContactAvatarHelper(this);
        callContact = new Gson().fromJson(getIntent().getStringExtra(Constant.CALL_CONTACT), CallContact.class);
        databaseViewModel = new ViewModelProvider(this).get(DatabaseViewModel.class);
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
            final int[] typeBgCall = {backgroundSelect.getType()};
           /* try {
                ContactRetrieve contactRetrieve = AppUtils.getContactName(getApplicationContext(), String.valueOf(phoneNumber));
                String name = contactRetrieve.getName();
                contactId = contactRetrieve.getContact_id();
                binding.txtName.setText(name);
                if (name.equals("")) {
                    binding.txtName.setText(getString(R.string.unknowContact));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            if (callContact!=null){
                if (!callContact.getName().equals(callContact.getNumber())){
                    binding.txtName.setText(callContact.getName());
                }else {
                    binding.txtName.setText(getString(R.string.unknowContact));
                }
                    Bitmap bmpAvatar =callContactAvatarHelper.getCallContactAvatar(callContact);
                if (bmpAvatar!=null){
                    binding.profileImage.setImageBitmap(bmpAvatar);
                }
                Log.e("TAN", "showViewCall:getContactId "+contactId+"##"+callContact.getContactId());
            }
            //Bitmap bmpAvatar = AppUtils.getContactPhoto(getApplicationContext(), String.valueOf(phoneNumber));
           // binding.profileImage.setImageBitmap(bmpAvatar);
   //        binding.txtPhone.setText(String.valueOf(phoneNumber));
            binding.vdoBackgroundCall.setVisibility(View.VISIBLE);
           /* List<Contact> listQueryContactID = DataManager.query().getContactDao().queryBuilder()
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
            checkTypeCall(typeBgCall);*/
            // Sử dụng Room và ViewModel để truy vấn danh sách Contact từ DB
            Log.e("TAN", "showViewCall:11 ");
            databaseViewModel.getContactsByContactId(callContact.getContactId()+"").observe(this, new Observer<List<Contact>>() {
                @Override
                public void onChanged(List<Contact> contacts) {
                    Log.e("TAN", "showViewCall:22 ");

                    if (contacts != null && !contacts.isEmpty()) {
                        // Xử lý kết quả trả về
                        Log.e("TAN", "showViewCall:333 ");

                        Contact mContact = contacts.get(0);
                        back_ground_contact = new Gson().fromJson(mContact.getBackground(), Background.class);

                        if (back_ground_contact != null) {
                            backgroundSelect = back_ground_contact;
                            typeBgCall[0] = back_ground_contact.getType(); // Tạo biến mới
                            Log.e("TAN", "showViewCall:444 ");

                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            checkTypeCall(typeBgCall[0]); // Sử dụng biến mới
                        }
                    });
                }
            });
           //
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
        binding.btnAccept.startAnimation(anim8);
    }

    private void handlingCallState() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        Class clazz;
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                clazz = Class.forName(telephonyManager.getClass().getName());
                m1 = clazz.getDeclaredMethod("getITelephony");
                m1.setAccessible(true);
                telephonyService = m1.invoke(telephonyManager);
            }
        } catch (Exception e) {
            finish();
            e.printStackTrace();
        }
    }

    private void listener() {
        binding.btnAccept.setOnClickListener(v -> {
            Log.e("TAN", "listener: 111");
            analystic.trackEvent(ManagerEvent.callAcceptCall());
            startActivity(CallDialerActivity.Companion.getStartIntent(this));
            CallManager.Companion.accept();
            isDisable = true;
            finish();
        });

        binding.btnReject.setOnClickListener(v -> {
            Log.e("TAN", "listener: 222");
            analystic.trackEvent(ManagerEvent.callRejectCall());
            CallManager.Companion.reject();
                isDisable = true;
                finish();
        });
    }

    private void handlingBgCallVideo() {
        String sPath;
        binding.imgBackgroundCall.setVisibility(View.GONE);
        binding.vdoBackgroundCall.setVisibility(View.VISIBLE);
        if (backgroundSelect.getPathItem().contains("storage") || backgroundSelect.getPathItem().contains("/data/data") || backgroundSelect.getPathItem().contains("data/user/")) {
            sPath = backgroundSelect.getPathItem();
        } else {
            sPath = "android.resource://" + getPackageName() + backgroundSelect.getPathItem();
        }
        binding.vdoBackgroundCall.setVideoURI(Uri.parse(sPath));
        binding.vdoBackgroundCall.setOnErrorListener((mp, what, extra) -> {
            analystic.trackEvent(ManagerEvent.callVideoViewError(what, extra));
            finish();
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
            sPathThumb = backgroundSelect.getPathItem();
        }
        Glide.with(getApplicationContext())
                .load(sPathThumb)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .thumbnail(0.1f)
                .into( binding.imgBackgroundCall);
        binding.vdoBackgroundCall.setVisibility(View.GONE);
    }
}