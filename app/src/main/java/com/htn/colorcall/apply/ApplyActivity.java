package com.htn.colorcall.apply;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.htn.colorcall.R;
import com.htn.colorcall.constan.Constant;
import com.htn.colorcall.custom.CustomVideoView;
import com.htn.colorcall.database.DataManager;
import com.htn.colorcall.listener.DialogDeleteListener;
import com.htn.colorcall.model.Background;
import com.htn.colorcall.utils.AppUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.Manifest.permission.ANSWER_PHONE_CALLS;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_PHONE_STATE;

public class ApplyActivity extends AppCompatActivity implements DialogDeleteListener {
    @BindView(R.id.img_background_call)
    ImageView imgBackgroundCall;
    @BindView(R.id.vdo_background_call)
    CustomVideoView vdoBackgroundCall;
    @BindView(R.id.imgDelete)
    ImageView imgDelete;
    private Background background;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);
        ButterKnife.bind(this);
        checkInforTheme();
    }

    private void checkInforTheme() {
        if (getIntent().getBooleanExtra(Constant.SHOW_IMG_DELETE, false)) {
            imgDelete.setVisibility(View.VISIBLE);
        } else {
            imgDelete.setVisibility(View.GONE);
        }
        Gson gson = new Gson();
        background = gson.fromJson(getIntent().getStringExtra(Constant.BACKGROUND), Background.class);
        String uriPath = "android.resource://" + getPackageName() + background.getPathItem();

        Log.e("pathApply", background.getPathItem());
        if (background.getType() == 0) {
            imgBackgroundCall.setVisibility(View.GONE);
            vdoBackgroundCall.setVisibility(View.VISIBLE);
            if (background.getPathItem().contains("storage")) {
                vdoBackgroundCall.setVideoPath(background.getPathItem());
            } else {
                vdoBackgroundCall.setVideoURI(Uri.parse(uriPath));
            }
            vdoBackgroundCall.setOnPreparedListener(mediaPlayer -> mediaPlayer.setLooping(true));
            vdoBackgroundCall.start();
        } else {
            imgBackgroundCall.setVisibility(View.VISIBLE);
            if (background.getPathItem().contains("storage")) {
                Glide.with(getApplicationContext())
                        .load(background.getPathItem())
                        .apply(RequestOptions.placeholderOf(R.drawable.bg_gradient_green))
                        .into(imgBackgroundCall);
            } else {

            }
            vdoBackgroundCall.setVisibility(View.GONE);
        }
    }

    public void deleteTheme(Background background) {
        DataManager.query().getBackgroundDao().delete(background);
    }

    @Override
    protected void onResume() {
        vdoBackgroundCall.start();
        super.onResume();
    }

    @OnClick({R.id.btnBack, R.id.imgDelete, R.id.layoutApply})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                finish();
                break;
            case R.id.layoutApply:
                checkPermission();
                break;
            case R.id.imgDelete:
                AppUtils.showDialogDelete(this, this);
                break;
        }
    }

    @Override
    public void onDelete() {
        deleteTheme(background);
        Intent intent = new Intent();
        intent.putExtra(Constant.IS_DELETE_BG, true);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void checkPermission() {
        String[] permistion;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            permistion = new String[]{
                    READ_PHONE_STATE,
                    CALL_PHONE,
            };
        } else {
            permistion = new String[]{
                    ANSWER_PHONE_CALLS,
                    READ_PHONE_STATE,
                    CALL_PHONE,
            };
        }

        if (!AppUtils.checkPermission(this, permistion)) {
            ActivityCompat.requestPermissions(this, permistion, Constant.PERMISSION_REQUEST_CODE_CALL_PHONE);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!AppUtils.canDrawOverlays(this)) {
                    AppUtils.showDrawOverlayPermissionDialog(this);
                } else if (!AppUtils.checkNotificationAccessSettings(this)) {
                    AppUtils.showNotificationAccess(this);
                }
            }
        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constant.PERMISSION_REQUEST_CODE_CALL_PHONE && grantResults.length > 0 && AppUtils.checkPermissionGrand(grantResults)) {
            if (AppUtils.canDrawOverlays(this)) {
                if (!AppUtils.checkNotificationAccessSettings(this)) {
                    AppUtils.showNotificationAccess(this);
                }
            } else {
                AppUtils.checkDrawOverlayApp(this);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_OVERLAY) {
            if (AppUtils.canDrawOverlays(this)) {
                if (!AppUtils.checkNotificationAccessSettings(this)) {
                    AppUtils.showNotificationAccess(this);
                }
            }
        }
    }
}
