package com.htn.colorcall.setting;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.htn.colorcall.R;
import com.htn.colorcall.constan.Constant;

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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_setting);
        ButterKnife.bind( this);
    }

    @OnClick({R.id.btnBack, R.id.layoutShareApp, R.id.layoutPolicy , R.id.layoutCheckUpdate})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack :
                finish();
                return;
            case R.id.layoutCheckUpdate :
                Intent intentRate = new Intent("android.intent.action.VIEW");
                StringBuilder sb = new StringBuilder();
                sb.append(Constant.PLAY_STORE_LINK);
                sb.append(getPackageName());
                intentRate.setData(Uri.parse(sb.toString()));
                startActivity(intentRate);
                return;
            case R.id.layoutPolicy :
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse(Constant.POLICY_URL)));
                return;
            case R.id.layoutShareApp :
                Intent sendIntent = new Intent();
                sendIntent.setAction("android.intent.action.SEND");
                StringBuilder sb2 = new StringBuilder();
                sb2.append(Constant.PLAY_STORE_LINK);
                sb2.append(getPackageName());
                sendIntent.putExtra("android.intent.extra.TEXT", sb2.toString());
                sendIntent.setType(Constant.DATA_TYPE);
                startActivity(sendIntent);
                return;
            default:
                return;
        }
    }
}
