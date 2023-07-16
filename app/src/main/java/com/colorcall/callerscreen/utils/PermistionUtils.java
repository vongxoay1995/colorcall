package com.colorcall.callerscreen.utils;

import static android.Manifest.permission.ANSWER_PHONE_CALLS;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static com.colorcall.callerscreen.constan.Constant.PERMISSIONS_REQUEST_READ_CONTACTS;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.colorcall.callerscreen.constan.Constant;

public class PermistionUtils {

    public static void checkPermissionCall(Activity activity, PermistionCallListener listener) {
        String[] permistion;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            permistion = new String[]{
                    READ_PHONE_STATE,
                    CALL_PHONE,
                    READ_CONTACTS
            };
        } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P){
            permistion = new String[]{
                    ANSWER_PHONE_CALLS,
                    READ_PHONE_STATE,
                    CALL_PHONE,
                    READ_CONTACTS
            };
        }else {
            permistion = new String[]{
                    ANSWER_PHONE_CALLS,
                    READ_PHONE_STATE,
                    CALL_PHONE,
                    READ_CONTACTS
            };
        }

        if (!AppUtils.checkPermission(activity, permistion)) {
            ActivityCompat.requestPermissions(activity, permistion, Constant.PERMISSION_REQUEST_CODE_CALL_PHONE);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!AppUtils.canDrawOverlays(activity)) {
                    AppUtils.showDrawOverlayPermissionDialog(activity);
                } else if (!AppUtils.checkNotificationAccessSettings(activity)) {
                    AppUtils.showNotificationAccess(activity);
                } else {
                    if(listener!=null){
                        listener.onHasCallPermistion();
                    }
                }
            } else {
                if(listener!=null){
                    listener.onHasCallPermistion();
                }
            }
        }
    }
    public static void checkPermissionFlash(Activity activity, PermistionFlashListener listener) {
        String[] permistion = {
                READ_PHONE_STATE,
                CAMERA
        };
        if (!AppUtils.checkPermission(activity, permistion)) {
            ActivityCompat.requestPermissions(activity, permistion,
                    Constant.PERMISSION_REQUEST_CODE_CAMERA);
        } else {
           if (listener!=null){
               listener.onHasFlashPermistion();
           }
        }
    }
    public static void requestContactPermission(Activity activity,PermissionContactListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.READ_CONTACTS},
                        PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {
                if (listener!=null){
                    listener.onHasContactPermistion();
                }
            }
        } else {
            if (listener!=null){
                listener.onHasContactPermistion();
            }
        }
    }
}
