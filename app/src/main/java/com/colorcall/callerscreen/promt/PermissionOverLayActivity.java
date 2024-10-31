package com.colorcall.callerscreen.promt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.databinding.ActivityPermissionOverLayBinding;

public class PermissionOverLayActivity extends Activity {
    private ActivityPermissionOverLayBinding binding;

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
        binding = ActivityPermissionOverLayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        typePromt = getIntent().getIntExtra(Constant.TYPE_PROMPT, -1);
        if (typePromt == 0) {
            binding.txtTitle.setText(getString(R.string.titleDrawrOver));
            binding.txtPermissionContent.setText(getString(R.string.prompt_permission_draw_window_msg));
        } else if (typePromt == 1) {
            binding.txtTitle.setText(getString(R.string.titleNotification));
            binding.txtPermissionContent.setText(getString(R.string.prompt_permission_notification_msg));
        } else {
            finish();
        }
    }
}