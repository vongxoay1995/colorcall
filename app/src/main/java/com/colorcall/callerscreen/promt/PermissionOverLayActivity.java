package com.colorcall.callerscreen.promt;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.constan.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PermissionOverLayActivity extends Activity {
    @BindView(R.id.txt_title)
    TextView txt_title;
    @BindView(R.id.txt_permission_content)
    TextView txt_permission_content;
    private int typePromt;
    public static void open(Context context,int typePromt) {
        if (context == null) {
            return;
        }
        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(context, PermissionOverLayActivity.class);
            intent.putExtra(Constant.TYPE_PROMPT, typePromt);
            context.startActivity(intent);
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_over_lay);
        ButterKnife.bind(this);
        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        typePromt = getIntent().getIntExtra(Constant.TYPE_PROMPT, -1);
        if (typePromt == 0) {
            txt_title.setText(getString(R.string.titleDrawrOver));
            txt_permission_content.setText(getString(R.string.prompt_permission_draw_window_msg));
        } else if (typePromt == 1) {
            txt_title.setText(getString(R.string.titleNotification));
            txt_permission_content.setText(getString(R.string.prompt_permission_notification_msg));
        } else {
            finish();
        }
    }
}