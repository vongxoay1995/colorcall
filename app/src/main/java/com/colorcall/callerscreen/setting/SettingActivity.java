package com.colorcall.callerscreen.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.FirebaseAnalystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.utils.HawkHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends AppCompatActivity {
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.btnCheckUpdate)
    ImageView btnCheckUpdate;
    @BindView(R.id.btnPolicy)
    ImageView btnPolicy;
    @BindView(R.id.btnShareApp)
    ImageView btnShareApp;
    @BindView(R.id.btnVip)
    ImageView btnVip;
    @BindView(R.id.layoutBottom)
    LinearLayout layoutBottom;
    @BindView(R.id.layoutCheckUpdate)
    RelativeLayout layoutCheckUpdate;
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    @BindView(R.id.layoutPolicy)
    RelativeLayout layoutPolicy;
    @BindView(R.id.layoutShareApp)
    RelativeLayout layoutShareApp;
    @BindView(R.id.layoutVip)
    RelativeLayout layoutVip;
    @BindView(R.id.swStateApp)
    SwitchCompat swStateApp;
    private FirebaseAnalystic firebaseAnalystic;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_setting);
        ButterKnife.bind( this);
        firebaseAnalystic = FirebaseAnalystic.getInstance(this);
        swStateApp.setChecked(HawkHelper.isEnableColorCall());
        listener();
    }

    private void listener() {
     swStateApp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
             HawkHelper.setStateColorCall(isChecked);
         }
     });
    }

    @OnClick({R.id.btnBack, R.id.layoutShareApp, R.id.layoutPolicy , R.id.layoutCheckUpdate,R.id.btnAds})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack :
                firebaseAnalystic.trackEvent(ManagerEvent.settingBackClick());
                finish();
                return;
            case R.id.layoutCheckUpdate :
                firebaseAnalystic.trackEvent(ManagerEvent.settingCheckUpdateClick());
                Intent intentRate = new Intent("android.intent.action.VIEW");
                StringBuilder sb = new StringBuilder();
                sb.append(Constant.PLAY_STORE_LINK);
                sb.append(getPackageName());
                intentRate.setData(Uri.parse(sb.toString()));
                startActivity(intentRate);
                return;
            case R.id.layoutPolicy :
                firebaseAnalystic.trackEvent(ManagerEvent.settingPolicyClick());
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse(Constant.POLICY_URL)));
                return;
            case R.id.layoutShareApp :
                firebaseAnalystic.trackEvent(ManagerEvent.settingShareAppClick());
                Intent sendIntent = new Intent();
                sendIntent.setAction("android.intent.action.SEND");
                StringBuilder sb2 = new StringBuilder();
                sb2.append(Constant.PLAY_STORE_LINK);
                sb2.append(getPackageName());
                sendIntent.putExtra("android.intent.extra.TEXT", sb2.toString());
                sendIntent.setType(Constant.DATA_TYPE);
                startActivity(sendIntent);
                return;
            case R.id.btnAds :
                firebaseAnalystic.trackEvent(ManagerEvent.settingAdsClick());
                return;
            default:
                return;
        }
    }

    @Override
    protected void onResume() {
        firebaseAnalystic.trackEvent(ManagerEvent.settingOpen());
        super.onResume();
    }
}
