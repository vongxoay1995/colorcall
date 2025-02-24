package com.colorcall.callerscreen.utils;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WindowInsetsHelper {
    public static void applyWindowInsets(Window window, boolean allowPadding, View rootView, WindowInsetsCallback callback) {
        ViewCompat.setOnApplyWindowInsetsListener(window.getDecorView(), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottomBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            if (allowPadding && rootView instanceof ViewGroup &&
                    (rootView.getPaddingTop() + rootView.getPaddingBottom() != statusBarHeight + bottomBarHeight) &&
                    hasNavBar(v.getContext())) {
                rootView.setPadding(0, 0, 0, bottomBarHeight);
            }

            if (callback != null) {
                callback.onApplyInsets(statusBarHeight, bottomBarHeight);
            }

            return WindowInsetsCompat.CONSUMED;
        });
    }

    public interface WindowInsetsCallback {
        void onApplyInsets(int statusBarHeight, int bottomBarHeight);
    }
    public static boolean hasNavBar(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return false;
        }

        Point realPoint = new Point();
        DisplayMetrics metrics = new DisplayMetrics();

        wm.getDefaultDisplay().getRealSize(realPoint);
        wm.getDefaultDisplay().getMetrics(metrics);

        return (metrics.heightPixels + metrics.widthPixels) != (realPoint.y + realPoint.x);
    }
}
