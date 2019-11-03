package com.htn.colorcall.utils;

import android.content.Context;

import androidx.core.content.ContextCompat;

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
}
