package com.colorcall.callerscreen.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.FirebaseAnalystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SplashActivity extends AppCompatActivity {

    @BindView(R.id.imgBgSplash)
    ImageView imgBgSplash;
    private String ID_ADS = "ca-app-pub-3222539657172474/3893950076";
    private FirebaseAnalystic firebaseAnalystic ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        Glide.with(this).load(R.drawable.ic_bg_splash).into(imgBgSplash);
        firebaseAnalystic = FirebaseAnalystic.getInstance(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },3000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAnalystic.trackEvent(ManagerEvent.splashOpen());
    }

    @OnClick(R.id.btnSplash)
    public void onViewClicked() {
        firebaseAnalystic.trackEvent(ManagerEvent.splashStart());
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
