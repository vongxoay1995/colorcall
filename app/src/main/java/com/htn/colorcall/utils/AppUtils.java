package com.htn.colorcall.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.htn.colorcall.R;
import com.htn.colorcall.constan.Constant;
import com.htn.colorcall.listener.DialogDeleteListener;
import com.htn.colorcall.listener.DialogGalleryListener;
import com.htn.colorcall.model.Category;

import java.util.ArrayList;

public class AppUtils {
    public static boolean checkPermission(Context context, String[] permission) {
        for (String checkSelfPermission : permission) {
            if (ContextCompat.checkSelfPermission(context, checkSelfPermission) != 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkPermissionGrand(int[] grantResults) {
        boolean passed = true;
        for (int i : grantResults) {
            if (i != 0) {
                passed = false;
            }
        }
        return passed;
    }

    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            return Settings.canDrawOverlays(context);
        } else {
            if (Settings.canDrawOverlays(context)) return true;
            try {
                WindowManager mgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                if (mgr == null) return false; //getSystemService might return null
                View viewToAdd = new View(context);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(0, 0, android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
                viewToAdd.setLayoutParams(params);
                mgr.addView(viewToAdd, params);
                mgr.removeView(viewToAdd);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static void showDrawOverlayPermissionDialog(Context context) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(context.getString(R.string.draw_overlay_permision));
        alertDialog.setMessage(context.getString(R.string.draw_permision_content));
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.ok),
                (dialog, which) -> {
                    Intent intent = null;
                    if (Build.VERSION.SDK_INT >= 23) {
                        intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + context.getPackageName()));
                    }
                    ((Activity) context).startActivityForResult(intent, Constant.REQUEST_OVERLAY);
                    dialog.dismiss();
                });
        alertDialog.show();
    }

    public static boolean checkNotificationAccessSettings(Context context) {
        String string = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (TextUtils.isEmpty(string)) {
            return false;
        }
        for (String unflattenFromString : string.split(":")) {
            ComponentName unflattenFromString2 = ComponentName.unflattenFromString(unflattenFromString);
            if (unflattenFromString2 != null && TextUtils.equals(context.getPackageName(), unflattenFromString2.getPackageName())) {
                return true;
            }
        }
        return false;
    }
    public static void showNotificationAccess(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.turn_on_notifi_access))
                .setNegativeButton(R.string.ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    ((Activity)context).startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), Constant.REQUEST_NOTIFICATION_ACCESS);
                });
        builder.setCancelable(false);
        AlertDialog getNotifiAcessDialog = builder.create();
        getNotifiAcessDialog.show();
    }

    public static int dpToPx(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public static ArrayList<Category> loadDataFirst(Context context) {
        ArrayList<Category> list = new ArrayList<>();
        return list;
    }
    public static void showDialogMyGallery(Activity activity,DialogGalleryListener dialogGalleryListener){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_request_gallery);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final ImageView imgVideo, imgImages;
        imgVideo = dialog.findViewById(R.id.imgSelectVideo);
        imgImages = dialog.findViewById(R.id.imgSelectImage);
        dialog.show();
        imgVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogGalleryListener!=null){
                    dialogGalleryListener.onVideoClicked();
                }
                dialog.dismiss();
            }
        });
        imgImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogGalleryListener!=null){
                    dialogGalleryListener.onImagesClicked();
                }
                dialog.dismiss();
            }
        });
    }
    public static void showDialogDelete(Activity activity, DialogDeleteListener dialogDeleteListener) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_delete, null);
        final TextView txtNo, txtYes;
        txtNo = alertLayout.findViewById(R.id.btnNo);
        txtYes = alertLayout.findViewById(R.id.btnYes);

        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        AlertDialog dialog = alert.create();
        dialog.show();
        txtNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        txtYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogDeleteListener!=null){
                    dialogDeleteListener.onDelete();
                }
                dialog.dismiss();
            }
        });
    }

}
