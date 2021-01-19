package com.colorcall.callerscreen.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.colorcall.callerscreen.utils.AppUtils;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private Listener listener;

    public void registerReceiver(Context context, Listener listener) {
        if (context == null) {
            return;
        }
        this.listener = listener;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(this, filter);
    }

    public void unregisterReceiver(Context context) {
        if (context == null) {
            return;
        }
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (this.listener != null && (TextUtils.isEmpty(action) ? "" : action).equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            this.listener.netWorkStateChanged(AppUtils.isNetworkConnected(context));
        }
    }

    public interface Listener {
        void netWorkStateChanged(boolean isNetWork);
    }
}
