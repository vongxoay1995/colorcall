package com.colorcall.callerscreen.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
            context.registerReceiver(this, filter,Context.RECEIVER_NOT_EXPORTED);
        }else {
            context.registerReceiver(this, filter);
        }
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
        if (listener != null && ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            listener.netWorkStateChanged(AppUtils.isNetworkConnected(context));
        }
    }

    public interface Listener {
        void netWorkStateChanged(boolean isNetWork);
    }
}
